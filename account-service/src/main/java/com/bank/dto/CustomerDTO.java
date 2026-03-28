package com.bank.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerDTO {

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
