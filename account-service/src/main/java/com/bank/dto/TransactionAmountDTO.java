package com.bank.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionAmountDTO {

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;
}
