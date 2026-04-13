package com.bank.service;

import com.bank.ENUM.TransactionStatus;
import com.bank.ENUM.TransactionType;
import com.bank.dto.TransactionRecordRequestDTO;
import com.bank.model.Transaction;
import com.bank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Async("transactionExecutor")
    public void recordTransaction(TransactionRecordRequestDTO requestDTO) {
        try {
            TransactionType transactionType = TransactionType.valueOf(requestDTO.getTransactionType());

            Transaction transaction = new Transaction();
            transaction.setTransactionType(transactionType);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transaction.setAmount(requestDTO.getAmount());
            transaction.setSourceAccountNumber(requestDTO.getSourceAccountNumber());
            transaction.setDestinationAccountNumber(requestDTO.getDestinationAccountNumber());
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setTransactionDescription(
                    requestDTO.getTransactionDescription() != null
                            ? requestDTO.getTransactionDescription()
                            : "Transaction recorded"
            );

            transactionRepository.save(transaction);

            logger.info(
                    "Transaction recorded: type={}, amount={}, source={}, destination={}",
                    transactionType,
                    requestDTO.getAmount(),
                    requestDTO.getSourceAccountNumber(),
                    requestDTO.getDestinationAccountNumber()
            );
        } catch (Exception e) {
            // Best-effort: attempt to save FAILED transaction if request is sufficiently valid.
            logger.error("Failed to record transaction asynchronously", e);
            try {
                TransactionType transactionType = TransactionType.valueOf(requestDTO.getTransactionType());

                Transaction transaction = new Transaction();
                transaction.setTransactionType(transactionType);
                transaction.setTransactionStatus(TransactionStatus.FAILED);
                transaction.setAmount(requestDTO.getAmount());
                transaction.setSourceAccountNumber(requestDTO.getSourceAccountNumber());
                transaction.setDestinationAccountNumber(requestDTO.getDestinationAccountNumber());
                transaction.setCreatedAt(LocalDateTime.now());
                transaction.setTransactionDescription(
                        requestDTO.getTransactionDescription() != null
                                ? requestDTO.getTransactionDescription()
                                : "Transaction failed: " + e.getMessage()
                );

                transactionRepository.save(transaction);
            } catch (Exception ignored) {
                // Swallow to keep async worker from crashing.
            }
        }
    }
}
