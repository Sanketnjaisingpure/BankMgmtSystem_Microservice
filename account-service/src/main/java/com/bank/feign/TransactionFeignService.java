package com.bank.feign;

import com.bank.dto.TransactionRecordRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "TRANSACTION-SERVICE", url = "http://localhost:8083")
public interface TransactionFeignService {

    @PostMapping("/api/v1/transactions/record-transaction")
    void recordTransaction(@RequestBody TransactionRecordRequestDTO requestDTO);
}

