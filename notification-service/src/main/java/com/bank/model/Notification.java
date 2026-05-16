package com.bank.model;

import com.bank.ENUM.ChannelType;
import com.bank.ENUM.NotificationStatus;
import com.bank.ENUM.NotificationType;
import com.bank.ENUM.SourceService;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generic notification entity that stores notifications from all microservices.
 *
 * <p>Design goals:</p>
 * <ul>
 *   <li>Single entity handles notifications from account, loan, credit-card, and transaction services</li>
 *   <li>The {@code metadata} JSON column stores event-specific data (amounts, balances, etc.)
 *       so the schema never needs to change when new event types are added</li>
 *   <li>{@code referenceId} links back to the source entity (account number, loan ID, card ID)</li>
 * </ul>
 */
@Entity
@Table(name = "notification"
//        , indexes = {
//        @Index(name = "idx_notification_customer", columnList = "customerId"),
//        @Index(name = "idx_notification_type", columnList = "notificationType"),
//        @Index(name = "idx_notification_source", columnList = "sourceService"),
//        @Index(name = "idx_notification_status", columnList = "status")}
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;

    /** The customer who receives this notification (null for system-level events like bank registration) */
    @Column(nullable = true)
    private UUID customerId;

    /** Which microservice triggered this notification */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceService sourceService;

    /** The specific event/action type (e.g., ACCOUNT_CREATED, DEPOSIT, LOAN_APPROVED) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    /** Delivery channel — EMAIL, SMS, or PUSH_NOTIFICATION */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channelType;

    /**
     * Reference back to the source entity (account number, loan ID, card ID).
     * Stored as String to accommodate both UUID-based and String-based identifiers.
     */
    private String referenceId;

    /** Short summary/title for display purposes (e.g., "Account Created", "Loan Approved") */
    private String subject;

    /** Full notification message body */
    @Column(columnDefinition = "TEXT")
    private String message;

    /**
     * JSON-encoded map of event-specific data (e.g., amount, balance, transactionType).
     * This makes the entity truly extensible — new event data goes here without schema changes.
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /** Delivery status of this notification */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    /** Number of delivery retry attempts */
    @Column(name = "retry_count")
    private int retryCount = 0;

    /** Timestamp when the notification was created/queued */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the notification was actually sent/delivered (null if not yet sent) */
    private LocalDateTime sentAt;

    /**
     * Automatically set createdAt before persisting if not already set.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
