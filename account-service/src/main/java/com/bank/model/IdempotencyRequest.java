package com.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "idempotency_request",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key") , schema = "account_db")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyRequest {

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    private String accountNumber;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    private String status; // IN_PROGRESS, COMPLETED
}