package com.bank.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;


public record TransactionRecordRequestDTO (

    @NotNull String sourceAccountNumber,

     @NotNull String destinationAccountNumber,

     @NotNull BigDecimal amount){
}

