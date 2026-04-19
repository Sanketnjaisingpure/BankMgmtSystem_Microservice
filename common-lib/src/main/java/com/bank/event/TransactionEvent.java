package com.bank.event;

import com.bank.ENUM.TransactionStatus;
import com.bank.ENUM.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionEvent {


    private TransactionType transactionType;

    private TransactionStatus transactionStatus;

    private BigDecimal amount;

    private String sourceAccountNumber;


    private String destinationAccountNumber;

    private String transactionDescription;
}
