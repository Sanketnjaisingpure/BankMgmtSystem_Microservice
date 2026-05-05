package com.bank.dto;

import com.bank.ENUM.AccountType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a bank account.
 *
 * <p>{@code bankId} is optional — pass it when the account is being opened
 * under a specific registered bank (from bank-service). Leave {@code null}
 * for standalone account creation.
 */
public record AccountRequestDTO(

    @NotNull
    UUID customerId,

    @NotNull
    AccountType accountType,

    @NotNull
    String ifscCode,

    @NotNull
    String branchName,

    @NotNull
    BigDecimal balance,

    /** Optional: UUID of the bank (from bank-service) this account belongs to. */
    UUID bankId
) {}

