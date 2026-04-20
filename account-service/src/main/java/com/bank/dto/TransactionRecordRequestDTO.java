package com.bank.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;


public record TransactionRecordRequestDTO (

    @NotNull UUID sourceAccountNumber,

     @NotNull UUID destinationAccountNumber,

     @NotNull BigDecimal amount,

    /*
     * Must match {@code com.bank.ENUM.TransactionType} values:
     * DEPOSIT, WITHDRAW, TRANSFER
     */
     @NotNull String transactionType,

     @NotNull String transactionDescription){
}

