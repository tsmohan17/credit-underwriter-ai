package com.credit.underwriting.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanSubmittedEvent {
    private Long applicationId;
    private String firstName;
    private String lastName;
    private String email;
    private String panNumber;
    private Double monthlyIncome;
    private Double existingEmi;
    private Double loanAmount;
    private Integer tenureMonths;
}
