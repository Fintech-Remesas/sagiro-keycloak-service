package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.AccessContextView;
import com.sagiro.iamservice.application.port.output.CurrentUserProviderPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.valueobject.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAccessContextServiceTest {

    @Test
    void shouldReturnRolesFromTokenAndOperationalFlagsFromIamProjection() {
        CurrentUserProviderPort currentUserProviderPort = mock(CurrentUserProviderPort.class);
        UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);

        User user = User.registerNew(
                UUID.randomUUID(),
                "kc-777",
                "customer@techi.test",
                "customer.user",
                "+51922222222",
                "Customer",
                "User",
                Instant.parse("2026-04-04T20:00:00Z")
        );
        user.markActive(Instant.parse("2026-04-04T20:10:00Z"));
        user.updateVerification(
                com.sagiro.iamservice.domain.enums.VerificationStatus.VERIFIED,
                com.sagiro.iamservice.domain.enums.VerificationLevel.STANDARD,
                Instant.parse("2026-04-04T20:20:00Z"),
                Instant.parse("2026-04-04T20:20:00Z")
        );

        when(currentUserProviderPort.getCurrentUser()).thenReturn(new AuthenticatedUser(
                "kc-777",
                "customer.user",
                "customer@techi.test",
                Set.of("ROLE_CUSTOMER")
        ));
        when(userRepositoryPort.findByKeycloakUserId("kc-777")).thenReturn(Optional.of(user));

        GetAccessContextService service = new GetAccessContextService(currentUserProviderPort, userRepositoryPort);

        AccessContextView result = service.getCurrentAccessContext();

        assertThat(result.subject()).isEqualTo("kc-777");
        assertThat(result.roles()).containsExactly("ROLE_CUSTOMER");
        assertThat(result.canOperate()).isTrue();
        assertThat(result.accountStatus().name()).isEqualTo("ACTIVE");
    }
}
