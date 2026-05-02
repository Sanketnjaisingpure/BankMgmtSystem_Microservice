package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Credit Card Service.
 *
 * <p>Manages the full credit card lifecycle — application, approval,
 * activation, charges, payments, blocking, and closure.</p>
 */
@SpringBootApplication
@EnableFeignClients
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
