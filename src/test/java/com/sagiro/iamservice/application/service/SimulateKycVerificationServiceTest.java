package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UserStatusView;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.port.output.CurrentUserProviderPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.valueobject.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulateKycVerificationServiceTest {

    @Mock
    private CurrentUserProviderPort currentUserProviderPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private Clock clock;
    private SimulateKycVerificationService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneId.of("UTC"));
        service = new SimulateKycVerificationService(currentUserProviderPort, userRepositoryPort, clock);
    }

    @Test
    void simulateKycForCurrentUser_Success() {
        String keycloakUserId = "kc-user-123";
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                keycloakUserId, "john.doe", "john@example.com", Set.of("ROLE_CUSTOMER")
        );
        when(currentUserProviderPort.getCurrentUser()).thenReturn(authenticatedUser);

        User existingUser = User.registerNew(
                UUID.randomUUID(),
                keycloakUserId,
                "john@example.com",
                "john.doe",
                "+123456789",
                "John",
                "Doe",
                Instant.now()
        );

        when(userRepositoryPort.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStatusView result = service.simulateKycForCurrentUser();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(VerificationStatus.VERIFIED, savedUser.getVerificationStatus());
        assertEquals(VerificationLevel.STANDARD, savedUser.getVerificationLevel());

        assertNotNull(result);
        assertEquals(VerificationStatus.VERIFIED, result.verificationStatus());
        assertEquals(VerificationLevel.STANDARD, result.verificationLevel());
    }

    @Test
    void simulateKycForCurrentUser_UserNotFound() {
        String keycloakUserId = "kc-user-123";
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                keycloakUserId, "john.doe", "john@example.com", Set.of("ROLE_CUSTOMER")
        );
        when(currentUserProviderPort.getCurrentUser()).thenReturn(authenticatedUser);
        when(userRepositoryPort.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.simulateKycForCurrentUser()
        );

        assertEquals("Authenticated user is not provisioned in IAM", exception.getMessage());
    }
}
