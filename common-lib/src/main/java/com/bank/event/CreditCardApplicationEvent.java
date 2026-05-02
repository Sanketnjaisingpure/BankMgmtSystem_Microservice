package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kafka event published when a new credit card application is submitted.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardApplicationEvent {
    private UUID cardId;
    private UUID customerId;
    private String email;
    private BigDecimal creditLimit;
    private String message;
}
