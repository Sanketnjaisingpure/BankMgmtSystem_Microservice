package com.bank.event;

import com.bank.ENUM.NotificationType;
import com.bank.ENUM.SourceService;
import com.bank.ENUM.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kafka event published for deposit, withdrawal, and transfer transaction notifications.
 * Carries all fields needed by the notification service to build a generic Notification entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionNotificationEvent {

    private String accountNumber;

    private UUID customerId;

    private String email;

    private String message;

    private TransactionType transactionType;

    private BigDecimal amount;

    // ── Notification-specific fields ──

    /** Which service triggered this notification (e.g., "ACCOUNT_SERVICE") */
    private SourceService sourceService;

    /** The type of notification (e.g., "DEPOSIT", "WITHDRAWAL", "TRANSFER") */
    private NotificationType notificationType;

    /** Short summary/title for the notification */
    private String subject;

    /** Reference ID linking back to the source entity */
    private String referenceId;

    /** JSON-encoded metadata with transaction-specific details */
    private String metadata;
}
