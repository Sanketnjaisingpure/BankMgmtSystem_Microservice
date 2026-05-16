package com.bank.ENUM;

/**
 * Classifies the specific event/action that triggered the notification.
 * Covers all business events across account, loan, and credit card services.
 * Add new values here as new event types are introduced — no entity changes needed.
 */
public enum NotificationType {

    // ── Account Service Events ──
    ACCOUNT_CREATED,
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,

    // ── Loan Service Events ──
    LOAN_APPLIED,
    LOAN_APPROVED,
    LOAN_REJECTED,
    LOAN_DISBURSED,

    // ── Credit Card Service Events ──
    CREDIT_CARD_APPLIED,
    CREDIT_CARD_APPROVED,
    CREDIT_CARD_REJECTED,
    CREDIT_CARD_ACTIVATED,
    CREDIT_CARD_BLOCKED,
    CREDIT_CARD_UNBLOCKED,
    CREDIT_CARD_CLOSED,
    CREDIT_CARD_CHARGE,
    CREDIT_CARD_PAYMENT,

    // ── Bank Service Events ──
    BANK_REGISTERED
}
