package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.RemittanceContextView;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.port.input.GetRemittanceContextUseCase;
import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetRemittanceContextService implements GetRemittanceContextUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserProfileRepositoryPort userProfileRepositoryPort;

    public GetRemittanceContextService(
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.userProfileRepositoryPort = userProfileRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public RemittanceContextView getRemittanceContext(UUID userId) {
        // Try by internal ID first, then fallback to keycloak_user_id
        User user = userRepositoryPort.findById(userId)
                .or(() -> userRepositoryPort.findByKeycloakUserId(userId.toString()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UUID internalId = user.getId();
        UserProfile profile = userProfileRepositoryPort.findByUserId(internalId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found: " + userId));

        return new RemittanceContextView(
                user.getId(),
                profile.getCountry(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
