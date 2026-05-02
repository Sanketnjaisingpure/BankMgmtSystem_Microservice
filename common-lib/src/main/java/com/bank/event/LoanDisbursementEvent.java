package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanDisbursementEvent {
    private UUID customerId;
    private String accountNumber;
    private String  email;
    private String message;
}
