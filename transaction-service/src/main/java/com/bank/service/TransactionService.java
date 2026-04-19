package com.bank.service;


import com.bank.config.KafkaConstants;
import com.bank.event.TransactionEvent;
import com.bank.model.Transaction;
import com.bank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @KafkaListener(topics = {KafkaConstants.TRANSACTION_TOPIC}, groupId = KafkaConstants.TRANSACTION_GROUP )
    public void recordTransaction(TransactionEvent event, Acknowledgment ack) {

        logger.info("Received transaction event: type={}, amount={}, sourceAccount={}, destinationAccount={}",
                event.getTransactionType(),
                event.getAmount(),
                event.getSourceAccountNumber(),
                event.getDestinationAccountNumber());

        try {
            Transaction transaction = new Transaction();
            transaction.setTransactionDescription(event.getTransactionDescription());
            transaction.setTransactionType(event.getTransactionType());
            transaction.setAmount(event.getAmount());
            transaction.setSourceAccountNumber(event.getSourceAccountNumber());
            transaction.setDestinationAccountNumber(event.getDestinationAccountNumber());
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setTransactionStatus(event.getTransactionStatus());

            transactionRepository.save(transaction);

            ack.acknowledge();

            logger.info("Transaction saved successfully: type={}, amount={}, sourceAccount={}",
                    event.getTransactionType(),
                    event.getAmount(),
                    event.getSourceAccountNumber());

        } catch (Exception e) {

            logger.error("Failed to process transaction: type={}, amount={}, sourceAccount={}, error={}",
                    event.getTransactionType(),
                    event.getAmount(),
                    event.getSourceAccountNumber(),
                    e.getMessage(),
                    e); // ✅ important: logs full stack trace
        }
    }

    @KafkaListener(topics = KafkaConstants.TRANSACTION_PAYMENT_TOPIC, groupId = KafkaConstants.TRANSACTION_PAYMENT_GROUP)
    public void recordTransactionForDepositOrWithdrawal(TransactionEvent event, Acknowledgment ack) {
        logger.info("Received payment transaction event: type={}, amount={}, sourceAccount={}, destinationAccount={}",
                event.getTransactionType(),
                event.getAmount(),
                event.getSourceAccountNumber(),
                event.getDestinationAccountNumber());

        try {
            Transaction transaction = new Transaction();
            transaction.setTransactionDescription(event.getTransactionDescription());
            transaction.setTransactionType(event.getTransactionType());
            transaction.setAmount(event.getAmount());
            transaction.setSourceAccountNumber(event.getSourceAccountNumber());
            transaction.setDestinationAccountNumber(event.getDestinationAccountNumber());
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setTransactionStatus(event.getTransactionStatus());

            transactionRepository.save(transaction);

            ack.acknowledge();

            logger.info("Payment transaction saved successfully: type={}, amount={}, account={}",
                    event.getTransactionType(),
                    event.getAmount(),
                    event.getSourceAccountNumber());

        } catch (Exception e) {
            logger.error("Failed to process payment transaction: type={}, amount={}, account={}, error={}",
                    event.getTransactionType(),
                    event.getAmount(),
                    event.getSourceAccountNumber(),
                    e.getMessage(),
                    e);
        }
    }


}
