package com.bank.dto.accounts;

import com.bank.ENUM.accounts.AccountStatus;
import com.bank.ENUM.accounts.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountResponseDTO {

    private UUID customerId;

    @NotNull
    private String accountNumber;

    @NotNull private AccountType accountType;

    @NotNull private AccountStatus status;

    @NotNull private BigDecimal balance;

}
