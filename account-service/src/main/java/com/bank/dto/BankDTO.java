package com.bank.dto;

import lombok.*;

import java.util.UUID;

/**
 * Minimal mirror of BankResponseDTO from bank-service.
 * Used by account-service's BankFeignService to deserialize
 * the GET /api/v1/banks/{bankId} response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDTO {

    private UUID bankId;
    private String bankName;
    private String bankCode;
    private String headquartersCity;
    private String ifscPrefix;
    private String bankStatus;   // "ACTIVE", "SUSPENDED", "CLOSED"
}
