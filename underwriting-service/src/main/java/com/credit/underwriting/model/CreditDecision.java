package com.credit.underwriting.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private String status; // APPROVED or DECLINED

    @Column(nullable = false)
    private Double score; // Credit score calculated (e.g. 300-850)

    @Column(nullable = false)
    private Double interestRate;

    @Column(length = 2000)
    private String riskSummary;

    private LocalDateTime evaluatedAt;

    @PrePersist
    protected void onEvaluate() {
        evaluatedAt = LocalDateTime.now();
    }
}
