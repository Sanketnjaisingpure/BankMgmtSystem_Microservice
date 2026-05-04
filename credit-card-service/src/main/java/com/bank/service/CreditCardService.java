package com.bank.service;

import com.bank.ENUM.CardStatus;
import com.bank.config.KafkaConstants;
import com.bank.dto.*;
import com.bank.dto.accounts.AccountResponseDTO;
import com.bank.event.CreditCardApplicationEvent;
import com.bank.event.CreditCardStatusEvent;
import com.bank.event.CreditCardTransactionEvent;
import com.bank.exception.ResourceNotFoundException;
import com.bank.feign.AccountFeignService;
import com.bank.feign.CustomerFeignService;
import com.bank.model.CreditCard;
import com.bank.repository.CreditCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Core service managing the full credit card lifecycle:
 * Apply → Approve/Reject → Activate → Charge/Payment → Block/Unblock → Close.
 *
 * <p><b>State Machine:</b></p>
 * <pre>
 *   PENDING → APPROVED → ACTIVE → BLOCKED → ACTIVE (unblock)
 *      │                    │         │
 *      └→ REJECTED          └→ CLOSED └→ CLOSED
 * </pre>
 */
@Service
public class CreditCardService {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardService.class);

    private final CreditCardRepository creditCardRepository;
    private final CustomerFeignService customerFeignService;
    private final AccountFeignService accountFeignService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${credit-card.default.credit-limit:50000}")
    private BigDecimal defaultCreditLimit;

    @Value("${credit-card.default.interest-rate:36.0}")
    private Double defaultInterestRate;

    @Value("${credit-card.default.annual-fee:499}")
    private BigDecimal defaultAnnualFee;

    @Value("${credit-card.default.billing-cycle-day:1}")
    private Integer defaultBillingCycleDay;

    public CreditCardService(CreditCardRepository creditCardRepository,
                             CustomerFeignService customerFeignService,
                             AccountFeignService accountFeignService,
                             KafkaTemplate<String, Object> kafkaTemplate) {
        this.creditCardRepository = creditCardRepository;
        this.customerFeignService = customerFeignService;
        this.accountFeignService = accountFeignService;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ═══════════════════════════════════════════════════
    //  Apply for Credit Card
    // ═══════════════════════════════════════════════════

    /**
     * Creates a new credit card application in PENDING status.
     * Validates customer and account ownership before persisting.
     */
    @Transactional
    public CreditCardResponseDTO applyCard(CreditCardRequestDTO request) {
        logger.info("applyCard started: customerId={}, accountNumber={}",
                request.customerId(), request.accountNumber());

        // Step 1: Validate customer exists
        CustomerDTO customer = fetchCustomer(request.customerId());
        logger.debug("Customer validated: customerId={}", request.customerId());

        // Step 2: Validate account exists and belongs to this customer
        AccountResponseDTO account = fetchAccount(request.accountNumber());
        if (!account.getCustomerId().equals(request.customerId())) {
            logger.error("Ownership mismatch: requestCustomerId={} vs accountOwnerId={}",
                    request.customerId(), account.getCustomerId());
            throw new IllegalArgumentException("Account does not belong to the requesting customer");
        }
        logger.debug("Account ownership validated: accountNumber={}", request.accountNumber());

        // Step 3: Build and persist the credit card entity
        CreditCard card = new CreditCard();
        card.setCardId(UUID.randomUUID());
        card.setCardNumber(generateMaskedCardNumber());
        card.setCustomerId(request.customerId());
        card.setAccountNumber(request.accountNumber());
        card.setCardHolderName(customer.getFirstName() + " " + customer.getLastName());
        card.setCreditLimit(defaultCreditLimit);
        card.setAvailableLimit(defaultCreditLimit);
        card.setOutstandingBalance(BigDecimal.ZERO);
        card.setMinimumDueAmount(BigDecimal.ZERO);
        card.setAnnualFee(defaultAnnualFee);
        card.setInterestRate(defaultInterestRate);
        card.setCardStatus(CardStatus.PENDING);
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setBillingCycleDay(defaultBillingCycleDay);
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Credit card application saved: cardId={}, status={}", card.getCardId(), card.getCardStatus());

        // Step 4: Publish Kafka event (non-critical)
        publishApplicationEvent(card, customer.getEmail());

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Approve Card
    // ═══════════════════════════════════════════════════

    /** Approves a PENDING credit card application. */
    @Transactional
    public CreditCardResponseDTO approveCard(UUID cardId) {
        logger.info("approveCard started: cardId={}", cardId);

        CreditCard card = fetchCard(cardId);
        validateStatus(card, CardStatus.PENDING, "approve");

        card.setCardStatus(CardStatus.APPROVED);
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Credit card approved: cardId={}", cardId);

        publishStatusEvent(card, "APPROVED",
                "Your credit card application has been APPROVED. Card ID: " + cardId);

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Reject Card
    // ═══════════════════════════════════════════════════

    /** Rejects a PENDING credit card application. */
    @Transactional
    public CreditCardResponseDTO rejectCard(UUID cardId) {
        logger.info("rejectCard started: cardId={}", cardId);

        CreditCard card = fetchCard(cardId);
        validateStatus(card, CardStatus.PENDING, "reject");

        card.setCardStatus(CardStatus.REJECTED);
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Credit card rejected: cardId={}", cardId);

        publishStatusEvent(card, "REJECTED",
                "Your credit card application with ID " + cardId + " has been REJECTED.");

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Activate Card
    // ═══════════════════════════════════════════════════

    /** Activates an APPROVED credit card so it can be used for transactions. */
    @Transactional
    public CreditCardResponseDTO activateCard(UUID cardId) {
        logger.info("activateCard started: cardId={}", cardId);

        CreditCard card = fetchCard(cardId);
        validateStatus(card, CardStatus.APPROVED, "activate");

        card.setCardStatus(CardStatus.ACTIVE);
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Credit card activated: cardId={}", cardId);

        publishStatusEvent(card, "ACTIVE",
                "Your credit card " + card.getCardNumber() + " is now ACTIVE.");

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Block Card
    // ═══════════════════════════════════════════════════

    /** Blocks an ACTIVE credit card (suspicious activity or customer request). */
    @Transactional
    public CreditCardResponseDTO blockCard(UUID cardId) {
        logger.info("blockCard started: cardId={}", cardId);

        CreditCard card = fetchCard(cardId);
        validateStatus(card, CardStatus.ACTIVE, "block");

        card.setCardStatus(CardStatus.BLOCKED);
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Credit card blocked: cardId={}", cardId);

        publishStatusEvent(card, "BLOCKED",
                "Your credit card " + card.getCardNumber() + " has been BLOCKED.");

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Unblock Card
    // ═══════════════════════════════════════════════════

    /** Unblocks a BLOCKED credit card, returning it to ACTIVE status. */
    @Transactional
    public CreditCardResponseDTO unblockCard(UUID cardId) {
        logger.info("unblockCard started: cardId={}", cardId);

        CreditCard card = fetchCard(cardId);
        validateStatus(card, CardStatus.BLOCKED, "unblock");

        card.setCardStatus(CardStatus.ACTIVE);
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Credit card unblocked: cardId={}", cardId);

        publishStatusEvent(card, "ACTIVE",
                "Your credit card " + card.getCardNumber() + " has been UNBLOCKED and is now ACTIVE.");

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Close Card
    // ═══════════════════════════════════════════════════

    /**
     * Permanently closes a credit card. Only ACTIVE or BLOCKED cards can be closed.
     * Outstanding balance must be zero before closure.
     */
    @Transactional
    public CreditCardResponseDTO closeCard(UUID cardId) {
        logger.info("closeCard started: cardId={}", cardId);

        CreditCard card = fetchCard(cardId);

        // Allow closing from ACTIVE or BLOCKED
        if (card.getCardStatus() != CardStatus.ACTIVE && card.getCardStatus() != CardStatus.BLOCKED) {
            logger.warn("Cannot close card: cardId={}, currentStatus={}", cardId, card.getCardStatus());
            throw new IllegalStateException(
                    "Cannot close card. Card must be ACTIVE or BLOCKED, current: " + card.getCardStatus());
        }

        // Ensure no outstanding balance
        if (card.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
            logger.warn("Cannot close card with outstanding balance: cardId={}, balance={}",
                    cardId, card.getOutstandingBalance());
            throw new IllegalStateException(
                    "Cannot close card with outstanding balance of " + card.getOutstandingBalance()
                    + ". Please clear the balance first.");
        }

        card.setCardStatus(CardStatus.CLOSED);
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Credit card closed: cardId={}", cardId);

        publishStatusEvent(card, "CLOSED",
                "Your credit card " + card.getCardNumber() + " has been permanently CLOSED.");

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Charge (Purchase)
    // ═══════════════════════════════════════════════════

    /**
     * Charges a purchase amount to the credit card.
     * Reduces availableLimit and increases outstandingBalance.
     */
    @Transactional
    public CreditCardResponseDTO chargeCard(UUID cardId, CreditCardTransactionDTO txn) {
        logger.info("chargeCard started: cardId={}, amount={}, desc='{}'",
                cardId, txn.amount(), txn.description());

        CreditCard card = fetchCard(cardId);
        validateStatus(card, CardStatus.ACTIVE, "charge");

        if (txn.amount() == null || txn.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Charge amount must be greater than zero");
        }

        // Check available credit limit
        if (txn.amount().compareTo(card.getAvailableLimit()) > 0) {
            logger.warn("Charge exceeds available limit: cardId={}, requested={}, available={}",
                    cardId, txn.amount(), card.getAvailableLimit());
            throw new IllegalStateException(
                    "Insufficient credit limit. Available: " + card.getAvailableLimit()
                    + ", Requested: " + txn.amount());
        }

        // Update balances
        card.setAvailableLimit(card.getAvailableLimit().subtract(txn.amount()));
        card.setOutstandingBalance(card.getOutstandingBalance().add(txn.amount()));
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Charge applied: cardId={}, amount={}, newAvailable={}, newOutstanding={}",
                cardId, txn.amount(), card.getAvailableLimit(), card.getOutstandingBalance());

        publishTransactionEvent(card, "CHARGE", txn.amount(), txn.description());

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Payment
    // ═══════════════════════════════════════════════════

    /**
     * Makes a payment toward the credit card's outstanding balance.
     * Debits the linked bank account and restores available credit limit.
     */
    @Transactional
    public CreditCardResponseDTO makePayment(UUID cardId, CreditCardTransactionDTO txn) {
        logger.info("makePayment started: cardId={}, amount={}, desc='{}'",
                cardId, txn.amount(), txn.description());

        CreditCard card = fetchCard(cardId);

        // Payments allowed on ACTIVE or BLOCKED cards (customer should still be able to pay)
        if (card.getCardStatus() != CardStatus.ACTIVE && card.getCardStatus() != CardStatus.BLOCKED) {
            throw new IllegalStateException("Payments can only be made on ACTIVE or BLOCKED cards");
        }

        if (txn.amount() == null || txn.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        // Cap payment at outstanding balance — no overpayment
        BigDecimal paymentAmount = txn.amount().min(card.getOutstandingBalance());
        if (paymentAmount.compareTo(BigDecimal.ZERO) == 0) {
            logger.info("No outstanding balance to pay: cardId={}", cardId);
            return toResponseDTO(card);
        }

        // Debit from the linked bank account
        String idempotencyKey = "CC-PAY-" + cardId + "-" + System.currentTimeMillis();
        try {
            logger.info("Debiting payment from bank account: accountNumber={}, amount={}",
                    card.getAccountNumber(), paymentAmount);
            accountFeignService.withdrawDebit(idempotencyKey, card.getAccountNumber(), paymentAmount);
            logger.info("Bank account debited successfully: accountNumber={}", card.getAccountNumber());
        } catch (Exception ex) {
            logger.error("Failed to debit bank account for payment: cardId={}, accountNumber={}, error={}",
                    cardId, card.getAccountNumber(), ex.getMessage(), ex);
            throw new IllegalStateException(
                    "Payment failed: unable to debit from account " + card.getAccountNumber(), ex);
        }

        // Update card balances after successful bank debit
        card.setOutstandingBalance(card.getOutstandingBalance().subtract(paymentAmount));
        card.setAvailableLimit(card.getAvailableLimit().add(paymentAmount));
        card.setUpdatedAt(LocalDateTime.now());

        creditCardRepository.save(card);
        logger.info("Payment applied: cardId={}, paid={}, newOutstanding={}, newAvailable={}",
                cardId, paymentAmount, card.getOutstandingBalance(), card.getAvailableLimit());


        // -- transaction kafka notification
        publishTransactionEvent(card, "PAYMENT", paymentAmount,
                txn.description() != null ? txn.description() : "Credit card payment");

        // -- notification kafka event (need to add)

        return toResponseDTO(card);
    }

    // ═══════════════════════════════════════════════════
    //  Get Card / Get Cards by Customer
    // ═══════════════════════════════════════════════════

    /** Retrieves a credit card by its ID. */
    public CreditCardResponseDTO getCard(UUID cardId) {
        logger.info("getCard: cardId={}", cardId);
        return toResponseDTO(fetchCard(cardId));
    }

    /** Retrieves all credit cards belonging to a customer. */
    public List<CreditCardResponseDTO> getCardsByCustomer(UUID customerId) {
        logger.info("getCardsByCustomer: customerId={}", customerId);
        List<CreditCard> cards = creditCardRepository.findByCustomerId(customerId);
        logger.info("Found {} cards for customerId={}", cards.size(), customerId);
        return cards.stream().map(this::toResponseDTO).toList();
    }

    // ═══════════════════════════════════════════════════
    //  Private Helpers
    // ═══════════════════════════════════════════════════

    private CreditCard fetchCard(UUID cardId) {
        return creditCardRepository.findById(cardId)
                .orElseThrow(() -> {
                    logger.error("Credit card not found: cardId={}", cardId);
                    return new ResourceNotFoundException("Credit card not found with ID: " + cardId);
                });
    }

    /**
     * Guards status transitions — ensures the card is in the expected state.
     */
    private void validateStatus(CreditCard card, CardStatus expected, String action) {
        if (card.getCardStatus() != expected) {
            logger.warn("Invalid state transition: cannot {} card — cardId={}, current={}, required={}",
                    action, card.getCardId(), card.getCardStatus(), expected);
            throw new IllegalStateException(
                    "Cannot " + action + " card. Expected status: " + expected
                    + ", current: " + card.getCardStatus());
        }
        logger.debug("Status validation passed: cardId={}, status={}, action={}",
                card.getCardId(), card.getCardStatus(), action);
    }

    private CreditCardResponseDTO toResponseDTO(CreditCard card) {
        return new CreditCardResponseDTO(
                card.getCardId(),
                card.getCardNumber(),
                card.getCustomerId(),
                card.getAccountNumber(),
                card.getCardHolderName(),
                card.getCreditLimit(),
                card.getAvailableLimit(),
                card.getOutstandingBalance(),
                card.getMinimumDueAmount(),
                card.getInterestRate(),
                card.getExpiryDate(),
                card.getCardStatus().name()
        );
    }

    /** Generates a masked 16-digit card number: "****-****-****-XXXX" */
    private String generateMaskedCardNumber() {
        Random random = new Random();
        int last4 = 1000 + random.nextInt(9000);
        return "****-****-****-" + last4;
    }

    // ═══════════════════════════════════════════════════
    //  Feign Helpers
    // ═══════════════════════════════════════════════════

    private CustomerDTO fetchCustomer(UUID customerId) {
        logger.debug("Fetching customer: customerId={}", customerId);
        CustomerDTO dto = customerFeignService.findById(customerId).getBody();
        if (dto == null) {
            logger.error("Customer not found: customerId={}", customerId);
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        return dto;
    }

    private AccountResponseDTO fetchAccount(String accountNumber) {
        logger.debug("Fetching account: accountNumber={}", accountNumber);
        AccountResponseDTO dto = accountFeignService.getAccountByAccountNumber(accountNumber).getBody();
        if (dto == null) {
            logger.error("Account not found: accountNumber={}", accountNumber);
            throw new ResourceNotFoundException("Account not found: " + accountNumber);
        }
        return dto;
    }

    // ═══════════════════════════════════════════════════
    //  Kafka Event Publishers (non-critical)
    // ═══════════════════════════════════════════════════

    private void publishApplicationEvent(CreditCard card, String email) {
        try {
            CreditCardApplicationEvent event = new CreditCardApplicationEvent(
                    card.getCardId(), card.getCustomerId(), email,
                    card.getCreditLimit(),
                    "Credit card application submitted. Card ID: " + card.getCardId());
            kafkaTemplate.send(KafkaConstants.CREDIT_CARD_APPLICATION_TOPIC,
                            card.getCustomerId().toString(), event)
                    .whenComplete((r, ex) -> {
                        if (ex != null) logger.error("Kafka failed [CC_APPLICATION]: cardId={}", card.getCardId(), ex);
                        else logger.info("Kafka sent [CC_APPLICATION]: cardId={}", card.getCardId());
                    });
        } catch (Exception e) {
            logger.error("Failed to publish CC application event: cardId={}", card.getCardId(), e);
        }
    }

    private void publishStatusEvent(CreditCard card, String status, String message) {
        try {
            CreditCardStatusEvent event = new CreditCardStatusEvent(
                    card.getCardId(), card.getCustomerId(), status, message);
            kafkaTemplate.send(KafkaConstants.CREDIT_CARD_STATUS_TOPIC,
                            card.getCustomerId().toString(), event)
                    .whenComplete((r, ex) -> {
                        if (ex != null) logger.error("Kafka failed [CC_STATUS]: cardId={}, status={}", card.getCardId(), status, ex);
                        else logger.info("Kafka sent [CC_STATUS]: cardId={}, status={}", card.getCardId(), status);
                    });
        } catch (Exception e) {
            logger.error("Failed to publish CC status event: cardId={}", card.getCardId(), e);
        }
    }

    private void publishTransactionEvent(CreditCard card, String type, BigDecimal amount, String desc) {
        try {
            CreditCardTransactionEvent event = new CreditCardTransactionEvent(
                    card.getCardId(), card.getCustomerId(), type, amount,
                    card.getOutstandingBalance(), card.getAvailableLimit(), desc);
            kafkaTemplate.send(KafkaConstants.CREDIT_CARD_TRANSACTION_TOPIC,
                            card.getCustomerId().toString(), event)
                    .whenComplete((r, ex) -> {
                        if (ex != null) logger.error("Kafka failed [CC_TXN]: cardId={}, type={}", card.getCardId(), type, ex);
                        else logger.info("Kafka sent [CC_TXN]: cardId={}, type={}, amount={}", card.getCardId(), type, amount);
                    });
        } catch (Exception e) {
            logger.error("Failed to publish CC transaction event: cardId={}", card.getCardId(), e);
        }
    }
}
