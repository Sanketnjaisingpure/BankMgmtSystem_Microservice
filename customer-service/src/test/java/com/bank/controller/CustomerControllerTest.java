package com.bank.controller;

import com.bank.dto.CreateCustomerDTO;
import com.bank.dto.CustomerDTO;
import com.bank.dto.PageResponse;
import com.bank.dto.UpdateCustomerDTO;
import com.bank.exception.DuplicateResourceException;
import com.bank.exception.ResourceNotFoundException;
import com.bank.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDTO customerDTO;
    private CreateCustomerDTO createCustomerDTO;
    private UpdateCustomerDTO updateCustomerDTO;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

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
    }

    // ==========================================
    // GET /find-by-email Tests
    // ==========================================

    @Test
    void findByEmail_Success() throws Exception {
        // Given
        String email = "john.doe@example.com";
        when(customerService.findByEmail(email)).thenReturn(customerDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/find-by-email")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.mobileNumber", is("1234567890")));

        verify(customerService, times(1)).findByEmail(email);
    }

    @Test
    void findByEmail_CustomerNotFound() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        when(customerService.findByEmail(email))
                .thenThrow(new ResourceNotFoundException("Customer not found " + email));

        // When & Then
        mockMvc.perform(get("/api/v1/customers/find-by-email")
                        .param("email", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Customer not found")));

        verify(customerService, times(1)).findByEmail(email);
    }

    @Test
    void findByEmail_MissingParameter() throws Exception {
        // When & Then - No email parameter provided
        mockMvc.perform(get("/api/v1/customers/find-by-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    // ==========================================
    // POST /create-customer Tests
    // ==========================================

    @Test
    void createCustomer_Success() throws Exception {
        // Given
        when(customerService.createCustomer(any(CreateCustomerDTO.class))).thenReturn(customerDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/customers/create-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCustomerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.mobileNumber", is("1234567890")));

        verify(customerService, times(1)).createCustomer(any(CreateCustomerDTO.class));
    }

    @Test
    void createCustomer_DuplicateResource() throws Exception {
        // Given
        when(customerService.createCustomer(any(CreateCustomerDTO.class)))
                .thenThrow(new DuplicateResourceException("john.doe@example.com", "1234567890"));

        // When & Then
        mockMvc.perform(post("/api/v1/customers/create-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCustomerDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));

        verify(customerService, times(1)).createCustomer(any(CreateCustomerDTO.class));
    }

    @Test
    void createCustomer_ValidationError_MissingFirstName() throws Exception {
        // Given - Invalid DTO with missing first name
        CreateCustomerDTO invalidDTO = new CreateCustomerDTO();
        invalidDTO.setLastName("Doe");
        invalidDTO.setEmail("john.doe@example.com");
        invalidDTO.setMobileNumber("1234567890");
        invalidDTO.setPasswordHash("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/customers/create-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void createCustomer_ValidationError_InvalidEmail() throws Exception {
        // Given - Invalid DTO with invalid email format
        CreateCustomerDTO invalidDTO = new CreateCustomerDTO();
        invalidDTO.setFirstName("John");
        invalidDTO.setLastName("Doe");
        invalidDTO.setEmail("invalid-email");
        invalidDTO.setMobileNumber("1234567890");
        invalidDTO.setPasswordHash("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/customers/create-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    void createCustomer_ValidationError_ShortPassword() throws Exception {
        // Given - Invalid DTO with password too short
        CreateCustomerDTO invalidDTO = new CreateCustomerDTO();
        invalidDTO.setFirstName("John");
        invalidDTO.setLastName("Doe");
        invalidDTO.setEmail("john.doe@example.com");
        invalidDTO.setMobileNumber("1234567890");
        invalidDTO.setPasswordHash("12345"); // Less than 6 characters

        // When & Then
        mockMvc.perform(post("/api/v1/customers/create-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void createCustomer_EmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/customers/create-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // ==========================================
    // POST /update-customer Tests
    // ==========================================

    @Test
    void updateCustomer_Success() throws Exception {
        // Given
        CustomerDTO updatedDTO = new CustomerDTO();
        updatedDTO.setFirstName("John");
        updatedDTO.setLastName("Updated");
        updatedDTO.setEmail("john.doe@example.com");
        updatedDTO.setMobileNumber("0987654321");
        updatedDTO.setCreatedAt(LocalDateTime.now());
        updatedDTO.setUpdatedAt(LocalDateTime.now());

        when(customerService.updateCustomer(any(UpdateCustomerDTO.class))).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/customers/update-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCustomerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Updated")))
                .andExpect(jsonPath("$.mobileNumber", is("0987654321")));

        verify(customerService, times(1)).updateCustomer(any(UpdateCustomerDTO.class));
    }

    @Test
    void updateCustomer_CustomerNotFound() throws Exception {
        // Given
        when(customerService.updateCustomer(any(UpdateCustomerDTO.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/customers/update-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCustomerDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));

        verify(customerService, times(1)).updateCustomer(any(UpdateCustomerDTO.class));
    }

    @Test
    void updateCustomer_EmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/customers/update-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk()); // Should process with null/empty values
    }

    // ==========================================
    // DELETE /delete-customer Tests
    // ==========================================

    @Test
    void deleteCustomer_Success() throws Exception {
        // Given
        doNothing().when(customerService).deleteCustomer(customerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/customers/delete-customer")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isNoContent())
                .andExpect(content().string("Customer deleted successfully"));

        verify(customerService, times(1)).deleteCustomer(customerId);
    }

    @Test
    void deleteCustomer_CustomerNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Customer not found"))
                .when(customerService).deleteCustomer(customerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/customers/delete-customer")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));

        verify(customerService, times(1)).deleteCustomer(customerId);
    }

    @Test
    void deleteCustomer_MissingParameter() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/customers/delete-customer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void deleteCustomer_InvalidUUID() throws Exception {
        // When & Then - Invalid UUID format
        mockMvc.perform(delete("/api/v1/customers/delete-customer")
                        .param("customerId", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // ==========================================
    // GET /find-all-customers Tests
    // ==========================================

    @Test
    void findAllCustomers_Success() throws Exception {
        // Given
        CustomerDTO customer1 = new CustomerDTO();
        customer1.setFirstName("John");
        customer1.setLastName("Doe");
        customer1.setEmail("john@example.com");

        CustomerDTO customer2 = new CustomerDTO();
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane@example.com");

        PageResponse<CustomerDTO> pageResponse = new PageResponse<>();
        pageResponse.setData(Arrays.asList(customer1, customer2));
        pageResponse.setPageNumber(0);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);

        when(customerService.findAllCustomers(0, 10, "firstName", "asc")).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/find-all-customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.data[0].firstName", is("John")))
                .andExpect(jsonPath("$.data[1].firstName", is("Jane")));

        verify(customerService, times(1)).findAllCustomers(0, 10, "firstName", "asc");
    }

    @Test
    void findAllCustomers_WithCustomPagination() throws Exception {
        // Given
        PageResponse<CustomerDTO> pageResponse = new PageResponse<>();
        pageResponse.setData(Collections.singletonList(customerDTO));
        pageResponse.setPageNumber(1);
        pageResponse.setTotalElements(20);
        pageResponse.setTotalPages(4);

        when(customerService.findAllCustomers(1, 5, "lastName", "desc")).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/find-all-customers")
                        .param("pageNumber", "1")
                        .param("pageSize", "5")
                        .param("sort", "lastName")
                        .param("order", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalElements", is(20)))
                .andExpect(jsonPath("$.totalPages", is(4)));

        verify(customerService, times(1)).findAllCustomers(1, 5, "lastName", "desc");
    }

    @Test
    void findAllCustomers_EmptyList() throws Exception {
        // Given
        PageResponse<CustomerDTO> pageResponse = new PageResponse<>();
        pageResponse.setData(Collections.emptyList());
        pageResponse.setPageNumber(0);
        pageResponse.setTotalElements(0);
        pageResponse.setTotalPages(0);

        when(customerService.findAllCustomers(0, 10, "firstName", "asc")).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/find-all-customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)));
    }

    @Test
    void findAllCustomers_InvalidPageNumber() throws Exception {
        // When & Then - Negative page number
        mockMvc.perform(get("/api/v1/customers/find-all-customers")
                        .param("pageNumber", "-1"))
                .andExpect(status().isOk()); // Service layer handles this, returns empty or first page
    }

    @Test
    void findAllCustomers_InvalidPageSize() throws Exception {
        // When & Then - Zero page size
        mockMvc.perform(get("/api/v1/customers/find-all-customers")
                        .param("pageSize", "0"))
                .andExpect(status().isOk()); // Service layer handles this
    }

    // ==========================================
    // Invalid Endpoint Tests
    // ==========================================

    @Test
    void invalidEndpoint_Returns404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/customers/invalid-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void invalidMethod_Returns405() throws Exception {
        // When & Then - DELETE on GET endpoint
        mockMvc.perform(delete("/api/v1/customers/find-by-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status", is(405)))
                .andExpect(jsonPath("$.error", is("Method Not Allowed")));
    }
}
