package com.bank.event;

import com.bank.ENUM.NotificationType;
import com.bank.ENUM.SourceService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event published when a new account is created.
 * Carries all fields needed by the notification service to build a generic Notification entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreationEvent {

    private String accountNumber;

    private UUID customerId;

    private String email;

    private String message;

    // ── Notification-specific fields ──

    /** Which service triggered this notification (e.g., "ACCOUNT_SERVICE") */
    private SourceService sourceService;

    /** The type of notification (e.g., "ACCOUNT_CREATED") */
    private NotificationType notificationType;

    /** Short summary/title for the notification */
    private String subject;

    /** Reference ID linking back to the source entity */
    private String referenceId;

    private LocalDateTime createdAt;
}
