package com.bank.controller;

import com.bank.dto.CreditCardRequestDTO;
import com.bank.dto.CreditCardResponseDTO;
import com.bank.dto.CreditCardTransactionDTO;
import com.bank.service.CreditCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Credit Card Service.
 * Base path: {@code /api/v1/credit-cards}
 */
@RestController
@RequestMapping("/api/v1/credit-cards")
public class CreditCardController {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardController.class);

    private final CreditCardService creditCardService;

    public CreditCardController(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    // ── Apply ──

    /** Submit a new credit card application. */
    @PostMapping("/apply")
    public ResponseEntity<CreditCardResponseDTO> applyCard(@RequestBody CreditCardRequestDTO request) {
        logger.info("Received request to apply for credit card: customerId={}", request.customerId());
        CreditCardResponseDTO response = creditCardService.applyCard(request);
        logger.info("Credit card application created: cardId={}", response.cardId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Approve ──

    /** Approve a pending credit card application. */
    @PutMapping("/{cardId}/approve")
    public ResponseEntity<CreditCardResponseDTO> approveCard(@PathVariable UUID cardId) {
        logger.info("Received request to approve card: cardId={}", cardId);
        CreditCardResponseDTO response = creditCardService.approveCard(cardId);
        return ResponseEntity.ok(response);
    }

    // ── Reject ──

    /** Reject a pending credit card application. */
    @PutMapping("/{cardId}/reject")
    public ResponseEntity<CreditCardResponseDTO> rejectCard(@PathVariable UUID cardId) {

        logger.info("Received request to reject card: cardId={}", cardId);

        CreditCardResponseDTO response = creditCardService.rejectCard(cardId);

        return ResponseEntity.ok(response);
    }

    // ── Activate ──

    /** Activate an approved credit card. */
    @PutMapping("/{cardId}/activate")
    public ResponseEntity<CreditCardResponseDTO> activateCard(@PathVariable UUID cardId) {
        logger.info("Received request to activate card: cardId={}", cardId);
        CreditCardResponseDTO response = creditCardService.activateCard(cardId);
        return ResponseEntity.ok(response);
    }

    // ── Block ──

    /** Block an active credit card. */
    @PutMapping("/{cardId}/block")
    public ResponseEntity<CreditCardResponseDTO> blockCard(@PathVariable UUID cardId) {
        logger.info("Received request to block card: cardId={}", cardId);
        CreditCardResponseDTO response = creditCardService.blockCard(cardId);
        return ResponseEntity.ok(response);
    }

    // ── Unblock ──

    /** Unblock a blocked credit card. */
    @PutMapping("/{cardId}/unblock")
    public ResponseEntity<CreditCardResponseDTO> unblockCard(@PathVariable UUID cardId) {
        logger.info("Received request to unblock card: cardId={}", cardId);
        CreditCardResponseDTO response = creditCardService.unblockCard(cardId);
        return ResponseEntity.ok(response);
    }

    // ── Close ──

    /** Permanently close a credit card (balance must be zero). */
    @PutMapping("/{cardId}/close")
    public ResponseEntity<CreditCardResponseDTO> closeCard(@PathVariable UUID cardId) {
        logger.info("Received request to close card: cardId={}", cardId);
        CreditCardResponseDTO response = creditCardService.closeCard(cardId);
        return ResponseEntity.ok(response);
    }

    // ── Charge ──

    /** Make a purchase/charge on the credit card. */
    @PostMapping("/{cardId}/charge")
    public ResponseEntity<CreditCardResponseDTO> chargeCard(
            @PathVariable UUID cardId,
            @RequestBody CreditCardTransactionDTO transaction) {
        logger.info("Received charge request: cardId={}, amount={}", cardId, transaction.amount());
        CreditCardResponseDTO response = creditCardService.chargeCard(cardId, transaction);
        return ResponseEntity.ok(response);
    }

    // ── Payment ──

    /** Make a payment toward the outstanding balance. */
    @PostMapping("/{cardId}/payment")
    public ResponseEntity<CreditCardResponseDTO> makePayment(
            @PathVariable UUID cardId,
            @RequestBody CreditCardTransactionDTO transaction) {
        logger.info("Received payment request: cardId={}, amount={}", cardId, transaction.amount());
        CreditCardResponseDTO response = creditCardService.makePayment(cardId, transaction);
        return ResponseEntity.ok(response);
    }

    // ── Get Card ──

    /** Get credit card details by card ID. */
    @GetMapping("/{cardId}")
    public ResponseEntity<CreditCardResponseDTO> getCardById(@PathVariable UUID cardId) {
        logger.info("Received request to fetch card: cardId={}", cardId);
        CreditCardResponseDTO response = creditCardService.getCard(cardId);
        return ResponseEntity.ok(response);
    }

    // ── Get Cards by Customer ──

    /** Get all credit cards for a customer. */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CreditCardResponseDTO>> getCardsByCustomer(@PathVariable UUID customerId) {
        logger.info("Received request to fetch cards for customer: customerId={}", customerId);
        List<CreditCardResponseDTO> response = creditCardService.getCardsByCustomer(customerId);
        return ResponseEntity.ok(response);
    }
}
