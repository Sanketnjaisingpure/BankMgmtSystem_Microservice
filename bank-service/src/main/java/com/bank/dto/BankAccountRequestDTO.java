package com.bank.dto;

import com.bank.ENUM.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body for opening a new bank account via the bank-service.
 * Bank-service will enrich this with IFSC code and branch name from the Bank entity
 * before forwarding to account-service.
 *
 * @param customerId  The UUID of the customer opening the account
 * @param accountType SAVINGS or CURRENT
 * @param balance     Initial deposit amount (must be greater than 500)
 */
public record BankAccountRequestDTO(

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Account type is required")
        AccountType accountType,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "500.01", message = "Initial balance must be greater than 500")
        BigDecimal balance
) {}
