package com.bank.ENUM;

/**
 * Mirrors AccountStatus from account-service.
 * Used for Feign response deserialization in bank-service.
 */
public enum AccountStatus {
    ACTIVE, INACTIVE, BLOCKED, CLOSED
}
