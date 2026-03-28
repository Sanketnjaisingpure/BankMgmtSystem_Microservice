package com.bank.dto;


import com.bank.ENUM.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountRequestDTO {
    @NotNull
    private UUID customerId;

    @NotNull
    private AccountType accountType;

    @NotNull
    private String ifscCode;

    @NotNull
    private String branchName;

    @NotNull
    private BigDecimal balance;



}
