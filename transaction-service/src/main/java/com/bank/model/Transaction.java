package com.bank.model;


import com.bank.ENUM.TransactionStatus;
import com.bank.ENUM.TransactionType;
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
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private UUID sourceAccountNumber;

    @NotNull
    private UUID destinationAccountNumber;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private String transactionDescription;

}
