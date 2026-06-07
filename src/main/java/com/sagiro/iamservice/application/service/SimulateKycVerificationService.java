package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UserStatusView;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.mapper.UserViewMapper;
import com.sagiro.iamservice.application.port.input.SimulateKycVerificationUseCase;
import com.sagiro.iamservice.application.port.output.CurrentUserProviderPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.valueobject.AuthenticatedUser;

import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

public class SimulateKycVerificationService implements SimulateKycVerificationUseCase {

    private final CurrentUserProviderPort currentUserProviderPort;
    private final UserRepositoryPort userRepositoryPort;
    private final Clock clock;

    public SimulateKycVerificationService(
            CurrentUserProviderPort currentUserProviderPort,
            UserRepositoryPort userRepositoryPort,
            Clock clock
    ) {
        this.currentUserProviderPort = currentUserProviderPort;
        this.userRepositoryPort = userRepositoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserStatusView simulateKycForCurrentUser() {
        AuthenticatedUser authenticatedUser = currentUserProviderPort.getCurrentUser();
        User user = userRepositoryPort.findByKeycloakUserId(authenticatedUser.subject())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user is not provisioned in IAM"));

        Instant now = Instant.now(clock);
        user.updateVerification(
                VerificationStatus.VERIFIED,
                VerificationLevel.STANDARD,
                now,
                now
        );
        // Activar la cuenta: un usuario verificado debe pasar a ACTIVE y poder operar
        user.markActive(now);

        User savedUser = userRepositoryPort.save(user);
        return UserViewMapper.toStatusView(savedUser);
    }
}
