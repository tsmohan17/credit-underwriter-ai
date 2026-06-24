package com.credit.loan.service;

import com.credit.loan.dto.LoanRequest;
import com.credit.loan.dto.LoanResponse;
import com.credit.loan.model.Customer;
import com.credit.loan.model.LoanApplication;
import com.credit.loan.model.LoanStatus;
import com.credit.loan.repository.CustomerRepository;
import com.credit.loan.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private LoanRequest request;
    private Customer customer;
    private LoanApplication application;

    @BeforeEach
    public void setUp() {
        request = LoanRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("9876543210")
                .panNumber("ABCDE1234F")
                .monthlyIncome(5000.0)
                .existingEmi(500.0)
                .loanAmount(20000.0)
                .tenureMonths(36)
                .build();

        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("9876543210")
                .panNumber("ABCDE1234F")
                .monthlyIncome(5000.0)
                .existingEmi(500.0)
                .build();

        application = LoanApplication.builder()
                .id(100L)
                .customer(customer)
                .loanAmount(20000.0)
                .tenureMonths(36)
                .status(LoanStatus.SUBMITTED)
                .build();
    }

    @Test
    public void testSubmitLoanApplication_NewCustomer() {
        // Mocking repo calls
        when(customerRepository.findByPanNumber(any())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(application);
        when(kafkaTemplate.send(any(String.class), any(), any())).thenReturn(null);

        // Run service
        LoanResponse response = loanApplicationService.submitLoanApplication(request);

        // Verification
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals("ABCDE1234F", response.getPanNumber());
        assertEquals(LoanStatus.SUBMITTED, response.getStatus());

        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(loanApplicationRepository, times(1)).save(any(LoanApplication.class));
        verify(kafkaTemplate, times(1)).send(eq("loan-applications"), eq("100"), any());
    }

    @Test
    public void testGetLoanApplicationById_Success() {
        when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(application));

        LoanResponse response = loanApplicationService.getLoanApplicationById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(LoanStatus.SUBMITTED, response.getStatus());
        verify(loanApplicationRepository, times(1)).findById(100L);
    }

    @Test
    public void testGetLoanApplicationById_NotFound() {
        when(loanApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            loanApplicationService.getLoanApplicationById(999L);
        });

        assertEquals("Loan application not found with id: 999", exception.getMessage());
    }
}
