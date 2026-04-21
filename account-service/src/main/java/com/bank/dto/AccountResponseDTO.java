package com.bank.dto;

import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.AccountType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AccountResponseDTO {

     @NotNull private String accountNumber;

     @NotNull private AccountType accountType;

     @NotNull private AccountStatus status;

     @NotNull private BigDecimal balance;

}
