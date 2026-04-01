package com.bank.service;


import com.bank.ENUM.AccountStatus;
import com.bank.config.MapperConfig;
import com.bank.dto.*;
import com.bank.exception.ResourceNotFoundException;
import com.bank.feign.CustomerFeignService;
import com.bank.feign.NotificationFeignService;
import com.bank.model.Account;
import com.bank.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AccountService {

    private final CustomerFeignService customerFeignService;

    private final NotificationFeignService notificationFeignService;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final MapperConfig mapperConfig;

   private  final AccountRepository accountRepository;
   
   private static final AtomicInteger sequenceCounter = new AtomicInteger(1000);


   public AccountService(AccountRepository accountRepository, NotificationFeignService notificationFeignService,CustomerFeignService customerFeignService , MapperConfig mapperConfig) {
       this.accountRepository = accountRepository;
       this.notificationFeignService = notificationFeignService;
       this.mapperConfig = mapperConfig;
       this.customerFeignService = customerFeignService;
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


    public AccountResponseDTO createAccount(AccountRequestDTO accountDto) {
        logger.info("Creating new Account");

        if(accountDto.getBalance().compareTo(BigDecimal.valueOf(500.0)) <= 0){
            throw new IllegalArgumentException("Balance must be greater than 500");
        }

        CustomerDTO customerDTO;
        try {

            customerDTO = customerFeignService.findById(accountDto.getCustomerId()).getBody();
            logger.info("customer details fetch successfully !!!");
        }
        catch(Exception e){
            logger.error("Failed to fetch customer", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch customer");
        }

        if(customerDTO == null){
            throw new ResourceNotFoundException("Customer Not Found");
        }

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
            NotificationRequestDTO dto = new NotificationRequestDTO();
            dto.setCustomerId(accountDto.getCustomerId());
            dto.setMessage("Account created successfully");
            dto.setEmail(customerDTO.getEmail());
            dto.setPhone(customerDTO.getMobileNumber());
            notificationFeignService.sendNotification(dto);
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



//    PUT/api/accounts/{accountId}/depositCredit amount to account→ Notification Service


//    PUT/api/accounts/{accountId}/withdrawDebit amount from account→ Notification Service


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


}
