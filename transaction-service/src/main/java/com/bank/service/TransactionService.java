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

    @KafkaListener(
            topics = KafkaConstants.TRANSACTION_TOPIC,
            groupId = KafkaConstants.TRANSACTION_GROUP
    )
    public void recordTransaction(TransactionEvent event, Acknowledgment ack) {

        logger.info("Received TRANSACTION event: type={}, amount={}",
                event.getTransactionType(), event.getAmount());

        try {
            processTransactionEvent(event);

            ack.acknowledge();
            logger.info("ACK success: TRANSACTION event");

        } catch (Exception e) {
            logger.error("Failed to process TRANSACTION event: type={}, amount={}",
                    event.getTransactionType(),
                    event.getAmount(),
                    e);

            // ❗ No ACK → Kafka retry
        }
    }

    @KafkaListener(
            topics = KafkaConstants.TRANSACTION_PAYMENT_TOPIC,
            groupId = KafkaConstants.TRANSACTION_PAYMENT_GROUP
    )
    public void recordTransactionForDepositOrWithdrawal(TransactionEvent event,
                                                        Acknowledgment ack) {

        logger.info("Received PAYMENT event: type={}, amount={}",
                event.getTransactionType(), event.getAmount());

        try {
            processTransactionEvent(event);

            ack.acknowledge();
            logger.info("ACK success: PAYMENT event");

        } catch (Exception e) {
            logger.error("Failed to process PAYMENT event: type={}, amount={}",
                    event.getTransactionType(),
                    event.getAmount(),
                    e);

            // ❗ No ACK → retry
        }
    }

    private void processTransactionEvent(TransactionEvent event) {

        logger.info("Processing transaction: type={}, amount={}, sourceAccount={}, destinationAccount={}",
                event.getTransactionType(),
                event.getAmount(),
                event.getSourceAccountNumber(),
                event.getDestinationAccountNumber());

        // ❗ (Optional for now) Idempotency placeholder
        // if (transactionRepository.existsByTransactionId(event.getTransactionId())) {
        //     logger.warn("Duplicate transaction detected. Skipping...");
        //     return;
        // }

        Transaction transaction = new Transaction();
        transaction.setTransactionDescription(event.getTransactionDescription());
        transaction.setTransactionType(event.getTransactionType());
        transaction.setAmount(event.getAmount());
        transaction.setSourceAccountNumber(event.getSourceAccountNumber());
        transaction.setDestinationAccountNumber(event.getDestinationAccountNumber());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setTransactionStatus(event.getTransactionStatus());

        transactionRepository.save(transaction);

        logger.info("Transaction saved successfully: type={}, amount={}, sourceAccount={}",
                event.getTransactionType(),
                event.getAmount(),
                event.getSourceAccountNumber());
    }
}
