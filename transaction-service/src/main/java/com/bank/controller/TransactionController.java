package com.bank.controller;

import com.bank.dto.TransactionRecordRequestDTO;
import com.bank.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/record-transaction")
    public ResponseEntity<Void> recordTransaction(@RequestBody TransactionRecordRequestDTO requestDTO) {
        logger.info("Received transaction record request: type={}, amount={}", requestDTO.getTransactionType(),
                requestDTO.getAmount());
        transactionService.recordTransaction(requestDTO);
        // Service method is async; return immediately.
        return ResponseEntity.accepted().build();
    }

}
