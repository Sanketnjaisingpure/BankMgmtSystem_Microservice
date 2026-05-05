package com.bank.dto;

import java.util.UUID;

/**
 * Minimal customer DTO used by bank-service to validate customer existence
 * via a Feign call to customer-service.
 */
public class CustomerDTO {

    private UUID customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;

    public CustomerDTO() {}

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
}
