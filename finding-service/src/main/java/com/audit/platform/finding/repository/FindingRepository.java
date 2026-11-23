package com.audit.platform.finding.repository;

import com.audit.platform.finding.entity.Finding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FindingRepository extends JpaRepository<Finding, Long> {

    List<Finding> findByTransactionId(Long transactionId);

    List<Finding> findByControlCode(String controlCode);

    List<Finding> findBySeverity(String severity);

    List<Finding> findByStatus(String status);
}

