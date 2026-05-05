package com.bank.controller;

import com.bank.ENUM.BankStatus;
import com.bank.dto.BankRequestDTO;
import com.bank.dto.BankResponseDTO;
import com.bank.service.BankService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for bank-service.
 * Base path: {@code /api/v1/banks}
 *
 * <p><b>Account creation</b> is NOT handled here. Clients open accounts by
 * calling {@code POST /api/v1/accounts/create-account} on the account-service
 * directly, passing the optional {@code bankId} in the request body.
 */
@RestController
@RequestMapping("/api/v1/banks")
public class BankController {

    private static final Logger logger = LoggerFactory.getLogger(BankController.class);

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    /** POST /api/v1/banks/register — Register a new bank. */
    @PostMapping("/register")
    public ResponseEntity<BankResponseDTO> registerBank(@Valid @RequestBody BankRequestDTO request) {
        logger.info("POST /register - bankName={}, bankCode={}", request.bankName(), request.bankCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(bankService.registerBank(request));
    }

    /** GET /api/v1/banks/{bankId} — Get bank by UUID. */
    @GetMapping("/{bankId}")
    public ResponseEntity<BankResponseDTO> getBankById(@PathVariable UUID bankId) {
        logger.info("GET /{}", bankId);
        return ResponseEntity.ok(bankService.getBankById(bankId));
    }

    /** GET /api/v1/banks/code/{bankCode} — Get bank by its short code (e.g. "SBIN"). */
    @GetMapping("/code/{bankCode}")
    public ResponseEntity<BankResponseDTO> getBankByCode(@PathVariable String bankCode) {
        logger.info("GET /code/{}", bankCode);
        return ResponseEntity.ok(bankService.getBankByCode(bankCode));
    }

    /** GET /api/v1/banks — List all registered banks. */
    @GetMapping
    public ResponseEntity<List<BankResponseDTO>> getAllBanks() {
        logger.info("GET /");
        return ResponseEntity.ok(bankService.getAllBanks());
    }

    /** PUT /api/v1/banks/{bankId}/status?status=SUSPENDED — Change bank operational status. */
    @PutMapping("/{bankId}/status")
    public ResponseEntity<BankResponseDTO> updateBankStatus(
            @PathVariable UUID bankId,
            @RequestParam BankStatus status) {
        logger.info("PUT /{}/status - newStatus={}", bankId, status);
        return ResponseEntity.ok(bankService.updateBankStatus(bankId, status));
    }

    /** DELETE /api/v1/banks/{bankId} — Remove a bank record. */
    @DeleteMapping("/{bankId}")
    public ResponseEntity<Void> deleteBank(@PathVariable UUID bankId) {
        logger.info("DELETE /{}", bankId);
        bankService.deleteBank(bankId);
        return ResponseEntity.noContent().build();
    }
}
