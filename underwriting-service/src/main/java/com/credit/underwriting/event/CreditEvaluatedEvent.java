package com.credit.underwriting.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditEvaluatedEvent {
    private Long applicationId;
    private String status; // APPROVED or DECLINED
    private Double interestRate;
    private String aiDecisionSummary;
}
