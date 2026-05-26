package com.bank.service;

import com.bank.ENUM.BankStatus;
import com.bank.dto.BankRequestDTO;
import com.bank.dto.BankResponseDTO;
import com.bank.event.BankRegistrationEvent;
import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Bank;
import com.bank.repository.BankRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.bank.config.KafkaConstants.BANK_REGISTRATION_TOPIC;

/**
 * Core service for bank-service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Register new banks</li>
 *   <li>Retrieve bank details (by ID or code)</li>
 *   <li>Update bank status (ACTIVE / SUSPENDED / CLOSED)</li>
 *   <li>Delete a bank record</li>
 *   <li>Publish Kafka events on registration</li>
 * </ul>
 *
 * <p><b>Note on account creation:</b> Account opening lives entirely in
 * {@code account-service}. Clients call {@code POST /api/v1/accounts/create-account}
 * directly, passing the optional {@code bankId} field in {@code AccountRequestDTO}
 * to link the account to a registered bank.
 */
@Service
public class BankService {

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);



    private final BankRepository bankRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BankService(BankRepository bankRepository,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.bankRepository = bankRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ═══════════════════════════════════════════════════
    //  Register Bank
    // ═══════════════════════════════════════════════════

    /**
     * Registers a new bank. Validates that the bank code is unique.
     * Publishes a {@code bank-registration-topic} event after saving.
     */
    @Transactional
    public BankResponseDTO registerBank(BankRequestDTO request) {
        logger.info("registerBank started: bankName={}, bankCode={}", request.bankName(), request.bankCode());

        if (bankRepository.existsByBankCode(request.bankCode().toUpperCase())) {
            logger.warn("Duplicate bank code: bankCode={}", request.bankCode());
            throw new IllegalArgumentException("A bank with code '" + request.bankCode() + "' already exists.");
        }

        Bank bank = new Bank();
        bank.setBankName(request.bankName());
        bank.setBankCode(request.bankCode().toUpperCase());
        bank.setHeadquartersCity(request.headquartersCity());
        bank.setIfscPrefix(request.ifscPrefix().toUpperCase());
        bank.setContactEmail(request.contactEmail());
        bank.setContactPhone(request.contactPhone());
        bank.setBankStatus(BankStatus.ACTIVE);
        bank.setCreatedAt(LocalDateTime.now());
        bank.setUpdatedAt(LocalDateTime.now());

        bankRepository.save(bank);
        logger.info("Bank registered: bankId={}, bankCode={}", bank.getBankId(), bank.getBankCode());

        publishRegistrationEvent(bank);

        return toResponseDTO(bank);
    }

    // ═══════════════════════════════════════════════════
    //  Get by ID
    // ═══════════════════════════════════════════════════

    public BankResponseDTO getBankById(UUID bankId) {
        logger.info("getBankById: bankId={}", bankId);
        return toResponseDTO(fetchBank(bankId));
    }

    // ═══════════════════════════════════════════════════
    //  Get by Code
    // ═══════════════════════════════════════════════════

    public BankResponseDTO getBankByCode(String bankCode) {
        logger.info("getBankByCode: bankCode={}", bankCode);
        Bank bank = bankRepository.findByBankCode(bankCode.toUpperCase())
                .orElseThrow(() -> {
                    logger.warn("Bank not found: bankCode={}", bankCode);
                    return new ResourceNotFoundException("Bank not found with code: " + bankCode);
                });
        return toResponseDTO(bank);
    }

    // ═══════════════════════════════════════════════════
    //  Get All
    // ═══════════════════════════════════════════════════

    public List<BankResponseDTO> getAllBanks() {
        logger.info("getAllBanks called");
        return bankRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    // ═══════════════════════════════════════════════════
    //  Update Status
    // ═══════════════════════════════════════════════════

    @Transactional
    public BankResponseDTO updateBankStatus(UUID bankId, BankStatus newStatus) {
        logger.info("updateBankStatus: bankId={}, newStatus={}", bankId, newStatus);
        Bank bank = fetchBank(bankId);
        bank.setBankStatus(newStatus);
        bank.setUpdatedAt(LocalDateTime.now());
        bankRepository.save(bank);
        logger.info("Bank status updated: bankId={}, status={}", bankId, newStatus);
        return toResponseDTO(bank);
    }

    // ═══════════════════════════════════════════════════
    //  Delete
    // ═══════════════════════════════════════════════════

    @Transactional
    public void deleteBank(UUID bankId) {
        logger.info("deleteBank: bankId={}", bankId);
        fetchBank(bankId); // throws 404 if not found
        bankRepository.deleteById(bankId);
        logger.info("Bank deleted: bankId={}", bankId);
    }

    // ═══════════════════════════════════════════════════
    //  Private Helpers
    // ═══════════════════════════════════════════════════

    private Bank fetchBank(UUID bankId) {
        return bankRepository.findById(bankId)
                .orElseThrow(() -> {
                    logger.warn("Bank not found: bankId={}", bankId);
                    return new ResourceNotFoundException("Bank not found with ID: " + bankId);
                });
    }

    private BankResponseDTO toResponseDTO(Bank bank) {
        return new BankResponseDTO(
                bank.getBankId(),
                bank.getBankName(),
                bank.getBankCode(),
                bank.getHeadquartersCity(),
                bank.getIfscPrefix(),
                bank.getContactEmail(),
                bank.getContactPhone(),
                bank.getBankStatus(),
                bank.getCreatedAt(),
                bank.getUpdatedAt()
        );
    }

    // ═══════════════════════════════════════════════════
    //  Kafka (non-critical)
    // ═══════════════════════════════════════════════════

    private void publishRegistrationEvent(Bank bank) {
        try {
            BankRegistrationEvent event = new BankRegistrationEvent();
            event.setBankId(bank.getBankId());
            event.setBankName(bank.getBankName());
            event.setBankCode(bank.getBankCode());
            event.setBankStatus(bank.getBankStatus().name());
            event.setMessage("Bank '" + bank.getBankName() + "' has been registered successfully.");

            // ── Notification-specific fields ──
            event.setSourceService("BANK_SERVICE");
            event.setNotificationType("BANK_REGISTERED");
            event.setSubject("New Bank Registered");
            event.setReferenceId(bank.getBankId().toString());
            event.setMetadata(String.format(
                    "{\"bankId\":\"%s\",\"bankName\":\"%s\",\"bankCode\":\"%s\",\"bankStatus\":\"%s\"}",
                    bank.getBankId(), bank.getBankName(), bank.getBankCode(), bank.getBankStatus()
            ));

            kafkaTemplate.send(BANK_REGISTRATION_TOPIC, bank.getBankId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Kafka failed [BANK_REGISTRATION]: bankId={}", bank.getBankId(), ex);
                        } else {
                            logger.info("Kafka sent [BANK_REGISTRATION]: bankId={}, offset={}",
                                    bank.getBankId(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to publish bank registration event: bankId={}", bank.getBankId(), e);
        }
    }
}
