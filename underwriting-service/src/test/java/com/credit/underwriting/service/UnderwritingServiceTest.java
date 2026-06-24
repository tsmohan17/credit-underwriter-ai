package com.credit.underwriting.service;

import com.credit.underwriting.event.LoanSubmittedEvent;
import com.credit.underwriting.model.CreditDecision;
import com.credit.underwriting.repository.CreditDecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.ChatClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnderwritingServiceTest {

    @Mock
    private CreditDecisionRepository creditDecisionRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private UnderwritingService underwritingService;

    private LoanSubmittedEvent lowDtiEvent;
    private LoanSubmittedEvent highDtiEvent;

    @BeforeEach
    public void setUp() {
        // Set reflection field for API Key to simulate mock fallback
        ReflectionTestUtils.setField(underwritingService, "apiKey", "mock-key");

        lowDtiEvent = LoanSubmittedEvent.builder()
                .applicationId(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .panNumber("ABCDE1234F")
                .monthlyIncome(10000.0) // 10k income
                .existingEmi(1000.0)    // 1k debt
                .loanAmount(24000.0)    // 24k loan
                .tenureMonths(24)       // 24 months = 1k principal/mo, low DTI
                .build();

        highDtiEvent = LoanSubmittedEvent.builder()
                .applicationId(2L)
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@example.com")
                .panNumber("FGHIJ5678K")
                .monthlyIncome(3000.0)  // 3k income
                .existingEmi(1000.0)    // 1k debt
                .loanAmount(50000.0)    // 50k loan, high proposed EMI -> high DTI
                .tenureMonths(12)
                .build();
    }

    @Test
    public void testEvaluateCreditRisk_ApprovedFallback() {
        when(creditDecisionRepository.save(any(CreditDecision.class))).thenReturn(null);
        when(kafkaTemplate.send(any(String.class), any(), any())).thenReturn(null);

        underwritingService.evaluateCreditRisk(lowDtiEvent);

        verify(creditDecisionRepository, times(1)).save(argThat(decision -> 
                "APPROVED".equals(decision.getStatus()) && decision.getApplicationId().equals(1L)
        ));
        verify(kafkaTemplate, times(1)).send(eq("credit-evaluations"), eq("1"), any());
    }

    @Test
    public void testEvaluateCreditRisk_DeclinedFallback() {
        when(creditDecisionRepository.save(any(CreditDecision.class))).thenReturn(null);
        when(kafkaTemplate.send(any(String.class), any(), any())).thenReturn(null);

        underwritingService.evaluateCreditRisk(highDtiEvent);

        verify(creditDecisionRepository, times(1)).save(argThat(decision -> 
                "DECLINED".equals(decision.getStatus()) && decision.getApplicationId().equals(2L)
        ));
        verify(kafkaTemplate, times(1)).send(eq("credit-evaluations"), eq("2"), any());
    }
}
