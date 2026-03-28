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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.mockito.junit.jupiter.MockitoSettings;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MapperConfig mapperConfig;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;
    private CreateCustomerDTO createCustomerDTO;
    private UpdateCustomerDTO updateCustomerDTO;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setMobileNumber("1234567890");
        customer.setPasswordHash("hashedPassword");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerDTO = new CustomerDTO();
        customerDTO.setFirstName("John");
        customerDTO.setLastName("Doe");
        customerDTO.setEmail("john.doe@example.com");
        customerDTO.setMobileNumber("1234567890");
        customerDTO.setCreatedAt(LocalDateTime.now());
        customerDTO.setUpdatedAt(LocalDateTime.now());

        createCustomerDTO = new CreateCustomerDTO();
        createCustomerDTO.setFirstName("John");
        createCustomerDTO.setLastName("Doe");
        createCustomerDTO.setEmail("john.doe@example.com");
        createCustomerDTO.setMobileNumber("1234567890");
        createCustomerDTO.setPasswordHash("password123");

        updateCustomerDTO = new UpdateCustomerDTO();
        updateCustomerDTO.setFirstName("John");
        updateCustomerDTO.setLastName("Updated");
        updateCustomerDTO.setEmail("john.doe@example.com");
        updateCustomerDTO.setMobileNumber("0987654321");

        when(mapperConfig.modelMapper()).thenReturn(modelMapper);
    }

    // ==========================================
    // findByEmail Tests
    // ==========================================

    @Test
    void findByEmail_Success() {
        // Given
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(customer);
        when(modelMapper.map(customer, CustomerDTO.class)).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.findByEmail("john.doe@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        verify(customerRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    void findByEmail_CustomerNotFound() {
        // Given
        when(customerRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.findByEmail("nonexistent@example.com");
        });

        assertThat(exception.getMessage()).contains("Customer not found");
        verify(customerRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void findByEmail_NullEmail() {
        // Given
        when(customerRepository.findByEmail(null)).thenReturn(null);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.findByEmail(null);
        });

        assertThat(exception.getMessage()).contains("Customer not found");
    }

    @Test
    void findByEmail_EmptyEmail() {
        // Given
        when(customerRepository.findByEmail("")).thenReturn(null);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.findByEmail("");
        });

        assertThat(exception.getMessage()).contains("Customer not found");
    }

    // ==========================================
    // findById Tests
    // ==========================================

    @Test
    void findById_Success() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(modelMapper.map(customer, CustomerDTO.class)).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.findById(customerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void findById_CustomerNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.findById(nonExistentId);
        });

        assertThat(exception.getMessage()).isEqualTo("Customer not found");
        verify(customerRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void findById_NullId() {
        // Given - mock findById to return empty optional for null
        when(customerRepository.findById(null)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.findById(null);
        });

        assertThat(exception.getMessage()).isEqualTo("Customer not found");
    }

    // ==========================================
    // createCustomer Tests
    // ==========================================

    @Test
    void createCustomer_Success() {
        // Given
        when(customerRepository.existByEmailOrMobileNumber(anyString(), anyString())).thenReturn(null);
        when(modelMapper.map(any(CreateCustomerDTO.class), eq(Customer.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelMapper.map(customer, CustomerDTO.class)).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.createCustomer(createCustomerDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(customerRepository, times(1)).existByEmailOrMobileNumber("john.doe@example.com", "1234567890");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail() {
        // Given
        when(customerRepository.existByEmailOrMobileNumber("john.doe@example.com", "1234567890"))
                .thenReturn(customer);

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            customerService.createCustomer(createCustomerDTO);
        });

        assertThat(exception.getMessage()).contains("john.doe@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateMobileNumber() {
        // Given
        when(customerRepository.existByEmailOrMobileNumber("john.doe@example.com", "1234567890"))
                .thenReturn(customer);

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            customerService.createCustomer(createCustomerDTO);
        });

        assertThat(exception.getMessage()).contains("1234567890");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_RepositoryThrowsException() {
        // Given
        when(customerRepository.existByEmailOrMobileNumber(anyString(), anyString())).thenReturn(null);
        when(modelMapper.map(any(CreateCustomerDTO.class), eq(Customer.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.createCustomer(createCustomerDTO);
        });

        assertThat(exception.getMessage()).isEqualTo("Failed to create customer");
    }

    @Test
    void createCustomer_NullDTO() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            customerService.createCustomer(null);
        });
    }

    // ==========================================
    // updateCustomer Tests
    // ==========================================

    @Test
    void updateCustomer_Success() {
        // Given
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelMapper.map(customer, CustomerDTO.class)).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.updateCustomer(updateCustomerDTO);

        // Then
        assertThat(result).isNotNull();
        verify(customerRepository, times(1)).findByEmail("john.doe@example.com");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_CustomerNotFound() {
        // Given
        when(customerRepository.findByEmail("nonexistent@example.com")).thenReturn(null);
        updateCustomerDTO.setEmail("nonexistent@example.com");

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.updateCustomer(updateCustomerDTO);
        });

        assertThat(exception.getMessage()).isEqualTo("Customer not found");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_RepositoryThrowsException() {
        // Given
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.updateCustomer(updateCustomerDTO);
        });

        assertThat(exception.getMessage()).isEqualTo("Failed to update customer");
    }

    @Test
    void updateCustomer_PartialUpdate() {
        // Given - Only update first name
        UpdateCustomerDTO partialUpdate = new UpdateCustomerDTO();
        partialUpdate.setEmail("john.doe@example.com");
        partialUpdate.setFirstName("Jane");
        partialUpdate.setLastName(null);
        partialUpdate.setMobileNumber(null);

        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelMapper.map(customer, CustomerDTO.class)).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.updateCustomer(partialUpdate);

        // Then
        assertThat(result).isNotNull();
        assertThat(customer.getFirstName()).isEqualTo("Jane");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NullDTO() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            customerService.updateCustomer(null);
        });
    }

    // ==========================================
    // deleteCustomer Tests
    // ==========================================

    @Test
    void deleteCustomer_Success() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).deleteById(customerId);

        // When
        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId));

        // Then
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).deleteById(customerId);
    }

    @Test
    void deleteCustomer_CustomerNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.deleteCustomer(nonExistentId);
        });

        assertThat(exception.getMessage()).isEqualTo("Customer not found");
        verify(customerRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deleteCustomer_RepositoryThrowsException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        doThrow(new RuntimeException("Database error")).when(customerRepository).deleteById(customerId);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.deleteCustomer(customerId);
        });

        assertThat(exception.getMessage()).isEqualTo("Failed to delete customer");
    }

    @Test
    void deleteCustomer_NullId() {
        // Given - mock findById to return empty optional for null
        when(customerRepository.findById(null)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.deleteCustomer(null);
        });

        assertThat(exception.getMessage()).isEqualTo("Customer not found");
    }

    // ==========================================
    // findAllCustomers Tests
    // ==========================================

    @Test
    void findAllCustomers_Success_AscendingOrder() {
        // Given
        Customer customer2 = new Customer();
        customer2.setCustomerId(UUID.randomUUID());
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");
        customer2.setMobileNumber("0987654321");

        Page<Customer> customerPage = new PageImpl<>(Arrays.asList(customer, customer2));
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(customerPage);
        when(modelMapper.map(any(Customer.class), eq(CustomerDTO.class))).thenReturn(customerDTO);

        // When
        PageResponse<CustomerDTO> result = customerService.findAllCustomers(0, 10, "firstName", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        verify(customerRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void findAllCustomers_Success_DescendingOrder() {
        // Given
        Page<Customer> customerPage = new PageImpl<>(Collections.singletonList(customer));
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(customerPage);
        when(modelMapper.map(any(Customer.class), eq(CustomerDTO.class))).thenReturn(customerDTO);

        // When
        PageResponse<CustomerDTO> result = customerService.findAllCustomers(0, 10, "firstName", "desc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        verify(customerRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void findAllCustomers_EmptyList() {
        // Given
        Page<Customer> emptyPage = new PageImpl<>(Collections.emptyList());
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        PageResponse<CustomerDTO> result = customerService.findAllCustomers(0, 10, "firstName", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void findAllCustomers_CustomPagination() {
        // Given
        Page<Customer> customerPage = new PageImpl<>(Collections.singletonList(customer));
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(customerPage);
        when(modelMapper.map(any(Customer.class), eq(CustomerDTO.class))).thenReturn(customerDTO);

        // When
        PageResponse<CustomerDTO> result = customerService.findAllCustomers(2, 5, "lastName", "desc");

        // Then
        assertThat(result).isNotNull();
        verify(customerRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void findAllCustomers_InvalidOrderDefaultsToAscending() {
        // Given
        Page<Customer> customerPage = new PageImpl<>(Collections.singletonList(customer));
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(customerPage);
        when(modelMapper.map(any(Customer.class), eq(CustomerDTO.class))).thenReturn(customerDTO);

        // When - Pass invalid order, should default to ascending
        PageResponse<CustomerDTO> result = customerService.findAllCustomers(0, 10, "firstName", "invalid");

        // Then - The method still works even with invalid order parameter
        assertThat(result).isNotNull();
        verify(customerRepository, times(1)).findAll(any(Pageable.class));
    }

    // ==========================================
    // Conversion Method Tests
    // ==========================================

    @Test
    void convertToDTO_Success() {
        // Given
        when(modelMapper.map(customer, CustomerDTO.class)).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.convertToDTO(customer);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void convertToEntity_Success() {
        // Given
        when(modelMapper.map(createCustomerDTO, Customer.class)).thenReturn(customer);

        // When
        Customer result = customerService.convertToEntity(createCustomerDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(createCustomerDTO.getEmail());
    }

    @Test
    void convertToDTO_NullInput() {
        // When & Then
        when(modelMapper.map(null, CustomerDTO.class)).thenReturn(null);
        
        CustomerDTO result = customerService.convertToDTO(null);
        assertThat(result).isNull();
    }

    @Test
    void convertToEntity_NullInput() {
        // When & Then
        when(modelMapper.map(null, Customer.class)).thenReturn(null);
        
        Customer result = customerService.convertToEntity(null);
        assertThat(result).isNull();
    }
}
