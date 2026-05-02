package com.bank.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanResponseDTO(
        UUID loanId,
        UUID customerId,
        String accountNumber,
        BigDecimal amount,
        BigDecimal emiAmount,
        Double interestRate,
        Integer tenureMonths,
        String status
) {}
