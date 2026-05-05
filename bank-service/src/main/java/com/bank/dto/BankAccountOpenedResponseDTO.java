package com.bank.dto;

import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO returned after successfully opening a new account via bank-service.
 * Wraps the account details returned by account-service together with the originating bankId.
 */
public record BankAccountOpenedResponseDTO(
        UUID bankId,
        String bankName,
        String bankCode,
        UUID customerId,
        String accountNumber,
        AccountType accountType,
        AccountStatus accountStatus,
        BigDecimal balance,
        String ifscCode,
        String branchName
) {}
