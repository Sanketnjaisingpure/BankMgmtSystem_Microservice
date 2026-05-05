package com.bank.feign;

import com.bank.dto.AccountRequestDTO;
import com.bank.dto.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for account-service.
 *
 * <p>Bank-service uses this to:
 * <ol>
 *   <li>Create a new bank account for a customer ({@link #createAccount})</li>
 *   <li>Fetch account details by account number ({@link #getAccountByAccountNumber})</li>
 * </ol>
 */
@FeignClient(name = "ACCOUNT-SERVICE", url = "http://localhost:8081")
public interface AccountFeignService {

    /**
     * Delegates account creation to account-service.
     * Called inside {@code BankService.createAccount()}.
     */
    @PostMapping("/api/v1/accounts/create-account")
    ResponseEntity<AccountResponseDTO> createAccount(@RequestBody AccountRequestDTO request);

    /** Fetches account details by account number. */
    @GetMapping("/api/v1/accounts/get-account-by-account-number")
    ResponseEntity<AccountResponseDTO> getAccountByAccountNumber(@RequestParam String accountNumber);
}
