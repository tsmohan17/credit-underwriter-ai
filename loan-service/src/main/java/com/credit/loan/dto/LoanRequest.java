package com.credit.loan.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format (e.g. ABCDE1234F)")
    private String panNumber;

    @NotNull(message = "Monthly income is required")
    @Positive(message = "Monthly income must be positive")
    private Double monthlyIncome;

    @NotNull(message = "Existing EMI is required")
    @Min(value = 0, message = "Existing EMI cannot be negative")
    private Double existingEmi;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    private Double loanAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenureMonths;
}
