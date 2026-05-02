package com.bank.controller;

import com.bank.dto.LoanRequestDTO;
import com.bank.dto.LoanResponseDTO;
import com.bank.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing the loan lifecycle.
 *
 * <p>Exposes endpoints for applying, approving, rejecting,
 * disbursing, and retrieving loans.</p>
 *
 * <p>Base path: {@code /api/v1/loans}</p>
 */
@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // ═══════════════════════════════════════════════════
    //  Apply Loan
    // ═══════════════════════════════════════════════════

    /**
     * Submits a new loan application.
     *
     * <p>Creates a loan in PENDING status. The customer and account
     * are validated before the loan is persisted.</p>
     *
     * @param loanRequestDTO the loan application request body
     * @return the created loan details with HTTP 201
     */
    @PostMapping("/apply")
    public ResponseEntity<LoanResponseDTO> applyLoan(@RequestBody LoanRequestDTO loanRequestDTO) {
        logger.info("Received request to apply loan: customerId={}, accountNumber={}, amount={}",
                loanRequestDTO.customerId(), loanRequestDTO.accountNumber(), loanRequestDTO.loanAmount());

        LoanResponseDTO response = loanService.applyLoan(loanRequestDTO);

        logger.info("Loan application created successfully: loanId={}", response.loanId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ═══════════════════════════════════════════════════
    //  Approve Loan
    // ═══════════════════════════════════════════════════

    /**
     * Approves a pending loan application.
     *
     * <p>Calculates the EMI and transitions the loan from PENDING to APPROVED.</p>
     *
     * @param loanId the UUID of the loan to approve
     * @return the updated loan details with computed EMI
     */
    @PutMapping("/{loanId}/approve")
    public ResponseEntity<LoanResponseDTO> approveLoan(@PathVariable UUID loanId) {
        logger.info("Received request to approve loan: loanId={}", loanId);

        LoanResponseDTO response = loanService.approveLoan(loanId);

        logger.info("Loan approved successfully: loanId={}, emiAmount={}", response.loanId(), response.emiAmount());
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════
    //  Reject Loan
    // ═══════════════════════════════════════════════════

    /**
     * Rejects a pending loan application.
     *
     * <p>Transitions the loan from PENDING to REJECTED.</p>
     *
     * @param loanId the UUID of the loan to reject
     * @return the updated loan details
     */
    @PutMapping("/{loanId}/reject")
    public ResponseEntity<LoanResponseDTO> rejectLoan(@PathVariable UUID loanId) {
        logger.info("Received request to reject loan: loanId={}", loanId);

        LoanResponseDTO response = loanService.rejectLoan(loanId);

        logger.info("Loan rejected successfully: loanId={}", response.loanId());
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════
    //  Disburse Loan
    // ═══════════════════════════════════════════════════

    /**
     * Disburses an approved loan.
     *
     * <p>Credits the loan amount to the customer's account and
     * transitions the loan from APPROVED to ACTIVE.</p>
     *
     * @param loanId the UUID of the loan to disburse
     * @return the updated loan details
     */
    @PutMapping("/{loanId}/disburse")
    public ResponseEntity<LoanResponseDTO> disburseLoan(@PathVariable UUID loanId) {
        logger.info("Received request to disburse loan: loanId={}", loanId);

        LoanResponseDTO response = loanService.disburseLoan(loanId);

        logger.info("Loan disbursed successfully: loanId={}, amount={}", response.loanId(), response.amount());
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════
    //  Get Loan
    // ═══════════════════════════════════════════════════

    /**
     * Retrieves a loan by its ID.
     *
     * @param loanId the UUID of the loan to fetch
     * @return the loan details
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable UUID loanId) {
        logger.info("Received request to fetch loan: loanId={}", loanId);

        LoanResponseDTO response = loanService.getLoan(loanId);

        logger.info("Loan fetched successfully: loanId={}, status={}", response.loanId(), response.status());
        return ResponseEntity.ok(response);
    }
}
