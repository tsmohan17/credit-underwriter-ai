package com.credit.underwriting.controller;

import com.credit.underwriting.event.LoanSubmittedEvent;
import com.credit.underwriting.event.CreditEvaluatedEvent;
import com.credit.underwriting.service.UnderwritingService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/underwriting")
@CrossOrigin(origins = "*") // Allow Angular UI to access this endpoint
public class AdvisoryChatController {

    private final ChatClient chatClient;
    private final UnderwritingService underwritingService;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Autowired
    public AdvisoryChatController(ChatClient chatClient, UnderwritingService underwritingService) {
        this.chatClient = chatClient;
        this.underwritingService = underwritingService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<CreditEvaluatedEvent> evaluateCredit(@RequestBody LoanSubmittedEvent event) {
        System.out.println("DEBUG: Direct REST fallback evaluation invoked for application ID: " + event.getApplicationId());
        CreditEvaluatedEvent response = underwritingService.evaluateCreditRisk(event);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/advise")
    public ResponseEntity<Map<String, String>> getCreditAdvice(@RequestBody Map<String, String> requestBody) {
        String message = requestBody.get("message");
        if (message == null || message.trim().isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Message cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        System.out.println("DEBUG: Received credit advisory request: " + message);

        String advice;

        if ("mock-key".equalsIgnoreCase(apiKey)) {
            System.out.println("DEBUG: Mock OpenAI key detected. Executing local mock advisory rules.");
            advice = getMockAdvice(message);
        } else {
            try {
                String systemPrompt = "You are a professional credit underwriting advisor. "
                        + "Provide helpful, actionable advice on credit scores, debt management, "
                        + "and how to improve eligibility for credit/loans. Keep responses concise and friendly. "
                        + "Question: " + message;
                
                advice = chatClient.call(systemPrompt);
            } catch (Exception e) {
                System.err.println("ERROR: OpenAI call failed for advisor chat: " + e.getMessage());
                advice = getMockAdvice(message);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("response", advice);
        return ResponseEntity.ok(response);
    }

    private String getMockAdvice(String query) {
        query = query.toLowerCase().trim();
        String prefix = "[Mock AI Advisor] ";
        
        if (query.equals("hi") || query.equals("hello") || query.equals("hey") || query.contains("greetings")) {
            return prefix + "Hello! I am your AI Underwriting Advisor. Ask me anything about how credit decisions are made, how to calculate your Debt-to-Income (DTI) ratio, or how to qualify for better rates.";
        }
        if (query.contains("dti") || query.contains("debt-to-income")) {
            return prefix + "Debt-to-Income (DTI) ratio measures the percentage of your monthly income that goes toward paying debts. Formula: (Total Monthly Debt Payments / Gross Monthly Income) * 100. Lenders prefer a DTI ratio below 36%, and anything above 43% is typically a red flag.";
        }
        if (query.contains("income") || query.contains("salary") || query.contains("earn")) {
            return prefix + "Your net monthly income determines your capacity to repay. Lenders evaluate how much disposable income you have left after paying existing EMIs. Higher income relative to debt yields lower interest rates and higher approval chances.";
        }
        if (query.contains("debt") || query.contains("emi") || query.contains("loan")) {
            return prefix + "Existing debt EMIs reduce your borrowing power. If your current monthly EMI outflows are high, try to pay off smaller personal loans or credit cards to reduce your DTI ratio before applying for a new loan.";
        }
        if (query.contains("score") || query.contains("improve") || query.contains("credit rating")) {
            return prefix + "To improve your credit score: 1) Pay all EMIs and bills on time. 2) Keep credit card utilization below 30%. 3) Do not close old credit accounts. 4) Avoid applying for multiple new loans in a short period.";
        }
        if (query.contains("interest") || query.contains("rate")) {
            return prefix + "Interest rates are determined by credit risk. High credit scores (above 750) qualify for prime rates, whereas lower scores result in higher rates or rejection. Your DTI ratio also directly influences the interest rate offered.";
        }
        
        return prefix + "To optimize your credit eligibility, try to keep your monthly debts (existing EMIs) under 40% of your net income, pay all bills on time, and build a consistent savings record.";
    }
}
