package com.credit.loan.dto;

import com.credit.loan.model.LoanStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {
    private Long id;
    private String customerName;
    private String email;
    private String panNumber;
    private Double monthlyIncome;
    private Double existingEmi;
    private Double loanAmount;
    private Integer tenureMonths;
    private LoanStatus status;
    private Double interestRate;
    private String aiDecisionSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
