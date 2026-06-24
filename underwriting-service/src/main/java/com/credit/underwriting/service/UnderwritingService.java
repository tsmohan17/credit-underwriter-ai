package com.credit.underwriting.service;

import com.credit.underwriting.dto.AiUnderwritingReport;
import com.credit.underwriting.event.CreditEvaluatedEvent;
import com.credit.underwriting.event.LoanSubmittedEvent;
import com.credit.underwriting.model.CreditDecision;
import com.credit.underwriting.repository.CreditDecisionRepository;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UnderwritingService {

    private final CreditDecisionRepository creditDecisionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ChatClient chatClient;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Autowired
    public UnderwritingService(CreditDecisionRepository creditDecisionRepository,
                               KafkaTemplate<String, Object> kafkaTemplate,
                               ChatClient chatClient) {
        this.creditDecisionRepository = creditDecisionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.chatClient = chatClient;
    }

    @Transactional
    public CreditEvaluatedEvent evaluateCreditRisk(LoanSubmittedEvent event) {
        System.out.println("DEBUG: Starting credit risk evaluation for application ID: " + event.getApplicationId());

        AiUnderwritingReport report;

        // Check if using mock key or real key
        if ("mock-key".equalsIgnoreCase(apiKey)) {
            System.out.println("DEBUG: Mock OpenAI key detected. Executing rule-based fallback underwriting.");
            report = executeFallbackUnderwriting(event);
        } else {
            try {
                report = executeAiUnderwriting(event);
            } catch (Exception e) {
                System.err.println("ERROR: OpenAI API call failed: " + e.getMessage() + ". Falling back to rule-based evaluation.");
                report = executeFallbackUnderwriting(event);
            }
        }

        // Normalize status
        String decisionStatus = report.getDecision();
        if (decisionStatus != null) {
            String normalized = decisionStatus.trim().toUpperCase();
            if (normalized.equals("APPROVE") || normalized.equals("APPROVED")) {
                decisionStatus = "APPROVED";
            } else if (normalized.equals("DECLINE") || normalized.equals("DECLINED")) {
                decisionStatus = "DECLINED";
            }
        }

        // Save credit decision
        CreditDecision decision = CreditDecision.builder()
                .applicationId(event.getApplicationId())
                .status(decisionStatus)
                .score(report.getScore())
                .interestRate(report.getInterestRate())
                .riskSummary(report.getReason())
                .build();

        creditDecisionRepository.save(decision);

        // Publish event to Kafka
        CreditEvaluatedEvent evaluatedEvent = CreditEvaluatedEvent.builder()
                .applicationId(event.getApplicationId())
                .status(decisionStatus)
                .interestRate(report.getInterestRate())
                .aiDecisionSummary(report.getReason())
                .build();

        try {
            kafkaTemplate.send("credit-evaluations", event.getApplicationId().toString(), evaluatedEvent);
            System.out.println("DEBUG: Successfully published CreditEvaluatedEvent for ID: " + event.getApplicationId());
        } catch (Exception e) {
            System.err.println("ERROR: Failed to publish CreditEvaluatedEvent: " + e.getMessage());
        }

        return evaluatedEvent;
    }

    private AiUnderwritingReport executeAiUnderwriting(LoanSubmittedEvent event) {
        // Output parser enforces the LLM to output JSON matching our Java Bean
        BeanOutputParser<AiUnderwritingReport> parser = new BeanOutputParser<>(AiUnderwritingReport.class);

        String userPrompt = """
                Perform credit underwriting for the following loan applicant.
                Applicant details:
                - Name: {firstName} {lastName}
                - Monthly Income: ${monthlyIncome}
                - Existing Monthly Debt (EMI): ${existingEmi}
                - Requested Loan Amount: ${loanAmount}
                - Loan Tenure: {tenureMonths} months
                - PAN Number: {panNumber}

                Guidelines:
                1. Calculate the Debt-to-Income (DTI) ratio. If DTI is greater than 50%, DECLINE the application.
                2. Calculate a credit score between 300 and 850 based on income, current debt, and requested amount.
                3. If approved, assign an interest rate between 8.5% and 15.0% based on the risk score (higher score = lower interest rate).
                4. For declined applications, interest rate should be 0.0.
                5. Provide a short, detailed risk assessment summary.

                {format}
                """;

        PromptTemplate template = new PromptTemplate(userPrompt);
        Prompt prompt = template.create(Map.of(
                "firstName", event.getFirstName(),
                "lastName", event.getLastName(),
                "monthlyIncome", event.getMonthlyIncome(),
                "existingEmi", event.getExistingEmi(),
                "loanAmount", event.getLoanAmount(),
                "tenureMonths", event.getTenureMonths(),
                "panNumber", event.getPanNumber(),
                "format", parser.getFormat()
        ));

        String rawResponse = chatClient.call(prompt).getResult().getOutput().getContent();
        System.out.println("DEBUG: Raw LLM response: " + rawResponse);
        return parser.parse(rawResponse);
    }

    private AiUnderwritingReport executeFallbackUnderwriting(LoanSubmittedEvent event) {
        // Estimate proposed monthly EMI (Principal + 8% Interest / Tenure)
        double estimatedInterest = event.getLoanAmount() * 0.08 * (event.getTenureMonths() / 12.0);
        double proposedEmi = (event.getLoanAmount() + estimatedInterest) / event.getTenureMonths();

        double totalMonthlyDebt = event.getExistingEmi() + proposedEmi;
        double dtiRatio = totalMonthlyDebt / event.getMonthlyIncome();

        System.out.println("DEBUG: Fallback DTI calculation - Proposed EMI: " + proposedEmi + ", DTI: " + dtiRatio);

        String decision;
        double score;
        double interestRate;
        String reason;

        if (dtiRatio > 0.50) {
            decision = "DECLINED";
            score = 450.0;
            interestRate = 0.0;
            reason = String.format("Declined due to high Debt-to-Income (DTI) ratio of %.2f%%. Combined debt obligations exceed 50%% of monthly income.", dtiRatio * 100);
        } else {
            decision = "APPROVED";
            // Map DTI ratio directly to a credit score
            score = 850.0 - (dtiRatio * 400.0);
            interestRate = 8.5 + (dtiRatio * 12.0);
            reason = String.format("Approved. Debt-to-Income (DTI) ratio is healthy at %.2f%%. Proposed EMI of $%.2f represents a manageable obligation relative to monthly income of $%.2f.", dtiRatio * 100, proposedEmi, event.getMonthlyIncome());
        }

        return AiUnderwritingReport.builder()
                .decision(decision)
                .score(score)
                .interestRate(interestRate)
                .reason(reason)
                .build();
    }
}
