package com.bank.repository;

import com.bank.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankRepository extends JpaRepository<Bank, UUID> {

    Optional<Bank> findByBankCode(String bankCode);

    boolean existsByBankCode(String bankCode);
}
