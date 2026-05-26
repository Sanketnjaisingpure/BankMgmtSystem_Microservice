package com.bank.dto;

import com.bank.ENUM.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardResponseDTO {
    private String cardNumber;
    private UUID customerId;
    private String accountNumber;
    private String cardHolderName;
    private BigDecimal creditLimit;
    private BigDecimal availableLimit;
    private BigDecimal outstandingBalance;
    private BigDecimal minimumDueAmount;
    private Double interestRate;
    private LocalDate expiryDate;
    private CardStatus status;
}
