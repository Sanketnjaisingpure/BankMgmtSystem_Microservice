package com.bank.event;

import lombok.Data;

import java.util.UUID;

@Data
public class LoanApplicationEvent {

    private UUID customerId;

    private String accountNumber;

    private String  email;

    private String message;
}
