package com.expenseintelligence.repository;

import com.expenseintelligence.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    Page<Transaction> findByUserIdAndTransactionDateBetween(
            UUID userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    boolean existsByUserIdAndExternalId(UUID userId, String externalId);
}
