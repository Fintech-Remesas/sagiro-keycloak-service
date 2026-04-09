package com.sagiro.iamservice.application.mapper;

import com.sagiro.iamservice.application.dto.UserProfileView;
import com.sagiro.iamservice.application.dto.UserStatusView;
import com.sagiro.iamservice.application.dto.UserView;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;

public final class UserViewMapper {

    private UserViewMapper() {
    }

    public static UserView toView(User user, UserProfile profile) {
        return new UserView(
                user.getId(),
                user.getKeycloakUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getPhone(),
                user.getFirstName(),
                user.getLastName(),
                user.getAccountStatus(),
                user.getVerificationStatus(),
                user.getVerificationLevel(),
                user.getVerificationUpdatedAt(),
                user.isCanOperate(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                toProfileView(profile)
        );
    }

    public static UserProfileView toProfileView(UserProfile profile) {
        return new UserProfileView(
                profile.getId(),
                profile.getUserId(),
                profile.getCountry(),
                profile.getPreferredLanguage(),
                profile.isBlockchainVisibilityEnabled(),
                profile.isDarkModeEnabled()
        );
    }

    public static UserStatusView toStatusView(User user) {
        return new UserStatusView(
                user.getId(),
                user.getAccountStatus(),
                user.getVerificationStatus(),
                user.getVerificationLevel(),
                user.isCanOperate(),
                user.isEnabled(),
                user.getVerificationUpdatedAt()
        );
    }
}
