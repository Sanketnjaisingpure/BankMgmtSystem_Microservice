package com.bank.repository;

import com.bank.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link CreditCard} entity.
 */
@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    /** Find all credit cards belonging to a customer. */
    List<CreditCard> findByCustomerId(UUID customerId);

    /** Find a credit card by its masked card number. */
    Optional<CreditCard> findByCardNumber(String cardNumber);
}
