package com.bank.model;

import com.bank.ENUM.BankStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a registered bank in the system.
 * Tracks identity, contact info, IFSC prefix, and operational status.
 */
@Entity
@Table(name = "bank")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bankId;

    /** Full legal name of the bank (e.g., "State Bank of India"). */
    @Column(nullable = false)
    @NotNull
    private String bankName;

    /** Unique short code for the bank (e.g., "SBIN"). */
    @Column(unique = true, nullable = false)
    @NotNull
    private String bankCode;

    /** City where the bank's headquarters is located. */
    @Column(nullable = false)
    @NotNull
    private String headquartersCity;

    /**
     * First 4 characters of all IFSC codes issued under this bank.
     * For example, "SBIN" is the prefix for all SBI branches.
     * Full IFSC = ifscPrefix + "0" + branchCode (11 chars total).
     */
    @Column(nullable = false, length = 4)
    @NotNull
    private String ifscPrefix;

    @Column(nullable = false)
    @NotNull
    private String contactEmail;

    @Column(nullable = false)
    @NotNull
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private BankStatus bankStatus;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;
}
