package com.bank.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionRecordRequestDTO {

    private UUID sourceAccountNumber;
    private UUID destinationAccountNumber;

    private BigDecimal amount;

    /**
     * Must match {@code com.bank.ENUM.TransactionType} values:
     * DEPOSIT, WITHDRAW, TRANSFER
     */
    private String transactionType;

    private String transactionDescription;
}

