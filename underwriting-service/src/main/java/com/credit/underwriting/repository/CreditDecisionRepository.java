package com.credit.underwriting.repository;

import com.credit.underwriting.model.CreditDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CreditDecisionRepository extends JpaRepository<CreditDecision, Long> {
    Optional<CreditDecision> findByApplicationId(Long applicationId);
}
