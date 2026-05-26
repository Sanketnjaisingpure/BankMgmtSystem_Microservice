package com.bank.dto;

import com.bank.ENUM.AccountType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a bank account.
 *
 * <p>{@code bankId} is required — every account must be opened under
 * a registered, ACTIVE bank (from bank-service).
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

    /** UUID of the bank (from bank-service) this account belongs to. */
    @NotNull(message = "bankId is required — provide the UUID of a registered ACTIVE bank")
    UUID bankId
) {}


