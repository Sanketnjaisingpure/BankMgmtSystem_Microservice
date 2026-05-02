package com.bank.feign;

import com.bank.dto.accounts.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Feign client for the Account Service.
 *
 * <p>Used to validate account ownership and to debit payment amounts
 * from the customer's linked bank account.</p>
 */
@FeignClient(name = "ACCOUNT-SERVICE", url = "http://localhost:8081")
public interface AccountFeignService {

    /** Fetch account details by account number. */
    @GetMapping("/api/v1/accounts/get-account-by-account-number")
    ResponseEntity<AccountResponseDTO> getAccountByAccountNumber(@RequestParam String accountNumber);

    /**
     * Debits (withdraws) the given amount from the customer's account.
     * Used when a customer makes a credit card payment from their bank account.
     */
    @PutMapping("/api/v1/accounts/{accountNumber}/withdrawDebit")
    ResponseEntity<AccountResponseDTO> withdrawDebit(
            @RequestHeader("Idempotency-key") String idempotencyKey,
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount
    );
}
