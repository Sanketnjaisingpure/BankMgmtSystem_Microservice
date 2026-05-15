package com.bank.service;

import com.bank.ENUM.LoanStatus;
import com.bank.config.KafkaConstants;
import com.bank.dto.CustomerDTO;
import com.bank.dto.LoanRequestDTO;
import com.bank.dto.LoanResponseDTO;
import com.bank.dto.accounts.AccountResponseDTO;
import com.bank.event.LoanApplicationEvent;
import com.bank.event.LoanDisbursementEvent;
import com.bank.event.LoanStatusEvent;
import com.bank.event.TransactionEvent;
import com.bank.ENUM.TransactionType;
import com.bank.ENUM.TransactionStatus;
import com.bank.exception.ResourceNotFoundException;
import com.bank.feign.AccountFeignService;
import com.bank.feign.customers.CustomerFeignService;
import com.bank.model.Loan;
import com.bank.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core service handling the full loan lifecycle:
 * Apply → Approve/Reject → Disburse.
 *
 * <p><b>Loan State Machine:</b></p>
 * <pre>
 *   PENDING ──→ APPROVED ──→ ACTIVE (disbursed)
 *      │
 *      └──→ REJECTED
 * </pre>
 *
 * <p><b>Cross-service interactions:</b></p>
 * <ul>
 *   <li>Customer Service (Feign) — validates customer existence</li>
 *   <li>Account Service (Feign)  — validates account existence and ownership</li>
 *   <li>Kafka                    — publishes loan application, status, and disbursement events</li>
 * </ul>
 *
 * <p>Kafka events are fire-and-forget; failures are logged but never
 * block or roll back the database transaction.</p>
 */
