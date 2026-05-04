package com.bank.ENUM;

/**
 * Identifies which microservice originated the notification.
 * Used for filtering, analytics, and tracing notifications back to their source.
 */
public enum SourceService {
    ACCOUNT_SERVICE,
    LOAN_SERVICE,
    CREDIT_CARD_SERVICE,
    TRANSACTION_SERVICE
}
