package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.ExternalIdentityUserView;
import com.sagiro.iamservice.application.dto.RegisterUserCommand;
import com.sagiro.iamservice.application.dto.UserView;
import com.sagiro.iamservice.application.port.output.KeycloakAdminPort;
import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterUserServiceTest {

    @Test
    void shouldRegisterLocalUserAndProvisionKeycloakIdentity() {
        UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
        UserProfileRepositoryPort userProfileRepositoryPort = mock(UserProfileRepositoryPort.class);
        KeycloakAdminPort keycloakAdminPort = mock(KeycloakAdminPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-04-04T19:00:00Z"), ZoneOffset.UTC);

        when(userRepositoryPort.existsByEmail("ana.customer@techi.test")).thenReturn(false);
        when(userRepositoryPort.existsByUsername("ana.customer")).thenReturn(false);
        when(keycloakAdminPort.findUser("ana.customer@techi.test", "ana.customer")).thenReturn(Optional.empty());
        when(keycloakAdminPort.createUser(any())).thenReturn("kc-user-123");
        doNothing().when(keycloakAdminPort).assignRealmRoles(eq("kc-user-123"), eq(java.util.Set.of("ROLE_CUSTOMER")));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepositoryPort.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserService service = new RegisterUserService(
                userRepositoryPort,
                userProfileRepositoryPort,
                keycloakAdminPort,
                clock
        );

        UserView result = service.register(new RegisterUserCommand(
                "ana.customer@techi.test",
                "ana.customer",
                "+51999999999",
                "Ana",
                "Torres",
                "PE",
                "es",
                "TempPass#2026"
        ));

        assertThat(result.keycloakUserId()).isEqualTo("kc-user-123");
        assertThat(result.accountStatus().name()).isEqualTo("REGISTERED");
        assertThat(result.verificationStatus().name()).isEqualTo("NOT_STARTED");
        assertThat(result.canOperate()).isTrue();
        assertThat(result.profile().country()).isEqualTo("PE");
        assertThat(result.profile().preferredLanguage()).isEqualTo("es");
    }
}
