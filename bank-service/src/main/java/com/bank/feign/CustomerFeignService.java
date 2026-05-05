package com.bank.feign;

import com.bank.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Feign client for customer-service.
 * Used by bank-service to validate that a customer exists
 * before opening an account on their behalf.
 */
@FeignClient(name = "CUSTOMER-SERVICE", url = "http://localhost:8080")
public interface CustomerFeignService {

    @GetMapping("/api/v1/customers/find-by-id")
    ResponseEntity<CustomerDTO> findById(@RequestParam UUID customerId);
}
