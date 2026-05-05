package com.bank.feign;

import com.bank.dto.BankDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client for bank-service.
 *
 * <p>Used by account-service to validate that a bank exists and is ACTIVE
 * before linking it to a new account. Called only when the caller provides
 * a non-null {@code bankId} in {@code AccountRequestDTO}.
 */
@FeignClient(name = "BANK-SERVICE", url = "http://localhost:8086")
public interface BankFeignService {

    /**
     * Fetches bank details by UUID.
     * Returns 404 (propagated as Feign exception) if the bank does not exist.
     */
    @GetMapping("/api/v1/banks/{bankId}")
    ResponseEntity<BankDTO> getBankById(@PathVariable UUID bankId);
}
