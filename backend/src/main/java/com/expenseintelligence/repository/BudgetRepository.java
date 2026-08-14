package com.expenseintelligence.repository;

import com.expenseintelligence.domain.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserId(UUID userId);

    List<Budget> findByUserIdAndCategoryId(UUID userId, UUID categoryId);
}
