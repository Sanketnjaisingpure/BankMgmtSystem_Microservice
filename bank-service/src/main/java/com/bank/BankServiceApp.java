package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Bank Service — Spring Boot entry point.
 * Manages bank registration and CRUD operations.
 *
 * <p>Account creation is handled entirely by account-service.
 * Clients pass an optional {@code bankId} in {@code AccountRequestDTO}
 * to link a new account to a registered bank.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.bank")
public class BankServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(BankServiceApp.class, args);
    }
}
