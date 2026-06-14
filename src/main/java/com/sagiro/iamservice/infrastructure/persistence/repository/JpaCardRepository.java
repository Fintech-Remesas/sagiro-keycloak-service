package com.sagiro.iamservice.infrastructure.persistence.repository;

import com.sagiro.iamservice.infrastructure.persistence.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCardRepository extends JpaRepository<CardEntity, UUID> {
    List<CardEntity> findByUserIdAndDeletedAtIsNull(String userId);
    boolean existsByUserIdAndLast4AndCardBrandAndDeletedAtIsNull(String userId, String last4, String cardBrand);
    Optional<CardEntity> findByUserIdAndIsPrimaryTrueAndDeletedAtIsNull(String userId);
}
