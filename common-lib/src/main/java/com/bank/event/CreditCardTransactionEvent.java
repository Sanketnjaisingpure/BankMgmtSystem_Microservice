package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kafka event published when a credit card charge or payment is made.
 * Carries all fields needed by the notification service to build a generic Notification entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardTransactionEvent {

    private UUID cardId;

    private UUID customerId;

    /** CHARGE or PAYMENT */
    private String transactionType;

    private BigDecimal amount;

    private BigDecimal outstandingBalance;

    private BigDecimal availableLimit;

    private String description;

    // ── Notification-specific fields ──

    /** Which service triggered this notification (e.g., "CREDIT_CARD_SERVICE") */
    private String sourceService;

    /** The type of notification (e.g., "CREDIT_CARD_CHARGE", "CREDIT_CARD_PAYMENT") */
    private String notificationType;

    /** Short summary/title for the notification */
    private String subject;

    /** Reference ID linking back to the source entity (cardId) */
    private String referenceId;

    /** JSON-encoded metadata with transaction-specific details */
    private String metadata;
}
