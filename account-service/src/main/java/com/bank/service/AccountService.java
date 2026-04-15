package com.bank.service;


import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.TransactionType;
import com.bank.config.KafkaConstants;
import com.bank.config.MapperConfig;
import com.bank.dto.*;
import com.bank.event.AccountCreationEvent;
import com.bank.event.TransactionNotificationEvent;
import com.bank.exception.ResourceNotFoundException;
import com.bank.feign.CustomerFeignService;
import com.bank.model.Account;
import com.bank.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final CustomerFeignService customerFeignService;

    private final TransactionAsyncService transactionAsyncService;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final MapperConfig mapperConfig;

    private  final AccountRepository accountRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;
   
    private static final AtomicInteger sequenceCounter = new AtomicInteger(1000);


   public AccountService(AccountRepository accountRepository,

                          CustomerFeignService customerFeignService,
                          TransactionAsyncService transactionAsyncService,
                          KafkaTemplate<String, Object> kafkaTemplate,
                          MapperConfig mapperConfig) {
       this.accountRepository = accountRepository;
       this.mapperConfig = mapperConfig;
       this.customerFeignService = customerFeignService;
       this.transactionAsyncService = transactionAsyncService;
       this.kafkaTemplate = kafkaTemplate;
   }

   private String generateAccountNumber(String mobileNumber) {
       // Get last 4 digits of mobile number
       String last4Mobile = mobileNumber.length() >= 4 ? 
           mobileNumber.substring(mobileNumber.length() - 4) : 
           String.format("%04d", Math.abs(mobileNumber.hashCode() % 10000));
       
       // Get 4 digits from timestamp (last 4 digits of current epoch time in seconds)
       String timestamp4 = String.format("%04d", Instant.now().getEpochSecond() % 10000);
       
       // Get 4 digits based on sequence logic (incrementing counter)
       int sequence = sequenceCounter.getAndIncrement() % 10000;
       String sequence4 = String.format("%04d", sequence);
       
       // Combine all parts: last4Mobile + timestamp4 + sequence4
       return last4Mobile + timestamp4 + sequence4;
   }

   public CustomerDTO getCustomerDetails(UUID customerId) {
       CustomerDTO customerDTO;
       try {

           customerDTO = customerFeignService.findById(customerId).getBody();

           if(customerDTO==null){
               logger.error("Customer Not Found");
               throw new ResourceNotFoundException("Customer Not Found");
           }
           logger.info("customer details fetch successfully !!!");
       }
       catch(Exception e){
           logger.error("Failed to fetch customer", e);
           throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch customer");
       }
       return customerDTO;
   }


    public AccountResponseDTO createAccount(AccountRequestDTO accountDto) {
        logger.info("Creating new Account");

        if(accountDto.getBalance().compareTo(BigDecimal.valueOf(500.0)) <= 0){
            throw new IllegalArgumentException("Balance must be greater than 500");
        }

        CustomerDTO customerDTO =  getCustomerDetails(accountDto.getCustomerId());

        Account account = new Account();
        account.setAccountType(accountDto.getAccountType());


        account.setBalance(accountDto.getBalance());
        account.setStatus(AccountStatus.ACTIVE);
        
        // Generate unique account number using customer's mobile number
        String accountNumber = generateAccountNumber(customerDTO.getMobileNumber());
        account.setAccountNumber(accountNumber);
        
        account.setBranchName(accountDto.getBranchName());
        account.setIfscCode(accountDto.getIfscCode());
        account.setCustomerId(accountDto.getCustomerId());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        try{
            logger.info("account created successfully");
            accountRepository.save(account);

            // sending Notification


            AccountCreationEvent event = new AccountCreationEvent();
            event.setAccountNumber(accountNumber);
            event.setCustomerId(accountDto.getCustomerId());
            event.setEmail(customerDTO.getEmail());
            event.setMessage("Account "+accountNumber+" created  successfully");
            logger.info("sending account creation notification via Kafka ");
            kafkaTemplate.send(KafkaConstants.ACCOUNT_CREATION_TOPIC , accountDto.getCustomerId().toString()  , event);



            // Record initial balance as a DEPOSIT transaction (best-effort / async).
          /* TransactionRecordRequestDTO transactionRecordRequestDTO = new TransactionRecordRequestDTO();
            transactionRecordRequestDTO.setSourceAccountNumber(account.getAccountId());
            transactionRecordRequestDTO.setDestinationAccountNumber(account.getAccountId());
            transactionRecordRequestDTO.setAmount(accountDto.getBalance());
            transactionRecordRequestDTO.setTransactionType("DEPOSIT");
            transactionRecordRequestDTO.setTransactionDescription("Initial account balance");
            transactionAsyncService.recordTransaction(transactionRecordRequestDTO);*/
        }
        catch(Exception e){
            logger.error("Account Created Successfully but failed to send notification", e);
            throw new ResponseStatusException(HttpStatus.CREATED, "Account created successfully but failed to send notification!!!");
        }
        return convertToAccountResponseDTO(account);
   }


    private Account getAccountEntity(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber);

        if (account == null) {
            logger.warn("Account Not Found with Account Number : {}", accountNumber);
            throw new ResourceNotFoundException("Account Not Found");
        }

        return account;
    }

    public AccountResponseDTO getAccountByAccountNumber(String accountNumber) {
        logger.info("Fetching Account using Account Number : {} ", accountNumber);
       Account account = getAccountEntity(accountNumber);

       logger.info("Account Found Successfully with Account Number : {} ",accountNumber );
        return convertToAccountResponseDTO(account);
    }


    public PageResponse<AccountResponseDTO> getAllAccountsByCustomerId(UUID customerId, int pageNumber, int pageSize, String sortBy, String order) {
        logger.info("Fetching All Accounts for Customer with Customer ID : {} ", customerId);

        Sort sort = Sort.by(sortBy).ascending();
        if (sortBy.equalsIgnoreCase(order)){
            sort = Sort.by(sortBy).descending();
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize,sort);

        Page<Account> pageAccount = accountRepository.findAccountsByCustomerId(customerId, pageable);

        List<AccountResponseDTO> list = pageAccount.getContent().stream().filter(account -> account.getCustomerId().equals(customerId)).map(this::convertToAccountResponseDTO).toList();


        return new PageResponse<>(list, pageAccount.getNumber(), pageAccount.getTotalElements(), pageAccount.getTotalPages());

    }

    public AccountResponseDTO getAccountById(UUID accountId){
        logger.info("Fetching Account using Account ID : {} ", accountId);
       Account account = accountRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException("Account Not Found"));
       return convertToAccountResponseDTO(account);
    }

    public String updateAccountStatus(String accountNumber , AccountStatus status){
       logger.info("Updating Account Status for Account Number : {} ", accountNumber);
       Account account = getAccountEntity(accountNumber);
       account.setStatus(status);
       account.setUpdatedAt(LocalDateTime.now());
       accountRepository.save(account);
       logger.info("Account status updated successfully for Account Number : {}", accountNumber);
        return "Account Status is updated !!!";
    }

    public AccountResponseDTO getAccountBalance(String accountNumber) {
        logger.info("Fetching Account Balance using Account Number : {} ", accountNumber);
        Account account = getAccountEntity(accountNumber);
        return convertToAccountResponseDTO(account);
    }

    @Transactional
    public AccountResponseDTO depositCredit(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }

        Account account = getAccountEntity(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());
        try {

            accountRepository.save(account);
            logger.info("Amount Deposited successfully {} to account {}", amount, accountNumber);
            // Best-effort async transaction logging.
            logger.info("Logging transaction for deposit of {} to account {}", amount, accountNumber);

            sendTransactionNotification(account, accountNumber, amount, TransactionType.DEPOSIT);


            logger.info("Transaction logged successfully");
        }
        catch (Exception e) {
            // TODO: handle exception
            // transaction should never break logic
            logger.error("Transaction failed but account updated", e);
        }

        return convertToAccountResponseDTO(account);
    }

    @Transactional
    public AccountResponseDTO withdrawDebit(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be greater than 0");
        }

        Account account = getAccountEntity(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(LocalDateTime.now());
        try {
            accountRepository.save(account);

            sendTransactionNotification(account, accountNumber, amount, TransactionType.WITHDRAW);

        }
        catch (Exception e) {
            // TODO: handle exception
            // transaction should never break logic
            logger.error("Transaction failed but account updated", e);
        }
        // Best-effort async transaction logging.
       /* TransactionRecordRequestDTO tx = new TransactionRecordRequestDTO();
        tx.setSourceAccountNumber(account.getAccountId());
        tx.setDestinationAccountNumber(account.getAccountId());
        tx.setAmount(amount);
        tx.setTransactionType("WITHDRAW");
        tx.setTransactionDescription("Withdraw debit of " + amount);
        transactionAsyncService.recordTransaction(tx);*/

        return convertToAccountResponseDTO(account);
    }


    public void deleteAccount(UUID accountId) {
        logger.info("Deleting Account using Account ID : {} ", accountId);
        try {
            accountRepository.deleteById(accountId);
        }
        catch (Exception e){
            logger.error("Failed to delete Account using Account ID : {} ", accountId, e);
            throw new ResourceNotFoundException("Failed to delete Account");
        }
        logger.info("Account deleted successfully using Account ID : {} ", accountId);
    }

    public AccountResponseDTO convertToAccountResponseDTO(Account account) {
        return mapperConfig.modelMapper().map(account, AccountResponseDTO.class);
    }

    private void sendTransactionNotification(Account account, String accountNumber, BigDecimal amount, TransactionType transactionType) {

        TransactionNotificationEvent transactionNotificationEvent = new TransactionNotificationEvent();
        transactionNotificationEvent.setAccountNumber(accountNumber);
        transactionNotificationEvent.setCustomerId(account.getCustomerId());
        transactionNotificationEvent.setTransactionType(transactionType);
        if(transactionType == TransactionType.DEPOSIT){
            transactionNotificationEvent.setMessage("Amount " + amount + " is deposited in your Account " + accountNumber);
        }
        else{
            transactionNotificationEvent.setMessage("Amount " + amount + " is withdrawn from your Account " + accountNumber);
        }

        transactionNotificationEvent.setAmount(amount);
        kafkaTemplate.send(KafkaConstants.TRANSACTION_NOTIFICATION_TOPIC, transactionNotificationEvent);
    }

}
