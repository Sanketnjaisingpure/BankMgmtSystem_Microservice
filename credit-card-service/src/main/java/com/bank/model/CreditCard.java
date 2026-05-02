package com.bank.model;

import com.bank.ENUM.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a credit card.
 *
 * <p>Tracks the card's financial state (credit limit, outstanding balance,
 * available limit) and lifecycle status.</p>
 */
@Entity
@Table(name = "credit_cards")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreditCard {

    @Id
    private UUID cardId;

    /** Masked card number — only last 4 digits visible (e.g., "****-****-****-1234") */
    @Column(unique = true, nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private UUID customerId;

    /** Linked bank account for payment debits */
    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String cardHolderName;

    /** Maximum credit limit sanctioned for this card */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit;

    /** Remaining credit available for new charges (creditLimit - outstandingBalance) */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal availableLimit;

    /** Current unpaid balance on the card */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    /** Minimum payment due for the current billing cycle */
    @Column(precision = 15, scale = 2)
    private BigDecimal minimumDueAmount;

    /** Annual card fee */
    @Column(precision = 10, scale = 2)
    private BigDecimal annualFee;

    /** Annual Percentage Rate (APR) for outstanding balance */
    private Double interestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus cardStatus;

    /** Card expiry date (typically 5 years from issue) */
    private LocalDate expiryDate;

    /** Day of month when the billing cycle closes (1–28) */
    private Integer billingCycleDay;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
