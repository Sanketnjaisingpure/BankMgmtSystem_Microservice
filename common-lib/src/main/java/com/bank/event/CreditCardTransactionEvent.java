package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kafka event published when a credit card charge or payment is made.
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
}
