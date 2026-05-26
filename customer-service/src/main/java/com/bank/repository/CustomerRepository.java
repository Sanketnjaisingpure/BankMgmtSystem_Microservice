package com.bank.repository;

import com.bank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {


    @Query("SELECT c FROM Customer c WHERE c.email = :email")
    Customer findByEmail(@Param("email") String email);


    @Query("SELECT c FROM Customer c WHERE c.email = :email OR c.phoneNumber = :phoneNumber")
    Customer existByEmailOrPhoneNumber(String email, String phoneNumber);
}
