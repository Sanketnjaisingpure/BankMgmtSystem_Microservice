package com.bank.dto;

import lombok.Data;

@Data
public class UpdateCustomerDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
}
