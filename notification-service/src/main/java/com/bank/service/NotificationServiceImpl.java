package com.bank.service;

import com.bank.ENUM.ChannelType;
import com.bank.ENUM.NotificationStatus;
import com.bank.ENUM.NotificationType;
import com.bank.ENUM.SourceService;
import com.bank.config.KafkaConstants;
import com.bank.event.*;
import com.bank.model.Notification;
import com.bank.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link NotificationService}.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Consumes Kafka events from all microservices and persists generic notifications</li>
 *   <li>Provides query and analytics methods for notification management</li>
 * </ul>
 *
 * <p>All Kafka listeners follow the same pattern:</p>
 * <ol>
 *   <li>Log the received event</li>
 *   <li>Build a generic {@link Notification} using the builder pattern</li>
 *   <li>Persist via repository</li>
 *   <li>Manually acknowledge the Kafka offset on success</li>
 *   <li>On failure, skip ACK so Kafka retries automatically</li>
 * </ol>
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  KAFKA LISTENERS — Account Service Events
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Handles account creation events from account-service.
     * Reads notification-specific fields (sourceService, notificationType, subject, referenceId)
     * directly from the event — the producer service is the source of truth.
     */
    @KafkaListener(
            topics = KafkaConstants.ACCOUNT_CREATION_TOPIC,
            groupId = KafkaConstants.ACCOUNT_CREATION_GROUP
    )
    public void handleAccountCreationEvent(AccountCreationEvent event, Acknowledgment ack) {

        logger.info("Received ACCOUNT_CREATION event: customerId={}, accountNumber={}, notificationType={}",
                event.getCustomerId(), event.getAccountNumber(), event.getNotificationType());

        try {
            Notification notification = Notification.builder()
                    .customerId(event.getCustomerId())
                    .sourceService(event.getSourceService())
                    .notificationType(event.getNotificationType())
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .createdAt(event.getCreatedAt())
                    .metadata("metadata")
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: ACCOUNT_CREATION notification saved for customerId={}, referenceId={}",
                    event.getCustomerId(), event.getReferenceId());

        } catch (Exception e) {
            logger.error("Failed to process ACCOUNT_CREATION event: customerId={}", event.getCustomerId(), e);
            // ❗ No ACK → Kafka will retry automatically
        }
    }

    /**
     * Handles transaction notification events from account-service (deposit, withdrawal, transfer).
     * Reads all notification-specific fields directly from the event —
     * the producer service builds the notificationType, subject, metadata, etc.
     */
    @KafkaListener(
            topics = KafkaConstants.TRANSACTION_NOTIFICATION_TOPIC,
            groupId = KafkaConstants.TRANSACTION_NOTIFICATION_GROUP
    )
    public void handleTransactionNotificationEvent(TransactionNotificationEvent event, Acknowledgment ack) {

        logger.info("Received TRANSACTION event: customerId={}, transactionType={}, notificationType={}, amount={}",
                event.getCustomerId(), event.getTransactionType(), event.getNotificationType(), event.getAmount());

        try {
            Notification notification = Notification.builder()
                    .customerId(event.getCustomerId())
                    .sourceService(event.getSourceService())
                    .notificationType(event.getNotificationType())
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .metadata(event.getMetadata())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: TRANSACTION notification saved for customerId={}, type={}, referenceId={}",
                    event.getCustomerId(), event.getNotificationType(), event.getReferenceId());

        } catch (Exception e) {
            logger.error("Failed to process TRANSACTION event: customerId={}, type={}",
                    event.getCustomerId(), event.getTransactionType(), e);
            // ❗ No ACK → retry will happen
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  KAFKA LISTENERS — Loan Service Events
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Handles loan application events from loan-service.
     * Reads all notification-specific fields directly from the event.
     */
    @KafkaListener(
            topics = KafkaConstants.LOAN_APPLICATION_TOPIC,
            groupId = KafkaConstants.LOAN_APPLICATION_GROUP
    )
    public void handleLoanApplicationEvent(LoanApplicationEvent event, Acknowledgment ack) {

        logger.info("Received LOAN_APPLICATION event: customerId={}, notificationType={}, referenceId={}",
                event.getCustomerId(), event.getNotificationType(), event.getReferenceId());

        try {
            Notification notification = Notification.builder()
                    .customerId(event.getCustomerId())
                    .sourceService(SourceService.valueOf(event.getSourceService()))
                    .notificationType(NotificationType.valueOf(event.getNotificationType()))
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .metadata(event.getMetadata())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: LOAN_APPLICATION notification saved for customerId={}, referenceId={}",
                    event.getCustomerId(), event.getReferenceId());

        } catch (Exception e) {
            logger.error("Failed to process LOAN_APPLICATION event: customerId={}", event.getCustomerId(), e);
            // ❗ No ACK → retry will happen
        }
    }

    /**
     * Handles loan status change events (approved, rejected) from loan-service.
     * Reads all notification-specific fields directly from the event.
     */
    @KafkaListener(
            topics = KafkaConstants.LOAN_STATUS_TOPIC,
            groupId = KafkaConstants.LOAN_STATUS_GROUP
    )
    public void handleLoanStatusEvent(LoanStatusEvent event, Acknowledgment ack) {

        logger.info("Received LOAN_STATUS event: loanId={}, customerId={}, status={}, notificationType={}",
                event.getLoanId(), event.getCustomerId(), event.getStatus(), event.getNotificationType());

        try {
            Notification notification = Notification.builder()
                    .customerId(event.getCustomerId())
                    .sourceService(SourceService.valueOf(event.getSourceService()))
                    .notificationType(NotificationType.valueOf(event.getNotificationType()))
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .metadata(event.getMetadata())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: LOAN_STATUS notification saved for loanId={}, type={}",
                    event.getLoanId(), event.getNotificationType());

        } catch (Exception e) {
            logger.error("Failed to process LOAN_STATUS event: loanId={}", event.getLoanId(), e);
            // ❗ No ACK → retry will happen
        }
    }

    /**
     * Handles loan disbursement events from loan-service.
     * Reads all notification-specific fields directly from the event.
     */
    @KafkaListener(
            topics = KafkaConstants.LOAN_DISBURSEMENT_TOPIC,
            groupId = KafkaConstants.LOAN_DISBURSEMENT_GROUP
    )
    public void handleLoanDisbursementEvent(LoanDisbursementEvent event, Acknowledgment ack) {

        logger.info("Received LOAN_DISBURSEMENT event: customerId={}, notificationType={}, referenceId={}",
                event.getCustomerId(), event.getNotificationType(), event.getReferenceId());

        try {
            Notification notification = Notification.builder()
                    .customerId(event.getCustomerId())
                    .sourceService(SourceService.valueOf(event.getSourceService()))
                    .notificationType(NotificationType.valueOf(event.getNotificationType()))
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .metadata(event.getMetadata())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: LOAN_DISBURSEMENT notification saved for customerId={}, referenceId={}",
                    event.getCustomerId(), event.getReferenceId());

        } catch (Exception e) {
            logger.error("Failed to process LOAN_DISBURSEMENT event: customerId={}",
                    event.getCustomerId(), e);
            // ❗ No ACK → retry will happen
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  KAFKA LISTENERS — Credit Card Service Events
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Handles credit card application events from credit-card-service.
     * Reads all notification-specific fields directly from the event.
     */
    @KafkaListener(
            topics = KafkaConstants.CREDIT_CARD_APPLICATION_TOPIC,
            groupId = KafkaConstants.CREDIT_CARD_APPLICATION_GROUP
    )
    public void handleCreditCardApplicationEvent(CreditCardApplicationEvent event, Acknowledgment ack) {

        logger.info("Received CC_APPLICATION event: customerId={}, notificationType={}, referenceId={}",
                event.getCustomerId(), event.getNotificationType(), event.getReferenceId());

        try {
            Notification notification = Notification.builder()
                    .customerId(event.getCustomerId())
                    .sourceService(SourceService.valueOf(event.getSourceService()))
                    .notificationType(NotificationType.valueOf(event.getNotificationType()))
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .metadata(event.getMetadata())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: CC_APPLICATION notification saved for customerId={}, referenceId={}",
                    event.getCustomerId(), event.getReferenceId());

        } catch (Exception e) {
            logger.error("Failed to process CC_APPLICATION event: customerId={}", event.getCustomerId(), e);
            // ❗ No ACK → retry will happen
        }
    }

    /**
     * Handles credit card status change events from credit-card-service
     * (approved, rejected, activated, blocked, unblocked, closed).
     * Reads all notification-specific fields directly from the event.
     */
    @KafkaListener(
            topics = KafkaConstants.CREDIT_CARD_STATUS_TOPIC,
            groupId = KafkaConstants.CREDIT_CARD_STATUS_GROUP
    )
    public void handleCreditCardStatusEvent(CreditCardStatusEvent event, Acknowledgment ack) {

        logger.info("Received CC_STATUS event: cardId={}, customerId={}, status={}, notificationType={}",
                event.getCardId(), event.getCustomerId(), event.getStatus(), event.getNotificationType());

        try {
            Notification notification = Notification.builder()
                    .customerId(event.getCustomerId())
                    .sourceService(SourceService.valueOf(event.getSourceService()))
                    .notificationType(NotificationType.valueOf(event.getNotificationType()))
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .metadata(event.getMetadata())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: CC_STATUS notification saved for cardId={}, type={}",
                    event.getCardId(), event.getNotificationType());

        } catch (Exception e) {
            logger.error("Failed to process CC_STATUS event: cardId={}", event.getCardId(), e);
            // ❗ No ACK → retry will happen
        }
    }

    /**
     * Handles credit card transaction events (charge, payment) from credit-card-service.
     * Reads all notification-specific fields directly from the event.
     */
    @KafkaListener(
            topics = KafkaConstants.CREDIT_CARD_TRANSACTION_TOPIC,
            groupId = KafkaConstants.CREDIT_CARD_TRANSACTION_GROUP
    )
    public void handleCreditCardTransactionEvent(CreditCardTransactionEvent event, Acknowledgment ack) {

        logger.info("Received CC_TRANSACTION event: cardId={}, customerId={}, type={}, notificationType={}, amount={}",
                event.getCardId(), event.getCustomerId(), event.getTransactionType(),
                event.getNotificationType(), event.getAmount());

        try {
            Notification notification = Notification.builder()
                    .customerId(event.getCustomerId())
                    .sourceService(SourceService.valueOf(event.getSourceService()))
                    .notificationType(NotificationType.valueOf(event.getNotificationType()))
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getDescription())
                    .metadata(event.getMetadata())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: CC_TRANSACTION notification saved for cardId={}, type={}, amount={}",
                    event.getCardId(), event.getNotificationType(), event.getAmount());

        } catch (Exception e) {
            logger.error("Failed to process CC_TRANSACTION event: cardId={}, type={}",
                    event.getCardId(), event.getTransactionType(), e);
            // ❗ No ACK → retry will happen
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  KAFKA LISTENERS — Bank Service Events
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Handles bank registration events from bank-service.
     * Reads all notification-specific fields directly from the event.
     */
    @KafkaListener(
            topics = KafkaConstants.BANK_REGISTRATION_TOPIC,
            groupId = KafkaConstants.BANK_REGISTRATION_GROUP
    )
    public void handleBankRegistrationEvent(BankRegistrationEvent event, Acknowledgment ack) {

        logger.info("Received BANK_REGISTRATION event: bankId={}, bankName={}, notificationType={}",
                event.getBankId(), event.getBankName(), event.getNotificationType());

        try {
            Notification notification = Notification.builder()
                    .sourceService(SourceService.valueOf(event.getSourceService()))
                    .notificationType(NotificationType.valueOf(event.getNotificationType()))
                    .channelType(ChannelType.EMAIL)
                    .referenceId(event.getReferenceId())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .metadata(event.getMetadata())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            ack.acknowledge();
            logger.info("ACK success: BANK_REGISTRATION notification saved for bankId={}, referenceId={}",
                    event.getBankId(), event.getReferenceId());

        } catch (Exception e) {
            logger.error("Failed to process BANK_REGISTRATION event: bankId={}", event.getBankId(), e);
            // ❗ No ACK → Kafka will retry automatically
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  QUERY METHODS — Interface Implementation
    // ════════════════════════════════════════════════════════════════════════

    /** {@inheritDoc} */
    @Override
    public List<Notification> getNotificationsByCustomerId(UUID customerId) {
        logger.info("Fetching notifications for customerId={}", customerId);
        List<Notification> notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        logger.info("Found {} notifications for customerId={}", notifications.size(), customerId);
        return notifications;
    }

    /** {@inheritDoc} */
    @Override
    public List<Notification> getNotificationsByType(NotificationType notificationType) {
        logger.info("Fetching notifications by type={}", notificationType);
        List<Notification> notifications = notificationRepository.findByNotificationTypeOrderByCreatedAtDesc(notificationType);
        logger.info("Found {} notifications for type={}", notifications.size(), notificationType);
        return notifications;
    }

    /** {@inheritDoc} */
    @Override
    public List<Notification> getNotificationsBySourceService(SourceService sourceService) {
        logger.info("Fetching notifications from sourceService={}", sourceService);
        List<Notification> notifications = notificationRepository.findBySourceServiceOrderByCreatedAtDesc(sourceService);
        logger.info("Found {} notifications from sourceService={}", notifications.size(), sourceService);
        return notifications;
    }

    /** {@inheritDoc} */
    @Override
    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        logger.info("Fetching notifications with status={}", status);
        List<Notification> notifications = notificationRepository.findByStatusOrderByCreatedAtDesc(status);
        logger.info("Found {} notifications with status={}", notifications.size(), status);
        return notifications;
    }

    /** {@inheritDoc} */
    @Override
    public List<Notification> getNotificationsByCustomerAndType(UUID customerId, NotificationType notificationType) {
        logger.info("Fetching notifications for customerId={} with type={}", customerId, notificationType);
        List<Notification> notifications = notificationRepository
                .findByCustomerIdAndNotificationTypeOrderByCreatedAtDesc(customerId, notificationType);
        logger.info("Found {} notifications for customerId={} with type={}", notifications.size(), customerId, notificationType);
        return notifications;
    }

    /** {@inheritDoc} */
    @Override
    public List<Notification> getNotificationsByCustomerAndSource(UUID customerId, SourceService sourceService) {
        logger.info("Fetching notifications for customerId={} from source={}", customerId, sourceService);
        List<Notification> notifications = notificationRepository
                .findByCustomerIdAndSourceServiceOrderByCreatedAtDesc(customerId, sourceService);
        logger.info("Found {} notifications for customerId={} from source={}", notifications.size(), customerId, sourceService);
        return notifications;
    }

    /** {@inheritDoc} */
    @Override
    public List<Notification> getNotificationsByReferenceId(String referenceId) {
        logger.info("Fetching notifications for referenceId={}", referenceId);
        List<Notification> notifications = notificationRepository.findByReferenceIdOrderByCreatedAtDesc(referenceId);
        logger.info("Found {} notifications for referenceId={}", notifications.size(), referenceId);
        return notifications;
    }

    /** {@inheritDoc} */
    @Override
    public List<Notification> getNotificationsByDateRange(LocalDateTime start, LocalDateTime end) {
        logger.info("Fetching notifications between {} and {}", start, end);
        List<Notification> notifications = notificationRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        logger.info("Found {} notifications in the date range", notifications.size());
        return notifications;
    }

    /** {@inheritDoc} */
    @Override
    public long countByStatus(NotificationStatus status) {
        long count = notificationRepository.countByStatus(status);
        logger.info("Count of notifications with status={}: {}", status, count);
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public long countBySourceService(SourceService sourceService) {
        long count = notificationRepository.countBySourceService(sourceService);
        logger.info("Count of notifications from sourceService={}: {}", sourceService, count);
        return count;
    }

}
