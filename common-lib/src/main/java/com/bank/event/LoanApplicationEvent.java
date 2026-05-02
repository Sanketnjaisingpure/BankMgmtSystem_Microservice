package com.bank.event;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LoanApplicationEvent {

    private UUID customerId;

    private String accountNumber;

    private BigDecimal loanAmount;

    private String message;
}
