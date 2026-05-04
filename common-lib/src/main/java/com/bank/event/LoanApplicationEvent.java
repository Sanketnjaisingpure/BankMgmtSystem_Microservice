package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Kafka event published when a new loan application is submitted.
 * Carries all fields needed by the notification service to build a generic Notification entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationEvent {

    private UUID customerId;

    private String accountNumber;

    private String email;

    private String message;

    // ── Notification-specific fields ──

    /** Which service triggered this notification (e.g., "LOAN_SERVICE") */
    private String sourceService;

    /** The type of notification (e.g., "LOAN_APPLIED") */
    private String notificationType;

    /** Short summary/title for the notification */
    private String subject;

    /** Reference ID linking back to the source entity (loanId or accountNumber) */
    private String referenceId;

    /** JSON-encoded metadata with loan-specific details */
    private String metadata;
}
