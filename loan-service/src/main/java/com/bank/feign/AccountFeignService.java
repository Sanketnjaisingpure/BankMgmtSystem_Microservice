package com.bank.feign;

import com.bank.dto.accounts.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ACCOUNT-SERVICE", url = "http://localhost:8081")
public interface AccountFeignService {

    @GetMapping("/api/v1/accounts/get-account-by-account-number")
    ResponseEntity<AccountResponseDTO> getAccountByAccountNumber(@RequestParam String accountNumber);

    /**
     * Credits (deposits) the given amount into the customer's account.
     * Used during loan disbursement to transfer the approved loan amount.
     *
     * @param idempotencyKey unique key to prevent duplicate credits (loanId is a good choice)
     * @param accountNumber  the target account to credit
     * @param amount         the amount to deposit
     * @return the updated account details
     */
    @PutMapping("/api/v1/accounts/{accountNumber}/depositCredit")
    ResponseEntity<AccountResponseDTO> depositCredit(
            @RequestHeader("Idempotency-key") String idempotencyKey,
            @PathVariable String accountNumber,
            @RequestParam java.math.BigDecimal amount
    );
}


