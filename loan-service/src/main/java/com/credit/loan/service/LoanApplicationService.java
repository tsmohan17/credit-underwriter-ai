package com.credit.loan.service;

import com.credit.loan.config.KafkaConfig;
import com.credit.loan.dto.LoanRequest;
import com.credit.loan.dto.LoanResponse;
import com.credit.loan.event.LoanSubmittedEvent;
import com.credit.loan.model.Customer;
import com.credit.loan.model.LoanApplication;
import com.credit.loan.model.LoanStatus;
import com.credit.loan.repository.CustomerRepository;
import com.credit.loan.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public LoanApplicationService(LoanApplicationRepository loanApplicationRepository, 
                                  CustomerRepository customerRepository,
                                  KafkaTemplate<String, Object> kafkaTemplate) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public LoanResponse submitLoanApplication(LoanRequest request) {
        // Find existing customer or create a new one
        Customer customer = customerRepository.findByPanNumber(request.getPanNumber())
                .orElseGet(() -> Customer.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .panNumber(request.getPanNumber())
                        .monthlyIncome(request.getMonthlyIncome())
                        .existingEmi(request.getExistingEmi())
                        .build());

        // Update customer details if they've changed
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setExistingEmi(request.getExistingEmi());
        customer = customerRepository.save(customer);

        // Build application
        LoanApplication application = LoanApplication.builder()
                .customer(customer)
                .loanAmount(request.getLoanAmount())
                .tenureMonths(request.getTenureMonths())
                .status(LoanStatus.SUBMITTED)
                .build();

        application = loanApplicationRepository.save(application);

        // Build and publish Kafka event
        LoanSubmittedEvent event = LoanSubmittedEvent.builder()
                .applicationId(application.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .panNumber(customer.getPanNumber())
                .monthlyIncome(customer.getMonthlyIncome())
                .existingEmi(customer.getExistingEmi())
                .loanAmount(application.getLoanAmount())
                .tenureMonths(application.getTenureMonths())
                .build();

        try {
            kafkaTemplate.send(KafkaConfig.LOAN_SUBMITTED_TOPIC, application.getId().toString(), event);
            System.out.println("DEBUG: Successfully published LoanSubmittedEvent for ID: " + application.getId());
        } catch (Exception e) {
            System.err.println("ERROR: Failed to publish Kafka event: " + e.getMessage() + ". Executing direct REST fallback...");
            triggerDirectRestFallback(event, application);
        }

        return mapToResponse(application);
    }

    private void triggerDirectRestFallback(com.credit.loan.event.LoanSubmittedEvent event, LoanApplication application) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = "http://localhost:8082/api/underwriting/evaluate";
            System.out.println("DEBUG: Sending direct fallback REST request to: " + url);
            com.credit.loan.event.CreditEvaluatedEvent result = restTemplate.postForObject(url, event, com.credit.loan.event.CreditEvaluatedEvent.class);
            if (result != null) {
                System.out.println("DEBUG: Direct REST fallback evaluation received decision: " + result.getStatus());
                application.setStatus(com.credit.loan.model.LoanStatus.fromString(result.getStatus()));
                application.setInterestRate(result.getInterestRate());
                application.setAiDecisionSummary(result.getAiDecisionSummary());
                loanApplicationRepository.save(application);
            }
        } catch (Exception e) {
            System.err.println("ERROR: Direct REST fallback evaluation failed: " + e.getMessage());
        }
    }

    public LoanResponse getLoanApplicationById(Long id) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with id: " + id));
        return mapToResponse(application);
    }

    public List<LoanResponse> getAllLoanApplications() {
        return loanApplicationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanResponse updateLoanApplicationStatus(Long id, LoanStatus status, Double interestRate, String aiDecisionSummary) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with id: " + id));
        
        application.setStatus(status);
        application.setInterestRate(interestRate);
        application.setAiDecisionSummary(aiDecisionSummary);
        
        application = loanApplicationRepository.save(application);
        return mapToResponse(application);
    }

    private LoanResponse mapToResponse(LoanApplication app) {
        Customer cust = app.getCustomer();
        return LoanResponse.builder()
                .id(app.getId())
                .customerName(cust.getFirstName() + " " + cust.getLastName())
                .email(cust.getEmail())
                .panNumber(cust.getPanNumber())
                .monthlyIncome(cust.getMonthlyIncome())
                .existingEmi(cust.getExistingEmi())
                .loanAmount(app.getLoanAmount())
                .tenureMonths(app.getTenureMonths())
                .status(app.getStatus())
                .interestRate(app.getInterestRate())
                .aiDecisionSummary(app.getAiDecisionSummary())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
