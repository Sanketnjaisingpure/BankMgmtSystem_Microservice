package com.bank.event;

import lombok.Data;

import java.util.UUID;

@Data
public class AccountCreationEvent {
    private String accountNumber;

    private UUID customerId;

    private String email;

    private String phoneNumber;

    private String message;
}
