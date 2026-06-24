package com.credit.loan.consumer;

import com.credit.loan.event.CreditEvaluatedEvent;
import com.credit.loan.model.LoanStatus;
import com.credit.loan.service.LoanApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CreditEvaluatedConsumer {

    private final LoanApplicationService loanApplicationService;

    @Autowired
    public CreditEvaluatedConsumer(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @KafkaListener(topics = "credit-evaluations", groupId = "loan-group")
    public void consumeCreditEvaluation(CreditEvaluatedEvent event) {
        System.out.println("DEBUG: Consumed credit evaluation event for application ID: " + event.getApplicationId());
        try {
            LoanStatus status = LoanStatus.fromString(event.getStatus());
            loanApplicationService.updateLoanApplicationStatus(
                    event.getApplicationId(),
                    status,
                    event.getInterestRate(),
                    event.getAiDecisionSummary()
            );
            System.out.println("DEBUG: Successfully updated application state to " + status + " for ID: " + event.getApplicationId());
        } catch (Exception e) {
            System.err.println("ERROR: Failed to update loan status from Kafka event: " + e.getMessage());
        }
    }
}
