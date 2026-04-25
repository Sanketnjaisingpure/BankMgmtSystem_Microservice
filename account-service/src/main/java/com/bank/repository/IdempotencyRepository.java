package com.bank.repository;

import com.bank.model.IdempotencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRequest, Long> {
    Optional<IdempotencyRequest> findByIdempotencyKey(String key);

}
