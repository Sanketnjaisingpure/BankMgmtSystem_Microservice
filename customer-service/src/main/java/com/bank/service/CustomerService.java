package com.bank.service;

import com.bank.config.MapperConfig;
import com.bank.dto.CreateCustomerDTO;
import com.bank.dto.CustomerDTO;
import com.bank.dto.PageResponse;
import com.bank.dto.UpdateCustomerDTO;
import com.bank.exception.DuplicateResourceException;
import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Customer;
import com.bank.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final MapperConfig mapperConfig;

    public CustomerService(CustomerRepository customerRepository, MapperConfig mapperConfig) {
        this.customerRepository = customerRepository;
        this.mapperConfig = mapperConfig;
    }

// use logger method
    // Handle Customer properly
    public CustomerDTO findByEmail(String email) {
        logger.info("Fetching customer by email: {}", email);
        Customer customer = customerRepository.findByEmail(email);
        if (customer==null){
            logger.error("Customer not found for email: {}", email);
            throw new ResourceNotFoundException("Customer not found " + email);
        }
        logger.info("Customer found for email: {}", email);
        return convertToDTO(customer);
    }

    // find customer by id
    public CustomerDTO findById(UUID customerId) {
        logger.info("Thread: {}, isVirtual: {}",
                Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());
        logger.info("Fetching customer by id: {}", customerId);
        Customer customer =  customerRepository.findById(customerId).orElse(null);
        if (customer==null){
            logger.error("Customer not found for id: {}", customerId);
            throw new ResourceNotFoundException("Customer not found");
        }
        logger.info("Customer found for id: {}", customerId);
        return convertToDTO(customer);
    }


    // Add or Create customer
    public CustomerDTO createCustomer(CreateCustomerDTO createCustomerDTO) {
        logger.info("Creating customer with | Mobile number {} | email {}" , createCustomerDTO.getMobileNumber() , createCustomerDTO.getEmail());
        Customer customer = convertToEntity(createCustomerDTO);

        if(customerRepository.existByEmailOrMobileNumber(createCustomerDTO.getEmail(), createCustomerDTO.getMobileNumber())!=null){
            logger.error("Customer already exist  with Mobile number {} or email {} " , createCustomerDTO.getMobileNumber() , createCustomerDTO.getEmail());
            throw new DuplicateResourceException(createCustomerDTO.getEmail(), createCustomerDTO.getMobileNumber());
        }

        // Handle exception for email and mobile number
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        try {
            customerRepository.save(customer);
            logger.info("Customer created successfully with | Id {} " ,customer.getCustomerId());
        }
        catch (Exception ex){
            logger.error("Failed to create customer with | Mobile number {} | email {}" , createCustomerDTO.getMobileNumber() , createCustomerDTO.getEmail());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create customer");
        }
         return convertToDTO(customer);

    }

    // update customer Info

    public CustomerDTO updateCustomer(UpdateCustomerDTO updateCustomerDTO) {
        logger.info("Updating customer: email={}", updateCustomerDTO.getEmail());
        Customer customer = customerRepository.findByEmail(updateCustomerDTO.getEmail());

        if (customer == null) {
            logger.warn("Update failed — customer not found: email={}", updateCustomerDTO.getEmail());
            throw new ResourceNotFoundException("Customer not found");
        }

        logger.debug("Customer found for update: customerId={}, email={}", customer.getCustomerId(), updateCustomerDTO.getEmail());

        try {
            customer.setFirstName(updateCustomerDTO.getFirstName());
            customer.setLastName(updateCustomerDTO.getLastName());
            customer.setMobileNumber(updateCustomerDTO.getMobileNumber());
            customer.setUpdatedAt(LocalDateTime.now());
            customerRepository.save(customer);
            logger.info("Customer updated successfully: customerId={}, mobileNumber={}, email={}",
                    customer.getCustomerId(), updateCustomerDTO.getMobileNumber(), updateCustomerDTO.getEmail());
        }
        catch (Exception ex) {
            logger.error("Failed to update customer: mobileNumber={}, email={}",
                    updateCustomerDTO.getMobileNumber(), updateCustomerDTO.getEmail(), ex);
            throw new RuntimeException("Failed to update customer");
        }

        return convertToDTO(customer);
    }


    // delete customer

    public void deleteCustomer(UUID customerId) {
        logger.info("Deleting customer: customerId={}", customerId);
        findById(customerId);
        try {
            customerRepository.deleteById(customerId);
            logger.info("Customer deleted successfully: customerId={}", customerId);
        }
        catch (Exception ex) {
            logger.error("Failed to delete customer: customerId={}", customerId, ex);
            throw new RuntimeException("Failed to delete customer");
        }
    }

    // findAll customers
    public PageResponse<CustomerDTO> findAllCustomers(int pageNumber, int pageSize, String sortBy, String order) {
        logger.info("Fetching all customers: page={}, size={}, sortBy={}, order={}", pageNumber, pageSize, sortBy, order);
        Sort sort = Sort.by(sortBy).ascending();
        if (order.equals("desc")) {
            sort = Sort.by(sortBy).descending();
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Customer> page = customerRepository.findAll(pageable);

        List<CustomerDTO> dtoList = page.getContent().stream().map(this::convertToDTO).toList();

        logger.info("Fetched {} customers (page {}/{}, totalRecords={})",
                dtoList.size(), page.getNumber() + 1, page.getTotalPages(), page.getTotalElements());

        return new PageResponse<>(dtoList, page.getNumber(), page.getTotalElements(), page.getTotalPages());
    }


    public CustomerDTO convertToDTO(Customer customer) {
        return mapperConfig.modelMapper().map(customer, CustomerDTO.class);
    }

    public Customer convertToEntity(CreateCustomerDTO createCustomerDTO) {
        return mapperConfig.modelMapper().map(createCustomerDTO, Customer.class);
    }

}
