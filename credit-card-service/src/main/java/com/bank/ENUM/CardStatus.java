package com.bank.ENUM;

/**
 * Represents the lifecycle states of a credit card.
 *
 * <pre>
 * State transitions:
 *   PENDING  → APPROVED | REJECTED
 *   APPROVED → ACTIVE
 *   ACTIVE   → BLOCKED | CLOSED
 *   BLOCKED  → ACTIVE  | CLOSED
 * </pre>
 */
public enum CardStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ACTIVE,
    BLOCKED,
    CLOSED;
}
