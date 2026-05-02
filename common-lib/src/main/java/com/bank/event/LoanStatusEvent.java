package com.bank.event;

import com.bank.ENUM.LoanStatus;
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
    private LoanStatus status;
    private String message;
}
