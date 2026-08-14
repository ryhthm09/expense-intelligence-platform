package com.expenseintelligence.repository;

import com.expenseintelligence.domain.entity.Category;
import com.expenseintelligence.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("""
            SELECT c FROM Category c
            WHERE c.system = true OR c.user.id = :userId
            ORDER BY c.system DESC, c.name ASC
            """)
    List<Category> findAvailableForUser(@Param("userId") UUID userId);

    List<Category> findByUserId(UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);

    List<Category> findByTypeAndSystemTrue(CategoryType type);
}
