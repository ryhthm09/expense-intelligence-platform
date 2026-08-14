package com.expenseintelligence.repository;

import com.expenseintelligence.domain.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    List<Merchant> findByUserId(UUID userId);

    Optional<Merchant> findByUserIdAndNormalizedName(UUID userId, String normalizedName);
}
