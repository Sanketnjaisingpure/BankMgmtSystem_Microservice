package com.bank.service;


import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.TransactionStatus;
import com.bank.ENUM.TransactionType;
import com.bank.config.KafkaConstants;
import com.bank.config.MapperConfig;
import com.bank.dto.*;
import com.bank.event.AccountCreationEvent;
import com.bank.event.TransactionEvent;
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

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final MapperConfig mapperConfig;

    private  final AccountRepository accountRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;
   
    private static final AtomicInteger sequenceCounter = new AtomicInteger(1000);


   public AccountService(AccountRepository accountRepository,
                          CustomerFeignService customerFeignService,
                          KafkaTemplate<String, Object> kafkaTemplate,
                          MapperConfig mapperConfig) {
       this.accountRepository = accountRepository;
       this.mapperConfig = mapperConfig;
       this.customerFeignService = customerFeignService;

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
        logger.info("Creating new Account for customerId={}", accountDto.getCustomerId());

        if(accountDto.getBalance().compareTo(BigDecimal.valueOf(500.0)) <= 0){
            logger.warn("Account creation failed: Balance {} is not greater than 500", accountDto.getBalance());
            throw new IllegalArgumentException("Balance must be greater than 500");
        }

        CustomerDTO customerDTO = getCustomerDetails(accountDto.getCustomerId());
        logger.info("Fetched customer details for customerId={}", accountDto.getCustomerId());

        Account account = new Account();
        account.setAccountType(accountDto.getAccountType());
        account.setBalance(accountDto.getBalance());
        account.setStatus(AccountStatus.ACTIVE);

        String accountNumber = generateAccountNumber(customerDTO.getMobileNumber());
        account.setAccountNumber(accountNumber);
        logger.info("Generated accountNumber={} for customerId={}", accountNumber, accountDto.getCustomerId());

        account.setBranchName(accountDto.getBranchName());
        account.setIfscCode(accountDto.getIfscCode());
        account.setCustomerId(accountDto.getCustomerId());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        try {

            accountRepository.save(account);
            logger.info("Account saved successfully: accountNumber={}, customerId={}", accountNumber, accountDto.getCustomerId());

            TransactionEvent transactionEvent = createTransactionEvent(accountNumber, accountDto.getBalance());
            kafkaTemplate.send(KafkaConstants.TRANSACTION_TOPIC, account.getCustomerId().toString(), transactionEvent);


            AccountCreationEvent event = new AccountCreationEvent();
            event.setAccountNumber(accountNumber);
            event.setCustomerId(accountDto.getCustomerId());
            event.setEmail(customerDTO.getEmail());
            event.setMessage("Account " + accountNumber + " created successfully");

            logger.info("Sending account creation notification via Kafka for accountNumber={}", accountNumber);
            kafkaTemplate.send(KafkaConstants.ACCOUNT_CREATION_TOPIC, accountDto.getCustomerId().toString(), event);
            logger.info("Account creation notification sent successfully for accountNumber={}", accountNumber);
        }
        catch(Exception e){
            logger.error("Account created but failed to send notification for accountNumber={}", accountNumber, e);
            throw new ResponseStatusException(HttpStatus.CREATED, "Account created successfully but failed to send notification!!!");
        }

        logger.info("Account creation completed successfully: accountNumber={}", accountNumber);
        return convertToAccountResponseDTO(account);
   }


    private TransactionEvent createTransactionEvent(String accountNumber, BigDecimal amount) {
        TransactionEvent transactionEvent = new TransactionEvent();
        transactionEvent.setTransactionDescription("Account " + accountNumber + " created successfully");
        transactionEvent.setSourceAccountNumber(accountNumber);
        transactionEvent.setDestinationAccountNumber(accountNumber);
        transactionEvent.setAmount(amount);
        transactionEvent.setTransactionType(TransactionType.DEPOSIT);
        transactionEvent.setTransactionStatus(TransactionStatus.SUCCESS);
        return transactionEvent;
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


    private TransactionEvent paymentTransaction(String sourceAccountNumber , String destinationAccountNumber, TransactionType transactionType ,BigDecimal amount ){
        TransactionEvent transactionEvent = new TransactionEvent();
        if (transactionType==TransactionType.DEPOSIT){
            transactionEvent.setTransactionDescription("Deposit");
        }else{
            transactionEvent.setTransactionDescription("Withdraw");
        }
        transactionEvent.setSourceAccountNumber(sourceAccountNumber);
        transactionEvent.setDestinationAccountNumber(destinationAccountNumber);
        transactionEvent.setAmount(amount);
        transactionEvent.setTransactionType(transactionType);
        transactionEvent.setTransactionStatus(TransactionStatus.SUCCESS);
        return transactionEvent;
    }

    @Transactional
    public AccountResponseDTO depositCredit(String accountNumber, BigDecimal amount) {
        logger.info("Processing deposit: accountNumber={}, amount={}", accountNumber, amount);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Deposit failed: Invalid amount {} for accountNumber={}", amount, accountNumber);
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }

        Account account = getAccountEntity(accountNumber);
        logger.info("Found account: accountNumber={}, currentBalance={}", accountNumber, account.getBalance());

        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());

        try {
            accountRepository.save(account);
            logger.info("Deposit saved: accountNumber={}, amount={}, newBalance={}", accountNumber, amount, account.getBalance());

            sendTransactionNotification(account, accountNumber, amount, TransactionType.DEPOSIT);
            logger.info("Deposit notification sent: accountNumber={}, amount={}", accountNumber, amount);

            TransactionEvent transactionEvent = paymentTransaction(accountNumber, accountNumber, TransactionType.DEPOSIT, amount);
            kafkaTemplate.send(KafkaConstants.TRANSACTION_PAYMENT_TOPIC, transactionEvent);
        }
        catch (Exception e) {
            logger.error("Deposit notification failed but account updated: accountNumber={}, amount={}", accountNumber, amount, e);
        }

        logger.info("Deposit completed: accountNumber={}, amount={}", accountNumber, amount);
        return convertToAccountResponseDTO(account);
    }

    @Transactional
    public AccountResponseDTO withdrawDebit(String accountNumber, BigDecimal amount) {
        logger.info("Processing withdrawal: accountNumber={}, amount={}", accountNumber, amount);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Withdrawal failed: Invalid amount {} for accountNumber={}", amount, accountNumber);
            throw new IllegalArgumentException("Withdraw amount must be greater than 0");
        }

        Account account = getAccountEntity(accountNumber);
        logger.info("Found account: accountNumber={}, currentBalance={}", accountNumber, account.getBalance());

        if (account.getBalance().compareTo(amount) < 0) {
            logger.warn("Withdrawal failed: Insufficient balance for accountNumber={}, requested={}, available={}",
                    accountNumber, amount, account.getBalance());
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(LocalDateTime.now());

        try {
            accountRepository.save(account);
            logger.info("Withdrawal saved: accountNumber={}, amount={}, newBalance={}", accountNumber, amount, account.getBalance());

            sendTransactionNotification(account, accountNumber, amount, TransactionType.WITHDRAW);
            logger.info("Withdrawal notification sent: accountNumber={}, amount={}", accountNumber, amount);
        }
        catch (Exception e) {
            logger.error("Withdrawal notification failed but account updated: accountNumber={}, amount={}", accountNumber, amount, e);
        }

        logger.info("Withdrawal completed: accountNumber={}, amount={}", accountNumber, amount);
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
