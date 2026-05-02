package com.bank.dto;

import java.util.UUID;

/**
 * Request DTO for applying for a new credit card.
 *
 * @param customerId    the UUID of the customer applying
 * @param accountNumber the bank account to link for payments
 */
public record CreditCardRequestDTO(
        UUID customerId,
        String accountNumber
) {}
