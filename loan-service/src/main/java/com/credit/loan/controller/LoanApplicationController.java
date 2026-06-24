package com.credit.loan.controller;

import com.credit.loan.dto.LoanRequest;
import com.credit.loan.dto.LoanResponse;
import com.credit.loan.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*") // Allow Angular UI to access this endpoint
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @Autowired
    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    public ResponseEntity<LoanResponse> submitLoan(@Valid @RequestBody LoanRequest request) {
        LoanResponse response = loanApplicationService.submitLoanApplication(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        LoanResponse response = loanApplicationService.getLoanApplicationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        List<LoanResponse> responses = loanApplicationService.getAllLoanApplications();
        return ResponseEntity.ok(responses);
    }
}
