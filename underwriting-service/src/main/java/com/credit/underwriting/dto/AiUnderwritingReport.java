package com.credit.underwriting.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUnderwritingReport {
    private String decision; // APPROVED or DECLINED
    private Double score; // Calculated credit score (300 to 850)
    private Double interestRate; // Assigned interest rate
    private String reason; // Explanation of the decision
}
