package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Kafka event published when a credit card's status changes
 * (approved, rejected, blocked, unblocked, closed).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardStatusEvent {
    private UUID cardId;
    private UUID customerId;
    private String status;
    private String message;
}