@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final AccountFeignService accountFeignService;
    private final CustomerFeignService customerFeignService;
    private final LoanRepository loanRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /** Default annual interest rate (%), configurable via application properties. */
    @Value("${loan.default.interest-rate:12.5}")
    private Double defaultInterestRate;

    public LoanService(AccountFeignService accountFeignService,
                       CustomerFeignService customerFeignService,
                       LoanRepository loanRepository,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountFeignService = accountFeignService;
        this.customerFeignService = customerFeignService;
        this.loanRepository = loanRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ═══════════════════════════════════════════════════
    //  Apply Loan
    // ═══════════════════════════════════════════════════

    /**
     * Creates a new loan application in {@link LoanStatus#PENDING} state.
     *
     * <p><b>Workflow:</b></p>
     * <ol>
     *   <li>Fetch and validate the customer via Customer Service</li>
     *   <li>Fetch and validate the account via Account Service</li>
     *   <li>Cross-validate that the account belongs to the requesting customer</li>
     *   <li>Persist the loan with status PENDING (EMI is calculated later at approval)</li>
     *   <li>Publish a {@code LOAN_APPLICATION} Kafka event for notification</li>
     * </ol>
     *
     * @param loanRequestDTO the loan application request payload
     * @return the created loan details
     * @throws ResourceNotFoundException if the customer or account does not exist
     * @throws IllegalArgumentException  if the account does not belong to the customer
     */
    @Transactional
    public LoanResponseDTO applyLoan(LoanRequestDTO loanRequestDTO) {
        logger.info("applyLoan started: customerId={}, accountNumber={}, amount={}",
                loanRequestDTO.customerId(), loanRequestDTO.accountNumber(), loanRequestDTO.loanAmount());

        // Step 1: Validate that the customer exists in Customer Service
        CustomerDTO customer = getCustomerDetails(loanRequestDTO.customerId());
        logger.debug("Customer validated: customerId={}, name={} {}",
                loanRequestDTO.customerId(), customer.getFirstName(), customer.getLastName());

        // Step 2: Validate that the account exists in Account Service
        AccountResponseDTO account = getAccountDetails(loanRequestDTO.accountNumber());
        logger.debug("Account validated: accountNumber={}, accountOwnerId={}",
                loanRequestDTO.accountNumber(), account.getCustomerId());

        // Step 3: Cross-validate ownership — the account must belong to the requesting customer
        if (!account.getCustomerId().equals(loanRequestDTO.customerId())) {
            logger.error("Ownership mismatch: requestCustomerId={} vs accountOwnerId={}",
                    loanRequestDTO.customerId(), account.getCustomerId());
            throw new IllegalArgumentException("Customer ID does not match the Account owner");
        }
        logger.debug("Ownership cross-validation passed for customerId={}", loanRequestDTO.customerId());

        // Step 4: Build and persist the loan entity
        // EMI calculation is deferred to the approval step to keep application fast
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setCustomerId(loanRequestDTO.customerId());
        loan.setAccountNumber(loanRequestDTO.accountNumber());
        loan.setLoanAmount(loanRequestDTO.loanAmount());
        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setTenureMonths(loanRequestDTO.tenureMonths());
        loan.setInterestRate(defaultInterestRate);
        loan.setCreatedAt(LocalDateTime.now());
        loan.setUpdatedAt(LocalDateTime.now());

        loanRepository.save(loan);
        logger.info("Loan application saved: loanId={}, status={}", loan.getLoanId(), loan.getLoanStatus());

        // Step 5: Publish Kafka event (non-critical — failure won't roll back the DB transaction)
        sendApplicationEvent(loan, customer.getEmail(), loanRequestDTO.loanAmount());

        return toResponseDTO(loan);
    }

    // ═══════════════════════════════════════════════════
    //  Approve Loan (EMI calculated here)
    // ═══════════════════════════════════════════════════

    /**
     * Approves a {@link LoanStatus#PENDING} loan and computes its EMI.
     *
     * <p><b>Workflow:</b></p>
     * <ol>
     *   <li>Fetch the loan and validate it is in PENDING status</li>
     *   <li>Calculate EMI using the standard reducing-balance formula</li>
     *   <li>Transition status to APPROVED and persist</li>
     *   <li>Publish a {@code LOAN_STATUS} Kafka event</li>
     * </ol>
     *
     * @param loanId the UUID of the loan to approve
     * @return the updated loan details including the computed EMI
     * @throws ResourceNotFoundException if the loan does not exist
     * @throws IllegalStateException     if the loan is not in PENDING status
     */
    @Transactional
    public LoanResponseDTO approveLoan(UUID loanId) {
        logger.info("approveLoan started: loanId={}", loanId);

        // Step 1: Fetch loan and guard against invalid state transitions
        Loan loan = fetchLoan(loanId);
        validateStatus(loan, LoanStatus.PENDING, "approve");

        // Step 2: Calculate EMI at approval time (not at application time)
        BigDecimal emi = calculateEMI(
                loan.getLoanAmount(),
                BigDecimal.valueOf(loan.getInterestRate()),
                loan.getTenureMonths()
        );

        // Step 3: Update loan state
        loan.setEmiAmount(emi);
        loan.setLoanStatus(LoanStatus.APPROVED);
        loan.setUpdatedAt(LocalDateTime.now());

        loanRepository.save(loan);
        logger.info("Loan approved: loanId={}, emiAmount={}, interestRate={}%, tenureMonths={}",
                loanId, emi, loan.getInterestRate(), loan.getTenureMonths());

        // Step 4: Notify via Kafka (non-critical)
        sendStatusEvent(loan, "APPROVED",
                "Your loan with ID " + loanId + " has been APPROVED. EMI: " + emi);

        return toResponseDTO(loan);
    }

    // ═══════════════════════════════════════════════════
    //  Reject Loan
    // ═══════════════════════════════════════════════════

    /**
     * Rejects a {@link LoanStatus#PENDING} loan application.
     *
     * <p>Only loans in PENDING status can be rejected. Already approved,
     * active, or previously rejected loans will cause an {@link IllegalStateException}.</p>
     *
     * @param loanId the UUID of the loan to reject
     * @return the updated loan details
     * @throws ResourceNotFoundException if the loan does not exist
     * @throws IllegalStateException     if the loan is not in PENDING status
     */
    @Transactional
    public LoanResponseDTO rejectLoan(UUID loanId) {
        logger.info("rejectLoan started: loanId={}", loanId);

        Loan loan = fetchLoan(loanId);
        validateStatus(loan, LoanStatus.PENDING, "reject");

        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setUpdatedAt(LocalDateTime.now());

        loanRepository.save(loan);
        logger.info("Loan rejected: loanId={}", loanId);

        // Notify the customer via Kafka
        sendStatusEvent(loan, "REJECTED",
                "Your loan application with ID " + loanId + " has been REJECTED.");

        return toResponseDTO(loan);
    }

    // ═══════════════════════════════════════════════════
    //  Disburse Loan
    // ═══════════════════════════════════════════════════

    /**
     * Disburses an {@link LoanStatus#APPROVED} loan, transitioning it to {@link LoanStatus#ACTIVE}.
     *
     * <p>Includes a safety check ensuring the EMI was properly computed during approval.
     * If EMI is missing or zero, disbursement is blocked to prevent data inconsistency.</p>
     *
     * @param loanId the UUID of the loan to disburse
     * @return the updated loan details
     * @throws ResourceNotFoundException if the loan does not exist
     * @throws IllegalStateException     if the loan is not APPROVED or EMI is invalid
     */
    @Transactional
    public LoanResponseDTO disburseLoan(UUID loanId) {
        logger.info("disburseLoan started: loanId={}", loanId);

        // Step 1: Fetch loan and validate it is in APPROVED status
        Loan loan = fetchLoan(loanId);
        validateStatus(loan, LoanStatus.APPROVED, "disburse");

        // Step 2: Safety check — EMI must have been computed during approval
        if (loan.getEmiAmount() == null || loan.getEmiAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Disbursement blocked — EMI is missing or zero: loanId={}, emiAmount={}",
                    loanId, loan.getEmiAmount());
            throw new IllegalStateException(
                    "Loan cannot be disbursed: EMI amount not computed. Please re-approve the loan.");
        }

        // Step 3: Validate that the target account exists and is ACTIVE before depositing
        AccountResponseDTO account = getAccountDetails(loan.getAccountNumber());
        if (account.getStatus() != com.bank.ENUM.accounts.AccountStatus.ACTIVE) {
            logger.error("Disbursement blocked — account is not ACTIVE: loanId={}, accountNumber={}, status={}",
                    loanId, loan.getAccountNumber(), account.getStatus());
            throw new IllegalStateException(
                    "Loan cannot be disbursed: account " + loan.getAccountNumber()
                    + " is " + account.getStatus() + ". Only ACTIVE accounts can receive disbursements.");
        }
        logger.info("Account validated for disbursement: accountNumber={}, status={}",
                loan.getAccountNumber(), account.getStatus());

        // Step 4: Credit the loan amount to the customer's account via Account Service
        // Using loanId as the idempotency key to prevent duplicate credits on retries
        String idempotencyKey = loanId.toString();
        try {
            logger.info("Crediting loan amount to account: loanId={}, accountNumber={}, amount={}, idempotencyKey={}",
                    loanId, loan.getAccountNumber(), loan.getLoanAmount(), idempotencyKey);

            accountFeignService.depositCredit(
                    idempotencyKey,
                    loan.getAccountNumber(),
                    loan.getLoanAmount()
            );

            logger.info("Loan amount credited successfully: loanId={}, accountNumber={}",
                    loanId, loan.getAccountNumber());
        } catch (Exception ex) {
            // If the deposit fails, do NOT mark the loan as ACTIVE — let @Transactional roll back
            logger.error("Failed to credit loan amount to account: loanId={}, accountNumber={}, error={}",
                    loanId, loan.getAccountNumber(), ex.getMessage(), ex);
            throw new IllegalStateException(
                    "Loan disbursement failed: unable to credit amount to account " + loan.getAccountNumber(), ex);
        }

        // Step 5: Mark loan as ACTIVE only after successful fund transfer
        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setUpdatedAt(LocalDateTime.now());

        loanRepository.save(loan);
        logger.info("Loan disbursed and set ACTIVE: loanId={}, amount={}, accountNumber={}",
                loanId, loan.getLoanAmount(), loan.getAccountNumber());

        // Step 6: Publish disbursement Kafka event for notification
        sendDisbursementEvent(loan);

        // Step 7: Publish transaction event so transaction-service records the loan disbursement
        sendDisbursementTransactionEvent(loan);

        return toResponseDTO(loan);
    }

    // ═══════════════════════════════════════════════════
    //  Get Loan
    // ═══════════════════════════════════════════════════

    /**
     * Retrieves a loan by its ID.
     *
     * @param loanId the UUID of the loan to fetch
     * @return the loan details
     * @throws ResourceNotFoundException if the loan does not exist
     */
    public LoanResponseDTO getLoan(UUID loanId) {
        logger.info("getLoan: loanId={}", loanId);
        Loan loan = fetchLoan(loanId);
        return toResponseDTO(loan);
    }

    // ═══════════════════════════════════════════════════
    //  EMI Calculation
    // ═══════════════════════════════════════════════════

    /**
     * Calculates the Equated Monthly Instalment (EMI) using the standard
     * reducing-balance formula:
     *
     * <pre>
     *   EMI = P × r × (1 + r)^n / ((1 + r)^n − 1)
     *
     *   where:
     *     P = principal loan amount
     *     r = monthly interest rate  (annualRate / 12 / 100)
     *     n = tenure in months
     * </pre>
     *
     * <p>If the annual interest rate is zero (interest-free loan),
     * EMI is simply {@code principal / months}.</p>
     *
     * @param principal  the loan principal amount (must be &gt; 0)
     * @param annualRate the annual interest rate as a percentage (e.g., 12.5 for 12.5%)
     * @param months     the loan tenure in months (must be &gt; 0)
     * @return the calculated EMI rounded to 2 decimal places
     * @throws IllegalArgumentException if any input is invalid
     */
    public BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, int months) {
        logger.debug("calculateEMI: principal={}, annualRate={}%, months={}", principal, annualRate, months);

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be greater than zero");
        }
        if (months <= 0) {
            throw new IllegalArgumentException("Tenure in months must be greater than zero");
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual interest rate cannot be negative");
        }

        BigDecimal emi;

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            // Interest-free loan: simple division
            emi = principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        } else {
            // Standard reducing-balance EMI formula
            BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
            BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate).pow(months);
            BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
            BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
            emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        }

        logger.debug("calculateEMI result: emi={}", emi);
        return emi;
    }

    // ═══════════════════════════════════════════════════
    //  Private Helpers
    // ═══════════════════════════════════════════════════

    /**
     * Fetches a loan entity from the database or throws if not found.
     *
     * @param loanId the UUID of the loan to fetch
     * @return the loan entity
     * @throws ResourceNotFoundException if no loan exists with the given ID
     */
    private Loan fetchLoan(UUID loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.error("Loan not found: loanId={}", loanId);
                    return new ResourceNotFoundException("Loan not found with ID: " + loanId);
                });
    }

    /**
     * Guards a loan status transition. Ensures the loan is currently in the
     * {@code expected} status before allowing the {@code action} to proceed.
     *
     * <p>This enforces the loan state machine and prevents invalid transitions
     * such as approving an already-rejected loan or disbursing a pending one.</p>
     *
     * @param loan     the loan entity to validate
     * @param expected the required current status for the action to proceed
     * @param action   a human-readable action name for error messages (e.g., "approve", "reject")
     * @throws IllegalStateException if the current status does not match the expected status
     */
    private void validateStatus(Loan loan, LoanStatus expected, String action) {
        if (loan.getLoanStatus() != expected) {
            logger.warn("Invalid state transition: cannot {} loan — loanId={}, currentStatus={}, requiredStatus={}",
                    action, loan.getLoanId(), loan.getLoanStatus(), expected);
            throw new IllegalStateException(
                    "Cannot " + action + " loan. Expected status: " + expected
                    + ", current: " + loan.getLoanStatus());
        }
        logger.debug("Status validation passed: loanId={}, status={}, action={}",
                loan.getLoanId(), loan.getLoanStatus(), action);
    }

    /**
     * Maps a {@link Loan} entity to a {@link LoanResponseDTO}.
     */
    private LoanResponseDTO toResponseDTO(Loan loan) {
        return new LoanResponseDTO(
                loan.getLoanId(),
                loan.getCustomerId(),
                loan.getAccountNumber(),
                loan.getLoanAmount(),
                loan.getEmiAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getLoanStatus().name()
        );
    }

    // ═══════════════════════════════════════════════════
    //  Kafka Event Publishers
    //  Non-critical — failures are logged but never block
    //  or roll back the database transaction.
    // ═══════════════════════════════════════════════════

    /**
     * Publishes a loan application event to Kafka for downstream notification.
     *
     * @param loan   the newly created loan entity
     * @param email  the customer's email address
     * @param amount the requested loan amount
     */
    private void sendApplicationEvent(Loan loan, String email, BigDecimal amount) {
        try {
            LoanApplicationEvent event = new LoanApplicationEvent();
            event.setCustomerId(loan.getCustomerId());
            event.setAccountNumber(loan.getAccountNumber());
            event.setEmail(email);
            event.setMessage("Loan application for amount " + amount
                    + " submitted successfully. Loan ID: " + loan.getLoanId());

            // ── Notification-specific fields ──
            event.setSourceService("LOAN_SERVICE");
            event.setNotificationType("LOAN_APPLIED");
            event.setSubject("Loan Application Submitted");
            event.setReferenceId(loan.getLoanId().toString());
            event.setMetadata(String.format(
                    "{\"loanId\":\"%s\",\"loanAmount\":\"%s\",\"tenureMonths\":%d,\"accountNumber\":\"%s\"}",
                    loan.getLoanId(), amount, loan.getTenureMonths(), loan.getAccountNumber()
            ));

            kafkaTemplate.send(KafkaConstants.LOAN_APPLICATION_TOPIC, loan.getCustomerId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Kafka send failed [LOAN_APPLICATION]: loanId={}, error={}",
                                    loan.getLoanId(), ex.getMessage(), ex);
                        } else {
                            logger.info("Kafka sent [LOAN_APPLICATION]: loanId={}, offset={}",
                                    loan.getLoanId(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to publish loan application event: loanId={}", loan.getLoanId(), e);
        }
    }

    /**
     * Publishes a loan status change event (APPROVED / REJECTED) to Kafka.
     *
     * @param loan    the loan entity
     * @param status  the new loan status
     * @param message a human-readable message for the customer notification
     */
    private void sendStatusEvent(Loan loan, String status, String message) {
        try {
            LoanStatusEvent event = new LoanStatusEvent();
            event.setLoanId(loan.getLoanId());
            event.setCustomerId(loan.getCustomerId());
            event.setStatus(status);
            event.setMessage(message);

            // ── Notification-specific fields ──
            event.setSourceService("LOAN_SERVICE");
            event.setNotificationType("APPROVED".equals(status) ? "LOAN_APPROVED" : "LOAN_REJECTED");
            event.setSubject("Loan " + status);
            event.setReferenceId(loan.getLoanId().toString());
            event.setMetadata(String.format(
                    "{\"loanId\":\"%s\",\"loanStatus\":\"%s\",\"loanAmount\":\"%s\",\"emiAmount\":\"%s\"}",
                    loan.getLoanId(), status, loan.getLoanAmount(),
                    loan.getEmiAmount() != null ? loan.getEmiAmount() : "N/A"
            ));

            kafkaTemplate.send(KafkaConstants.LOAN_STATUS_TOPIC, loan.getCustomerId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Kafka send failed [LOAN_STATUS]: loanId={}, status={}, error={}",
                                    loan.getLoanId(), status, ex.getMessage(), ex);
                        } else {
                            logger.info("Kafka sent [LOAN_STATUS]: loanId={}, status={}, offset={}",
                                    loan.getLoanId(), status, result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to publish loan status event: loanId={}, status={}", loan.getLoanId(), status, e);
        }
    }

    /**
     * Publishes a loan disbursement event to Kafka for downstream processing
     * (e.g., notifying the customer that the loan amount has been credited).
     *
     * @param loan the disbursed loan entity
     */
    private void sendDisbursementEvent(Loan loan) {
        try {
            LoanDisbursementEvent event = new LoanDisbursementEvent();
            event.setCustomerId(loan.getCustomerId());
            event.setAccountNumber(loan.getAccountNumber());
            event.setEmail("dummy@example.com");
            event.setMessage("Your loan with ID " + loan.getLoanId()
                    + " has been DISBURSED. Amount: " + loan.getLoanAmount());

            // ── Notification-specific fields ──
            event.setSourceService("LOAN_SERVICE");
            event.setNotificationType("LOAN_DISBURSED");
            event.setSubject("Loan Disbursed");
            event.setReferenceId(loan.getLoanId().toString());
            event.setMetadata(String.format(
                    "{\"loanId\":\"%s\",\"loanAmount\":\"%s\",\"accountNumber\":\"%s\",\"emiAmount\":\"%s\"}",
                    loan.getLoanId(), loan.getLoanAmount(), loan.getAccountNumber(), loan.getEmiAmount()
            ));

            kafkaTemplate.send(KafkaConstants.LOAN_DISBURSEMENT_TOPIC, loan.getCustomerId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Kafka send failed [LOAN_DISBURSEMENT]: loanId={}, error={}",
                                    loan.getLoanId(), ex.getMessage(), ex);
                        } else {
                            logger.info("Kafka sent [LOAN_DISBURSEMENT]: loanId={}, offset={}",
                                    loan.getLoanId(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to publish loan disbursement event: loanId={}", loan.getLoanId(), e);
        }
    }

    /**
     * Publishes a {@link TransactionEvent} to the transaction-service via Kafka
     * so it independently records the loan disbursement as a DEPOSIT transaction.
     *
     * <p>This is separate from the account-service's own transaction event —
     * it ensures the loan disbursement is explicitly captured even if the
     * account-service event fails or is delayed.</p>
     *
     * @param loan the disbursed loan entity
     */
    private void sendDisbursementTransactionEvent(Loan loan) {
        try {
            TransactionEvent txnEvent = new TransactionEvent();
            txnEvent.setTransactionType(TransactionType.DEPOSIT);
            txnEvent.setTransactionStatus(TransactionStatus.SUCCESS);
            txnEvent.setAmount(loan.getLoanAmount());
            txnEvent.setSourceAccountNumber(loan.getAccountNumber());
            txnEvent.setDestinationAccountNumber(loan.getAccountNumber());
            txnEvent.setTransactionDescription(
                    "Loan disbursement — loanId: " + loan.getLoanId()
                    + " | amount: " + loan.getLoanAmount()
                    + " | account: " + loan.getAccountNumber()
            );

            kafkaTemplate.send(KafkaConstants.TRANSACTION_PAYMENT_TOPIC,
                            loan.getCustomerId().toString(), txnEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Kafka send failed [LOAN_DISBURSE → TXN_SERVICE]: loanId={}, error={}",
                                    loan.getLoanId(), ex.getMessage(), ex);
                        } else {
                            logger.info("Kafka sent [LOAN_DISBURSE → TXN_SERVICE]: loanId={}, amount={}, offset={}",
                                    loan.getLoanId(), loan.getLoanAmount(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to publish loan disbursement transaction event: loanId={}",
                    loan.getLoanId(), e);
        }
    }

    // ═══════════════════════════════════════════════════
    //  Feign Client Helpers
    // ═══════════════════════════════════════════════════

    /**
     * Fetches account details from the Account Service via Feign.
     *
     * @param accountNumber the account number to look up
     * @return the account details
     * @throws ResourceNotFoundException if the account does not exist
     */
    public AccountResponseDTO getAccountDetails(String accountNumber) {
        logger.debug("Fetching account details: accountNumber={}", accountNumber);
        AccountResponseDTO dto = accountFeignService.getAccountByAccountNumber(accountNumber).getBody();
        if (dto == null) {
            logger.error("Account not found: accountNumber={}", accountNumber);
            throw new ResourceNotFoundException("Account not found: " + accountNumber);
        }
        return dto;
    }

    /**
     * Fetches customer details from the Customer Service via Feign.
     *
     * @param customerId the UUID of the customer to look up
     * @return the customer details
     * @throws ResourceNotFoundException if the customer does not exist
     */
    public CustomerDTO getCustomerDetails(UUID customerId) {
        logger.debug("Fetching customer details: customerId={}", customerId);
        CustomerDTO dto = customerFeignService.findById(customerId).getBody();
        if (dto == null) {
            logger.error("Customer not found: customerId={}", customerId);
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        return dto;
    }
}
