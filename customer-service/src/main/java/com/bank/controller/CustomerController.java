package com.bank.controller;

import com.bank.dto.CreateCustomerDTO;
import com.bank.dto.CustomerDTO;
import com.bank.dto.PageResponse;
import com.bank.dto.UpdateCustomerDTO;
import com.bank.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/find-by-email")
    public ResponseEntity<CustomerDTO> findByEmail(@RequestParam String email) {
        logger.info("Received request to fetch customer with email : {} ",email);
        CustomerDTO dto = customerService.findByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PostMapping("/create-customer")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody @Valid CreateCustomerDTO customerDTO) {
        logger.info("Received request to create customer with mobile number : {} and email : {} ",customerDTO.getMobileNumber(), customerDTO.getEmail());
        CustomerDTO dto = customerService.createCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }


    @PostMapping("/update-customer")
    public ResponseEntity<CustomerDTO> updateCustomer(@RequestBody UpdateCustomerDTO customerDTO) {
        logger.info("Received request to update customer with mobile number : {} and email : {} ",customerDTO.getMobileNumber(), customerDTO.getEmail());
        CustomerDTO dto = customerService.updateCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @DeleteMapping("/delete-customer")
    public ResponseEntity<String> deleteCustomer(@RequestParam UUID customerId) {
        logger.info("Received request to delete customer with id : {} ",customerId);
       customerService.deleteCustomer(customerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Customer deleted successfully");
    }

    @GetMapping("/find-all-customers")
    public ResponseEntity<PageResponse<CustomerDTO>> findAllCustomers(
            @RequestParam(value = "pageNumber" ,defaultValue = "0") int pageNumber ,
            @RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize,
            @RequestParam(value = "sort" ,defaultValue = "firstName") String sort,
            @RequestParam(value = "order" ,defaultValue = "asc") String order) {
        logger.info("Received request to fetch all customers");
        PageResponse<CustomerDTO> dto = customerService.findAllCustomers(pageNumber, pageSize , sort , order);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }


}
