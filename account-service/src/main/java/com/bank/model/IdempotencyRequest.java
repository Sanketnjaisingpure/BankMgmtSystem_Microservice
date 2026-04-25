package com.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "idempotency_request",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotencyKey"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    private String idempotencyKey;
    private String accountNumber;
    private String status; // IN_PROGRESS, COMPLETED
}