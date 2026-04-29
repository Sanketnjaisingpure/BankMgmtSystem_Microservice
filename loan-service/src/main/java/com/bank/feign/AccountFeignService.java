package com.bank.feign;

import com.bank.dto.CustomerDTO;
import com.bank.dto.accounts.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name="ACCOUNT-SERVICE",url = "http://localhost:8081")
public interface AccountFeignService {

    @GetMapping("/api/v1/accounts/get-account-by-account-number")
    public ResponseEntity<AccountResponseDTO> getAccountByAccountNumber(@RequestParam String accountNumber);
}


