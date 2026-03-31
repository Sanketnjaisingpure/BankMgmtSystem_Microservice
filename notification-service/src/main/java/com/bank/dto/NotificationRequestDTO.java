package com.bank.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequestDTO {
    private UUID customerId;
    private String message;
    private String email;
    private String phone;
}