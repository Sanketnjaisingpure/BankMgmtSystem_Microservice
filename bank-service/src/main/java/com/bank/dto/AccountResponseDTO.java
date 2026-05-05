package com.bank.dto;

import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.AccountType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mirror of AccountResponseDTO returned by account-service.
 * Field names match the account-service JSON serialization exactly.
 * Note: account-service uses "status" (not "accountStatus").
 */
@Data
public class AccountResponseDTO {

    private UUID customerId;
    private String accountNumber;
    private AccountType accountType;

    /** Field name is "status" in account-service — matches JSON key exactly. */
    private AccountStatus status;

    private BigDecimal balance;
}
