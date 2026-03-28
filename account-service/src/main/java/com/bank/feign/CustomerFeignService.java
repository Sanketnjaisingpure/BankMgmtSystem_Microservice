package com.bank.feign;

import com.bank.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name="CUSTOMER-SERVICE")
public interface CustomerFeignService {

    @GetMapping("/find-by-id")
    ResponseEntity<CustomerDTO> findById(@RequestParam UUID customerId);
}
