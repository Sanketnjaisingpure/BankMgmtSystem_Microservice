package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Kafka event published when a new bank is registered.
 * Carries all fields needed by the notification service to build a generic Notification entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankRegistrationEvent {

    private UUID bankId;

    private String bankName;

    private String bankCode;

    private String bankStatus;

    private String message;

    // ── Notification-specific fields ──

    /** Which service triggered this notification (e.g., "BANK_SERVICE") */
    private String sourceService;

    /** The type of notification (e.g., "BANK_REGISTERED") */
    private String notificationType;

    /** Short summary/title for the notification */
    private String subject;

    /** Reference ID linking back to the source entity (bankId) */
    private String referenceId;

    /** JSON-encoded metadata with bank-specific details */
    private String metadata;
}
