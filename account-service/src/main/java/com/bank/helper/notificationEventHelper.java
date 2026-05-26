package com.bank.helper;

import com.bank.ENUM.NotificationType;
import com.bank.ENUM.SourceService;
import com.bank.ENUM.TransactionType;
import com.bank.config.KafkaConstants;
import com.bank.event.TransactionNotificationEvent;
import com.bank.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class notificationEventHelper {


    private final Logger logger = LoggerFactory.getLogger(notificationEventHelper.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public notificationEventHelper(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionNotification(Account account,
                                             String accountNumber,
                                             BigDecimal amount,
                                             TransactionType transactionType) {

        logger.info("Preparing transaction notification: accountNumber={}, customerId={}, type={}, amount={}",
                accountNumber,
                account.getCustomerId(),
                transactionType,
                amount);

        try {
            // ✅ Build event
            TransactionNotificationEvent event = buildTransactionNotificationEvent(
                    account, accountNumber, amount, transactionType
            );

            logger.debug("Notification event payload prepared: {}", event);

            // ✅ Send to Kafka with callback
            kafkaTemplate.send(KafkaConstants.TRANSACTION_NOTIFICATION_TOPIC, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Failed to send notification event: accountNumber={}, type={}, error={}",
                                    accountNumber, transactionType, ex.getMessage(), ex);
                        } else {
                            logger.info("Notification event sent successfully: accountNumber={}, partition={}, offset={}",
                                    accountNumber,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });

        } catch (Exception e) {
            // ⚠️ Do NOT break main flow
            logger.error("Error while preparing/sending notification: accountNumber={}, type={}",
                    accountNumber, transactionType, e);
        }
    }


    private TransactionNotificationEvent buildTransactionNotificationEvent(Account account,
                                                                           String accountNumber,
                                                                           BigDecimal amount,
                                                                           TransactionType type) {

        TransactionNotificationEvent event = new TransactionNotificationEvent();

        event.setAccountNumber(accountNumber);
        event.setCustomerId(account.getCustomerId());
        event.setTransactionType(type);
        event.setAmount(amount);
        event.setSourceService(SourceService.ACCOUNT_SERVICE);

        // ── Notification-specific fields ──
        event.setReferenceId(accountNumber);

        // Map TransactionType to NotificationType string and set message + subject
        switch (type) {
            case DEPOSIT -> {
                event.setMessage(
                        String.format("Amount %s credited to your account %s", amount, accountNumber)
                );
                event.setNotificationType(NotificationType.DEPOSIT);
                event.setSubject("Deposit Successful");
            }
            case WITHDRAW -> {
                event.setMessage(
                        String.format("Amount %s debited from your account %s", amount, accountNumber)
                );
                event.setNotificationType(NotificationType.WITHDRAWAL);
                event.setSubject("Withdrawal Successful");
            }
            case TRANSFER -> {
                event.setMessage(
                        String.format("Amount %s transferred from your account %s", amount, accountNumber)
                );
                event.setNotificationType(NotificationType.TRANSFER);
                event.setSubject("Transfer Successful");
            }
            default -> {
                event.setMessage("Transaction occurred");
                event.setNotificationType(NotificationType.DEPOSIT);
                event.setSubject("Transaction Notification");
            }
        }

        // Build JSON metadata with transaction-specific details
        event.setMetadata(String.format(
                "{\"transactionType\":\"%s\",\"amount\":\"%s\",\"accountNumber\":\"%s\"}",
                type, amount, accountNumber
        ));

        return event;
    }
}
