package com.bank.dto;

import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.AccountType;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountResponseDTO {

    private String accountNumber;

    private AccountType accountType;

    private AccountStatus status;

    private BigDecimal balance;
}
