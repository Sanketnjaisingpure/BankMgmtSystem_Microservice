package com.bank.model;

import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID accountId;

    @Column(nullable = false)
    @NotNull
    private UUID customerId;

    /**
     * Reference to the bank (from bank-service) under which this account was opened.
     * Mandatory — every account must be linked to a registered ACTIVE bank.
     * Not a DB foreign key since Bank lives in a separate database (bank_db).
     */
    @Column(nullable = false)
    @NotNull
    private UUID bankId;

    @Column(unique = true, nullable = false)
    @NotNull
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AccountStatus status;

    @Column(precision = 15, scale = 2)
    @NotNull
    private BigDecimal balance;

    @NotNull
    private String ifscCode;

    @NotNull
    private String branchName;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;
}
