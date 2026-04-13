package com.bank.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionRecordRequestDTO {

    @NotNull
    private UUID sourceAccountNumber;

    @NotNull
    private UUID destinationAccountNumber;

    @NotNull
    private BigDecimal amount;

    /**
     * Must match {@code com.bank.ENUM.TransactionType} enum values:
     * DEPOSIT, WITHDRAW, TRANSFER
     */
    @NotNull
    private String transactionType;

    private String transactionDescription;
}

