package com.bank.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanStatusEvent {
    private UUID loanId;
    private UUID customerId;
    private String status;
    private String message;
}
