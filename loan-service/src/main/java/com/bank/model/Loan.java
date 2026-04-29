package com.bank.model;


import com.bank.ENUM.LoanStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Loan {

    @Id
    private UUID loanId;


    private UUID customerId;

    private String accountNumber;

    private BigDecimal loanAmount;

    private Double interestRate;

    private BigDecimal emiAmount;

    private LoanStatus loanStatus;

    private Integer tenureMonths;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
