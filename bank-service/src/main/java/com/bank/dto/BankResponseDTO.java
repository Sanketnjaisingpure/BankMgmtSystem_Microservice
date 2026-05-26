package com.bank.dto;

import com.bank.ENUM.BankStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for bank details.
 */
public record BankResponseDTO(
        UUID bankId,
        String bankName,
        String bankCode,
        String headquartersCity,
        String ifscPrefix,
        String contactEmail,
        String contactPhone,
        BankStatus bankStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
