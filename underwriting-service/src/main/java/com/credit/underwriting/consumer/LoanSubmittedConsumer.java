package com.credit.underwriting.consumer;

import com.credit.underwriting.event.LoanSubmittedEvent;
import com.credit.underwriting.service.UnderwritingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LoanSubmittedConsumer {

    private final UnderwritingService underwritingService;

    @Autowired
    public LoanSubmittedConsumer(UnderwritingService underwritingService) {
        this.underwritingService = underwritingService;
    }

    @KafkaListener(topics = "loan-applications", groupId = "underwriting-group")
    public void consumeLoanApplication(LoanSubmittedEvent event) {
        System.out.println("DEBUG: Consumed LoanSubmittedEvent for application ID: " + event.getApplicationId());
        try {
            underwritingService.evaluateCreditRisk(event);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to process loan risk evaluation from Kafka event: " + e.getMessage());
        }
    }
}
