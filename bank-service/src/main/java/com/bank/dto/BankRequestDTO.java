package com.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for registering a new bank.
 *
 * @param bankName         Full legal name of the bank
 * @param bankCode         Unique short code (max 10 chars, e.g. "SBIN")
 * @param headquartersCity City of the bank's headquarters
 * @param ifscPrefix       First 4 characters of all IFSC codes for this bank
 * @param contactEmail     Official contact email
 * @param contactPhone     Official contact phone number
 */
public record BankRequestDTO(

        @NotBlank(message = "Bank name is required")
        String bankName,

        @NotBlank(message = "Bank code is required")
        @Size(max = 10, message = "Bank code must be at most 10 characters")
        String bankCode,

        @NotBlank(message = "Headquarters city is required")
        String headquartersCity,

        @NotBlank(message = "IFSC prefix is required")
        @Size(min = 4, max = 4, message = "IFSC prefix must be exactly 4 characters")
        String ifscPrefix,

        @NotBlank(message = "Contact email is required")
        @Email(message = "Invalid email format")
        String contactEmail,

        @NotBlank(message = "Contact phone is required")
        String contactPhone
) {}
