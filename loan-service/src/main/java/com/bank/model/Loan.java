package com.bank.model;


import com.bank.dto.LoanStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

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

    private Double loanAmount;

    private Double interestRate;

    private Double emiAmount;

    private LoanStatus loanStatus;

    private int tenureMonths;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
