package com.bank.dto;

import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.AccountType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


public record AccountResponseDTO (

     @NotNull String accountNumber,

     @NotNull AccountType accountType,

     @NotNull AccountStatus status,

     @NotNull BigDecimal balance)
    {
}
