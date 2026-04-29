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
import com.bank.exception.ResourceNotFoundException;
import com.bank.feign.AccountFeignService;
import com.bank.feign.customers.CustomerFeignService;
import com.bank.model.Loan;
import com.bank.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final AccountFeignService accountFeignService;
    private final CustomerFeignService customerFeignService;
    private final LoanRepository loanRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

    // ─────────────────────────────────────────────────
    // Apply Loan
    // ─────────────────────────────────────────────────

    @Transactional
    public LoanResponseDTO applyLoan(LoanRequestDTO loanRequestDTO) {
        logger.info("applyLoan started: customerId={}, accountNumber={}, amount={}",
                loanRequestDTO.customerId(), loanRequestDTO.accountNumber(), loanRequestDTO.loanAmount());

        // Validate customer
        CustomerDTO customer = getCustomerDetails(loanRequestDTO.customerId());
        logger.info("Customer fetched: customerId={}, name={} {}",
                loanRequestDTO.customerId(), customer.getFirstName(), customer.getLastName());

        // Validate account
        AccountResponseDTO account = getAccountDetails(loanRequestDTO.accountNumber());
        logger.info("Account fetched: accountNumber={}, customerId={}",
                loanRequestDTO.accountNumber(), account.getCustomerId());

        // Cross-validate ownership
        if (!account.getCustomerId().equals(loanRequestDTO.customerId())) {
            logger.error("Ownership mismatch: requestCustomerId={} vs accountOwnerId={}",
                    loanRequestDTO.customerId(), account.getCustomerId());
            throw new IllegalArgumentException("Customer ID does not match the Account owner");
        }
        logger.info("Ownership validated: customerId={}", loanRequestDTO.customerId());

        // Build loan
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

        // EMI calculation deferred to approval — set null for now
        logger.debug("Loan initialized: loanId={}, interestRate={}", loan.getLoanId(), loan.getInterestRate());

        // Save (any DB error will propagate naturally - no swallowing)
        loanRepository.save(loan);
        logger.info("Loan saved: loanId={}", loan.getLoanId());

        // Kafka — non-critical, isolated try-catch so DB transaction is not affected
        sendApplicationEvent(loan, loanRequestDTO.loanAmount());

        return toResponseDTO(loan);
    }

    // ─────────────────────────────────────────────────
    // Approve Loan  (EMI calculated here)
    // ─────────────────────────────────────────────────

    @Transactional
    public LoanResponseDTO approveLoan(UUID loanId) {
        logger.info("approveLoan started: loanId={}", loanId);

        Loan loan = fetchLoan(loanId);
        validateStatus(loan, LoanStatus.PENDING, "approve");

        // Calculate and set EMI at approval time
        BigDecimal emi = calculateEMI(
                loan.getLoanAmount(),
                BigDecimal.valueOf(loan.getInterestRate()),
                loan.getTenureMonths()
        );
        loan.setEmiAmount(emi);
        loan.setLoanStatus(LoanStatus.APPROVED);
        loan.setUpdatedAt(LocalDateTime.now());
        logger.info("Loan approved: loanId={}, emiAmount={}", loanId, emi);

        loanRepository.save(loan);

        // Kafka — non-critical
        sendStatusEvent(loan, LoanStatus.APPROVED,
                "Your loan with ID " + loanId + " has been APPROVED. EMI: " + emi);

        return toResponseDTO(loan);
    }

    // ─────────────────────────────────────────────────
    // Reject Loan
    // ─────────────────────────────────────────────────

    @Transactional
    public LoanResponseDTO rejectLoan(UUID loanId) {
        logger.info("rejectLoan started: loanId={}", loanId);

        Loan loan = fetchLoan(loanId);
        validateStatus(loan, LoanStatus.PENDING, "reject");

        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setUpdatedAt(LocalDateTime.now());

        loanRepository.save(loan);
        logger.info("Loan rejected: loanId={}", loanId);

        sendStatusEvent(loan, LoanStatus.REJECTED,
                "Your loan application with ID " + loanId + " has been REJECTED.");

        return toResponseDTO(loan);
    }

    // ─────────────────────────────────────────────────
    // Disburse Loan
    // ─────────────────────────────────────────────────

    @Transactional
    public LoanResponseDTO disburseLoan(UUID loanId) {
        logger.info("disburseLoan started: loanId={}", loanId);

        Loan loan = fetchLoan(loanId);
        validateStatus(loan, LoanStatus.APPROVED, "disburse");

        if (loan.getEmiAmount() == null || loan.getEmiAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Disbursement safety check failed: emiAmount is missing or zero for loanId={}", loanId);
            throw new IllegalStateException("Loan cannot be disbursed: EMI amount not computed. Please re-approve the loan.");
        }

        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setUpdatedAt(LocalDateTime.now());

        loanRepository.save(loan);
        logger.info("Loan disbursed and set ACTIVE: loanId={}, amount={}", loanId, loan.getLoanAmount());

        sendDisbursementEvent(loan);

        return toResponseDTO(loan);
    }

    // ─────────────────────────────────────────────────
    // Get Loan
    // ─────────────────────────────────────────────────

    public LoanResponseDTO getLoan(UUID loanId) {
        logger.info("getLoan: loanId={}", loanId);
        Loan loan = fetchLoan(loanId);
        return toResponseDTO(loan);
    }

    // ─────────────────────────────────────────────────
    // EMI Calculation
    // ─────────────────────────────────────────────────

    public BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, int months) {
        logger.debug("calculateEMI: principal={}, annualRate={}, months={}", principal, annualRate, months);

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
            emi = principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
            BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate).pow(months);
            BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
            BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
            emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        }

        logger.debug("calculateEMI result: emi={}", emi);
        return emi;
    }

    // ─────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────

    private Loan fetchLoan(UUID loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.error("Loan not found: loanId={}", loanId);
                    return new ResourceNotFoundException("Loan not found with ID: " + loanId);
                });
    }

    private void validateStatus(Loan loan, LoanStatus expected, String action) {
        if (loan.getLoanStatus() != expected) {
            logger.warn("Cannot {} loan: loanId={}, currentStatus={}, expectedStatus={}",
                    action, loan.getLoanId(), loan.getLoanStatus(), expected);
            throw new IllegalStateException(
                    "Cannot " + action + " loan. Expected status: " + expected
                    + ", current: " + loan.getLoanStatus());
        }
    }

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

    // ─────────────────────────────────────────────────
    // Kafka Helpers (non-critical — isolated from @Transactional)
    // ─────────────────────────────────────────────────

    private void sendApplicationEvent(Loan loan, BigDecimal amount) {
        try {
            LoanApplicationEvent event = new LoanApplicationEvent();
            event.setCustomerId(loan.getCustomerId());
            event.setAccountNumber(loan.getAccountNumber());
            event.setLoanAmount(amount);
            event.setMessage("Loan application for amount " + amount + " submitted successfully. Loan ID: " + loan.getLoanId());

            kafkaTemplate.send(KafkaConstants.LOAN_APPLICATION_TOPIC, loan.getCustomerId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Kafka send failed [LOAN_APPLICATION]: loanId={}, error={}", loan.getLoanId(), ex.getMessage(), ex);
                        } else {
                            logger.info("Kafka sent [LOAN_APPLICATION]: loanId={}, offset={}", loan.getLoanId(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to publish loan application event: loanId={}", loan.getLoanId(), e);
        }
    }

    private void sendStatusEvent(Loan loan, LoanStatus status, String message) {
        try {
            LoanStatusEvent event = new LoanStatusEvent(loan.getLoanId(), loan.getCustomerId(), status, message);
            kafkaTemplate.send(KafkaConstants.LOAN_STATUS_TOPIC, loan.getCustomerId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Kafka send failed [LOAN_STATUS]: loanId={}, status={}, error={}", loan.getLoanId(), status, ex.getMessage(), ex);
                        } else {
                            logger.info("Kafka sent [LOAN_STATUS]: loanId={}, status={}, offset={}", loan.getLoanId(), status, result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to publish loan status event: loanId={}, status={}", loan.getLoanId(), status, e);
        }
    }

    private void sendDisbursementEvent(Loan loan) {
        try {
            LoanDisbursementEvent event = new LoanDisbursementEvent(
                    loan.getLoanId(),
                    loan.getCustomerId(),
                    loan.getAccountNumber(),
                    loan.getLoanAmount(),
                    "Your loan with ID " + loan.getLoanId() + " has been DISBURSED. Amount: " + loan.getLoanAmount()
            );
            kafkaTemplate.send(KafkaConstants.LOAN_DISBURSEMENT_TOPIC, loan.getCustomerId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Kafka send failed [LOAN_DISBURSEMENT]: loanId={}, error={}", loan.getLoanId(), ex.getMessage(), ex);
                        } else {
                            logger.info("Kafka sent [LOAN_DISBURSEMENT]: loanId={}, offset={}", loan.getLoanId(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to publish loan disbursement event: loanId={}", loan.getLoanId(), e);
        }
    }

    // ─────────────────────────────────────────────────
    // Feign helpers
    // ─────────────────────────────────────────────────

    public AccountResponseDTO getAccountDetails(String accountNumber) {
        AccountResponseDTO dto = accountFeignService.getAccountByAccountNumber(accountNumber).getBody();
        if (dto == null) {
            logger.error("Account not found: accountNumber={}", accountNumber);
            throw new ResourceNotFoundException("Account not found: " + accountNumber);
        }
        logger.info("Account fetched: accountNumber={}", accountNumber);
        return dto;
    }

    public CustomerDTO getCustomerDetails(UUID customerId) {
        CustomerDTO dto = customerFeignService.findById(customerId).getBody();
        if (dto == null) {
            logger.error("Customer not found: customerId={}", customerId);
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        logger.info("Customer fetched: customerId={}", customerId);
        return dto;
    }
}
