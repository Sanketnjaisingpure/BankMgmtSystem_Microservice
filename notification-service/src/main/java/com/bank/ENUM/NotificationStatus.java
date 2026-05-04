package com.bank.ENUM;

/**
 * Represents the delivery status of a notification.
 * Replaces raw String status for type safety and consistency.
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    RETRYING
}
