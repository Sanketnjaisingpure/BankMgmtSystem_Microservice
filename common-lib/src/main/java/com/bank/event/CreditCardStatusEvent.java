package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Kafka event published when a credit card's status changes
 * (approved, rejected, activated, blocked, unblocked, closed).
 * Carries all fields needed by the notification service to build a generic Notification entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardStatusEvent {

    private UUID cardId;

    private UUID customerId;

    private String status;

    private String message;

    // ── Notification-specific fields ──

    /** Which service triggered this notification (e.g., "CREDIT_CARD_SERVICE") */
    private String sourceService;

    /** The type of notification (e.g., "CREDIT_CARD_APPROVED", "CREDIT_CARD_BLOCKED") */
    private String notificationType;

    /** Short summary/title for the notification */
    private String subject;

    /** Reference ID linking back to the source entity (cardId) */
    private String referenceId;

    /** JSON-encoded metadata with card-specific details */
    private String metadata;
}
