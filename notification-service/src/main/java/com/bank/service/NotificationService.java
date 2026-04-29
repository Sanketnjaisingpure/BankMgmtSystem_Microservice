package com.bank.service;

import com.bank.ENUM.ChannelType;
import com.bank.config.KafkaConstants;
import com.bank.event.AccountCreationEvent;
import com.bank.event.TransactionNotificationEvent;
import com.bank.model.Notification;
import com.bank.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = KafkaConstants.ACCOUNT_CREATION_TOPIC,
            groupId = KafkaConstants.ACCOUNT_CREATION_GROUP
    )
    public void sendAccountCreationNotification(AccountCreationEvent event,
                                                Acknowledgment ack) {

        logger.info("Received ACCOUNT_CREATION event: customerId={}", event.getCustomerId());

        try {
            processNotification(event.getCustomerId(), event.getMessage());

            ack.acknowledge();
            logger.info("ACK success: ACCOUNT_CREATION customerId={}", event.getCustomerId());

        } catch (Exception e) {
            logger.error("Failed to process ACCOUNT_CREATION event: customerId={}",
                    event.getCustomerId(), e);

            // ❗ No ACK → Kafka will retry automatically
        }
    }

    @KafkaListener(
            topics = KafkaConstants.TRANSACTION_NOTIFICATION_TOPIC,
            groupId = KafkaConstants.TRANSACTION_NOTIFICATION_GROUP
    )
    public void sendTransactionNotification(TransactionNotificationEvent event,
                                            Acknowledgment ack) {

        logger.info("Received TRANSACTION event: customerId={}, type={}, amount={}",
                event.getCustomerId(),
                event.getTransactionType(),
                event.getAmount());

        try {
            processNotification(event.getCustomerId(), event.getMessage());

            ack.acknowledge();
            logger.info("ACK success: TRANSACTION customerId={}", event.getCustomerId());

        } catch (Exception e) {
            logger.error("Failed to process TRANSACTION event: customerId={}",
                    event.getCustomerId(), e);

            // ❗ No ACK → retry will happen
        }
    }

    @KafkaListener(
            topics = KafkaConstants.LOAN_APPLICATION_TOPIC,
            groupId = KafkaConstants.LOAN_APPLICATION_GROUP
    )
    public void sendLoanApplicationNotification(com.bank.event.LoanApplicationEvent event,
                                                Acknowledgment ack) {

        logger.info("Received LOAN_APPLICATION event: customerId={}, amount={}",
                event.getCustomerId(), event.getLoanAmount());

        try {
            processNotification(event.getCustomerId(), event.getMessage());

            ack.acknowledge();
            logger.info("ACK success: LOAN_APPLICATION customerId={}", event.getCustomerId());

        } catch (Exception e) {
            logger.error("Failed to process LOAN_APPLICATION event: customerId={}",
                    event.getCustomerId(), e);

            // ❗ No ACK → retry will happen
        }
    }

    @KafkaListener(
            topics = KafkaConstants.LOAN_STATUS_TOPIC,
            groupId = KafkaConstants.LOAN_STATUS_GROUP
    )
    public void sendLoanStatusNotification(com.bank.event.LoanStatusEvent event,
                                           Acknowledgment ack) {

        logger.info("Received LOAN_STATUS event: loanId={}, customerId={}, status={}",
                event.getLoanId(), event.getCustomerId(), event.getStatus());

        try {
            processNotification(event.getCustomerId(), event.getMessage());

            ack.acknowledge();
            logger.info("ACK success: LOAN_STATUS loanId={}", event.getLoanId());

        } catch (Exception e) {
            logger.error("Failed to process LOAN_STATUS event: loanId={}",
                    event.getLoanId(), e);

            // ❗ No ACK → retry will happen
        }
    }

    @KafkaListener(
            topics = KafkaConstants.LOAN_DISBURSEMENT_TOPIC,
            groupId = KafkaConstants.LOAN_DISBURSEMENT_GROUP
    )
    public void sendLoanDisbursementNotification(com.bank.event.LoanDisbursementEvent event,
                                                 Acknowledgment ack) {

        logger.info("Received LOAN_DISBURSEMENT event: loanId={}, customerId={}, amount={}",
                event.getLoanId(), event.getCustomerId(), event.getAmount());

        try {
            processNotification(event.getCustomerId(), event.getMessage());

            ack.acknowledge();
            logger.info("ACK success: LOAN_DISBURSEMENT loanId={}", event.getLoanId());

        } catch (Exception e) {
            logger.error("Failed to process LOAN_DISBURSEMENT event: loanId={}",
                    event.getLoanId(), e);

            // ❗ No ACK → retry will happen
        }
    }

    private void processNotification(UUID customerId, String message) {

        logger.info("Processing notification: customerId={}", customerId);

        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setMessage(message);
        notification.setChannelType(ChannelType.EMAIL);
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus("SUCCESS");

        notificationRepository.save(notification);

        logger.info("Notification saved successfully: customerId={}", customerId);
    }

}
