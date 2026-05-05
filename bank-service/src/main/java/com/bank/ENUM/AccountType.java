package com.bank.ENUM;

/**
 * Mirrors AccountType from account-service.
 * Used for Feign response deserialization in bank-service.
 */
public enum AccountType {
    SAVINGS, CURRENT, FIXED_DEPOSIT
}
