package com.bank.service;

import com.bank.dto.TransactionRecordRequestDTO;
import com.bank.feign.TransactionFeignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TransactionAsyncService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionAsyncService.class);

    private final TransactionFeignService transactionFeignService;

    public TransactionAsyncService(TransactionFeignService transactionFeignService) {
        this.transactionFeignService = transactionFeignService;
    }

    @Async("transactionExecutor")
    public void recordTransaction(TransactionRecordRequestDTO requestDTO) {
        try {
            transactionFeignService.recordTransaction(requestDTO);
        } catch (Exception e) {
            // Best-effort: don't fail the account operation if transaction logging fails.
            logger.warn("Failed to record transaction asynchronously for source={}, destination={}, amount={}",
                    requestDTO.getSourceAccountNumber(),
                    requestDTO.getDestinationAccountNumber(),
                    requestDTO.getAmount(),
                    e);
        }
    }
}

