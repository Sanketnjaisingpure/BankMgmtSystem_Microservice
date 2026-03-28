package com.bank.model;

import com.bank.ENUM.AccountStatus;
import com.bank.ENUM.AccountType;
import jakarta.persistence.*;
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
    private UUID customerId;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance;

    private String ifscCode;

    private String branchName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
