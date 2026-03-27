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
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final MapperConfig mapperConfig;

    public CustomerService(CustomerRepository customerRepository, MapperConfig mapperConfig) {
        this.customerRepository = customerRepository;
        this.mapperConfig = mapperConfig;
    }


    // Handle Customer properly
    public CustomerDTO findByEmail(String email) {

        Customer customer = customerRepository.findByEmail(email);
        if (customer==null){
            throw new ResourceNotFoundException("Customer not found " + email);
        }
        return convertToDTO(customer);
    }

    // find customer by id
    public CustomerDTO findById(UUID customerId) {
        Customer customer =  customerRepository.findById(customerId).orElse(null);
        if (customer==null){
            throw new ResourceNotFoundException("Customer not found");
        }
        return null;
    }


    // Add or Create customer
    public CustomerDTO createCustomer(CreateCustomerDTO createCustomerDTO) {

        Customer customer = convertToEntity(createCustomerDTO);

        if(customerRepository.existByEmailOrMobileNumber(createCustomerDTO.getEmail(), createCustomerDTO.getMobileNumber())!=null){
            throw new DuplicateResourceException(createCustomerDTO.getEmail(), createCustomerDTO.getMobileNumber());
        }

        // Handle exception for email and mobile number
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

         customerRepository.save(customer);

         return convertToDTO(customer);

    }

    // update customer Info

    public CustomerDTO updateCustomer( UpdateCustomerDTO updateCustomerDTO){

        Customer customer = customerRepository.findByEmail(updateCustomerDTO.getEmail());

        if (customer==null){
            throw new ResourceNotFoundException("Customer not found");
        }

        customer.setFirstName(updateCustomerDTO.getFirstName());
        customer.setLastName(updateCustomerDTO.getLastName());
        customer.setMobileNumber(updateCustomerDTO.getMobileNumber());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);

        return  convertToDTO(customer);
    }


    // delete customer

    public void deleteCustomer(UUID customerId) {
        findById(customerId);
        customerRepository.deleteById(customerId);
    }

    // findAll customers
    public PageResponse<CustomerDTO> findAllCustomers(int pageNumber, int pageSize, String sortBy, String order) {
        List<Customer> list = customerRepository.findAll();
        Sort sort = Sort.by(sortBy).ascending();
        if(order.equals("desc")){
            sort = Sort.by(sortBy).descending();
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize,sort);

        Page<Customer> page = customerRepository.findAll(pageable);

        List<CustomerDTO> dtoList = page.getContent().stream().map(this::convertToDTO).toList();

        return new PageResponse<>(dtoList, page.getNumber(), page.getTotalElements(), page.getTotalPages());
    }


    public CustomerDTO convertToDTO(Customer customer) {
        return mapperConfig.modelMapper().map(customer, CustomerDTO.class);
    }

    public Customer convertToEntity(CreateCustomerDTO createCustomerDTO) {
        return mapperConfig.modelMapper().map(createCustomerDTO, Customer.class);
    }

}
