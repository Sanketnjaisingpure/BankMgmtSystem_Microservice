package com.bank.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanRequestDTO(

         UUID customerId,

         String accountNumber,

         BigDecimal loanAmount,

         Integer tenureMonths   ) {
}
