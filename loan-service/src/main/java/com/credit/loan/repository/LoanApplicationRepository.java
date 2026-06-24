package com.credit.loan.repository;

import com.credit.loan.model.LoanApplication;
import com.credit.loan.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByStatus(LoanStatus status);
}
