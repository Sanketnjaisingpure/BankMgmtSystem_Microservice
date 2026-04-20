package com.bank.dto;


import com.bank.ENUM.AccountType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;


public record AccountRequestDTO (
    @NotNull
     UUID customerId,

    @NotNull
     AccountType accountType,

    @NotNull
     String ifscCode,

    @NotNull
     String branchName,

    @NotNull
     BigDecimal balance
    )
{
}
