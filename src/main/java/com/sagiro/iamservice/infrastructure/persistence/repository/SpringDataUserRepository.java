package com.sagiro.iamservice.infrastructure.persistence.repository;

import com.sagiro.iamservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByKeycloakUserId(String keycloakUserId);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserEntity> findByPasswordResetToken(String passwordResetToken);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM UserEntity u WHERE u.verificationStatus = 'VERIFIED' AND (" +
           "(:type = 'name' AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')))) OR " +
           "(:type = 'phone' AND u.phone = :query) OR " +
           "(:type = 'id' AND CAST(u.id as string) = :query)" +
           ")")
    org.springframework.data.domain.Page<UserEntity> searchVerifiedUsers(
            @org.springframework.data.repository.query.Param("query") String query, 
            @org.springframework.data.repository.query.Param("type") String type, 
            org.springframework.data.domain.Pageable pageable);
}
