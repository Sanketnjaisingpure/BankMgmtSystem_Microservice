package com.bank.event;

import com.bank.ENUM.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionNotificationEvent {

    private String accountNumber;

    private UUID customerId;

    private String email;

    private String message;

    private TransactionType transactionType;

    private BigDecimal amount;

}
