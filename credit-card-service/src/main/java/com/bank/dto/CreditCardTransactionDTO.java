package com.bank.dto;

import java.math.BigDecimal;

/**
 * Request DTO for credit card charge and payment operations.
 *
 * @param amount      the transaction amount (must be > 0)
 * @param description a brief description of the transaction (e.g., "Amazon purchase", "Monthly payment")
 */
public record CreditCardTransactionDTO(
        BigDecimal amount,
        String description
) {}
