package com.sagiro.iamservice.infrastructure.persistence.repository;

import com.sagiro.iamservice.infrastructure.persistence.entity.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaBankAccountRepository extends JpaRepository<BankAccountEntity, UUID> {
    List<BankAccountEntity> findByUserIdAndDeletedAtIsNull(String userId);
    Optional<BankAccountEntity> findByUserIdAndIsPrimaryTrueAndDeletedAtIsNull(String userId);
}
