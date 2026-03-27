package com.bank.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCustomerDTO {

    @NotNull(message = "First name is required")
    @Size(min = 2, max = 30)
    private String firstName;

    @NotNull(message = "Last name is required")
    @Size(min = 2, max = 30)
    private String lastName;

    @Email(message = "Invalid email structure")
    private String email;

    @NotNull(message = "Password is required")
    @Size(min = 6, max = 30)
    private String passwordHash;

    @NotNull(message = "Mobile number is required")
    @Size(min = 10, max = 10)
    private String mobileNumber;
}
