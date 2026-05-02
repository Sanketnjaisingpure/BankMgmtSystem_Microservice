package com.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO exposing credit card details to the client.
 *
 * @param cardId             unique identifier for the card
 * @param cardNumber         masked card number (e.g., "****-****-****-1234")
 * @param customerId         the owning customer's UUID
 * @param accountNumber      the linked bank account number
 * @param cardHolderName     name on the card
 * @param creditLimit        sanctioned credit limit
 * @param availableLimit     remaining available credit
 * @param outstandingBalance current unpaid balance
 * @param minimumDueAmount   minimum payment due
 * @param interestRate       annual percentage rate
 * @param expiryDate         card expiry date
 * @param status             current card status (PENDING, ACTIVE, BLOCKED, etc.)
 */
public record CreditCardResponseDTO(
        UUID cardId,
        String cardNumber,
        UUID customerId,
        String accountNumber,
        String cardHolderName,
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        BigDecimal outstandingBalance,
        BigDecimal minimumDueAmount,
        Double interestRate,
        LocalDate expiryDate,
        String status
) {}
