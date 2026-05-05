package com.bank.dto;

import com.bank.ENUM.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO forwarded to account-service when creating an account.
 * Field order matches {@code AccountRequestDTO} in account-service exactly:
 * customerId → accountType → ifscCode → branchName → balance.
 *
 * @param customerId  Customer UUID
 * @param accountType SAVINGS or CURRENT
 * @param ifscCode    Auto-filled from the Bank entity by BankService
 * @param branchName  Auto-filled from the Bank entity by BankService
 * @param balance     Opening deposit
 */
public record AccountRequestDTO(
        UUID customerId,
        AccountType accountType,
        String ifscCode,
        String branchName,
        BigDecimal balance
) {}
