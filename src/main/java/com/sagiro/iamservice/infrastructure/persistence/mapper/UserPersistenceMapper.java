package com.sagiro.iamservice.infrastructure.persistence.mapper;

import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;
import com.sagiro.iamservice.infrastructure.persistence.entity.UserEntity;
import com.sagiro.iamservice.infrastructure.persistence.entity.UserProfileEntity;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getKeycloakUserId(),
                entity.getEmail(),
                entity.getUsername(),
                entity.getPhone(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAccountStatus(),
                entity.getVerificationStatus(),
                entity.getVerificationLevel(),
                entity.getVerificationUpdatedAt(),
                entity.isCanOperate(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPasswordResetToken(),
                entity.getPasswordResetTokenExpiresAt()
        );
    }

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setKeycloakUserId(user.getKeycloakUserId());
        entity.setEmail(user.getEmail());
        entity.setUsername(user.getUsername());
        entity.setPhone(user.getPhone());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setAccountStatus(user.getAccountStatus());
        entity.setVerificationStatus(user.getVerificationStatus());
        entity.setVerificationLevel(user.getVerificationLevel());
        entity.setVerificationUpdatedAt(user.getVerificationUpdatedAt());
        entity.setCanOperate(user.isCanOperate());
        entity.setEnabled(user.isEnabled());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setPasswordResetToken(user.getPasswordResetToken());
        entity.setPasswordResetTokenExpiresAt(user.getPasswordResetTokenExpiresAt());
        return entity;
    }

    public static UserProfile toDomain(UserProfileEntity entity) {
        return new UserProfile(
                entity.getId(),
                entity.getUserId(),
                entity.getCountry(),
                entity.getPreferredLanguage(),
                entity.isBlockchainVisibilityEnabled(),
                entity.isDarkModeEnabled()
        );
    }

    public static UserProfileEntity toEntity(UserProfile profile) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setId(profile.getId());
        entity.setUserId(profile.getUserId());
        entity.setCountry(profile.getCountry());
        entity.setPreferredLanguage(profile.getPreferredLanguage());
        entity.setBlockchainVisibilityEnabled(profile.isBlockchainVisibilityEnabled());
        entity.setDarkModeEnabled(profile.isDarkModeEnabled());
        return entity;
    }
}
