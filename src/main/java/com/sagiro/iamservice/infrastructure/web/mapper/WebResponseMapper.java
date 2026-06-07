package com.sagiro.iamservice.infrastructure.web.mapper;

import com.sagiro.iamservice.application.dto.AccessContextView;
import com.sagiro.iamservice.application.dto.LoginView;
import com.sagiro.iamservice.application.dto.UserProfileView;
import com.sagiro.iamservice.application.dto.UserStatusView;
import com.sagiro.iamservice.application.dto.UserView;
import com.sagiro.iamservice.infrastructure.web.response.AccessContextResponse;
import com.sagiro.iamservice.infrastructure.web.response.LoginResponse;
import com.sagiro.iamservice.infrastructure.web.response.UserProfileResponse;
import com.sagiro.iamservice.infrastructure.web.response.UserResponse;
import com.sagiro.iamservice.infrastructure.web.response.UserStatusResponse;

public final class WebResponseMapper {

    private WebResponseMapper() {
    }

    public static UserResponse toResponse(UserView view) {
        return new UserResponse(
                view.id(),
                view.keycloakUserId(),
                view.email(),
                view.username(),
                view.phone(),
                view.firstName(),
                view.lastName(),
                view.accountStatus(),
                view.verificationStatus(),
                view.verificationLevel(),
                view.verificationUpdatedAt(),
                view.canOperate(),
                view.enabled(),
                view.createdAt(),
                view.updatedAt(),
                toResponse(view.profile())
        );
    }

    public static UserProfileResponse toResponse(UserProfileView view) {
        return new UserProfileResponse(
                view.id(),
                view.userId(),
                view.country(),
                view.preferredLanguage(),
                view.blockchainVisibilityEnabled(),
                view.darkModeEnabled()
        );
    }

    public static AccessContextResponse toResponse(AccessContextView view) {
        return new AccessContextResponse(
                view.userId(),
                view.keycloakUserId(),
                view.subject(),
                view.username(),
                view.email(),
                view.roles(),
                view.accountStatus(),
                view.verificationStatus(),
                view.verificationLevel(),
                view.canOperate(),
                view.enabled()
        );
    }

    public static UserStatusResponse toResponse(UserStatusView view) {
        return new UserStatusResponse(
                view.userId(),
                view.accountStatus(),
                view.verificationStatus(),
                view.verificationLevel(),
                view.canOperate(),
                view.enabled(),
                view.verificationUpdatedAt()
        );
    }

    public static LoginResponse toResponse(LoginView view) {
        return new LoginResponse(
                view.accessToken(),
                view.refreshToken(),
                view.expiresIn(),
                view.refreshExpiresIn(),
                view.tokenType()
        );
    }
}
