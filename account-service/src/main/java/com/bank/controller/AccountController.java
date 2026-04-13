package com.bank.controller;

import com.bank.ENUM.AccountStatus;
import com.bank.dto.AccountRequestDTO;
import com.bank.dto.AccountResponseDTO;
import com.bank.dto.PageResponse;
import com.bank.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create-account")
    public ResponseEntity<AccountResponseDTO> createAccount(@RequestBody AccountRequestDTO accountDto) {
        logger.info("Received Request to create new Account");
        AccountResponseDTO dto = accountService.createAccount(accountDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }


    @GetMapping("/get-account-by-account-number")
    public ResponseEntity<AccountResponseDTO> getAccountByAccountNumber(@RequestParam String accountNumber) {
        logger.info("Received Request to fetch Account by Account Number : {}", accountNumber);
        AccountResponseDTO dto = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(dto);
    }


    @GetMapping("/get-all-accounts-by-customer-id")
    public ResponseEntity<PageResponse<AccountResponseDTO>> getAllAccountsByCustomerId(
            @RequestParam UUID customerId,
            @RequestParam(value = "pageNumber" ,defaultValue = "0") int pageNumber ,
            @RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize,
            @RequestParam(value = "sort" ,defaultValue = "accountNumber") String sort,
            @RequestParam(value = "order" ,defaultValue = "asc") String order) {


        logger.info("Received Request to fetch All Accounts for Customer with Customer ID : {} ", customerId);
        PageResponse<AccountResponseDTO> dto = accountService.getAllAccountsByCustomerId(customerId, pageNumber, pageSize, sort, order);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/get-account-by-Id")
    public ResponseEntity<AccountResponseDTO> getAccountById(@RequestParam UUID accountId) {
        logger.info("Received Request to fetch Account by Account ID : {} ", accountId);
        AccountResponseDTO dto = accountService.getAccountById(accountId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/update-status")
    public ResponseEntity<String> updateAccountStatus(@RequestParam String accountNumber, @RequestParam AccountStatus status) {
        logger.info("Received Request to update Account Status for Account Number : {} ", accountNumber);
        String message = accountService.updateAccountStatus(accountNumber, status);
        return ResponseEntity.ok(message);
    }

    @GetMapping("get-balance")
    public ResponseEntity<AccountResponseDTO> getBalance(@RequestParam String accountNumber) {
        logger.info("Received Request to fetch Balance for Account Number : {} ", accountNumber);
        AccountResponseDTO balance = accountService.getAccountBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @PutMapping("/{accountNumber}/depositCredit")
    public ResponseEntity<AccountResponseDTO> depositCredit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount
    ) {
        logger.info("Received Request to deposit amount={} to accountNumber={}", amount, accountNumber);
        AccountResponseDTO dto = accountService.depositCredit(accountNumber, amount);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{accountNumber}/withdrawDebit")
    public ResponseEntity<AccountResponseDTO> withdrawDebit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount
    ) {
        logger.info("Received Request to withdraw amount={} from accountNumber={}", amount, accountNumber);
        AccountResponseDTO dto = accountService.withdrawDebit(accountNumber, amount);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/delete-account-by-Id")
    public ResponseEntity<String> deleteAccountById(@RequestParam UUID accountId) {
        logger.info("Received Request to delete Account by Account ID : {} ", accountId);
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

}
