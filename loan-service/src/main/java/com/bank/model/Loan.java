package com.bank.model;


import com.bank.ENUM.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID loanId;


    private UUID customerId;

    private String accountNumber;

    @Column(precision = 15, scale = 2)
    private BigDecimal loanAmount;


    private Double interestRate;

    @Column(precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;

    private Integer tenureMonths;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
