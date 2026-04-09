package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UpdateVerificationStatusCommand;
import com.sagiro.iamservice.application.dto.UserStatusView;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import com.sagiro.iamservice.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateVerificationStatusServiceTest {

    @Test
    void shouldEnableOperationsWhenVerificationBecomesVerifiedAndAccountIsActive() {
        UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-04-04T21:00:00Z"), ZoneOffset.UTC);
        UUID userId = UUID.randomUUID();

        User user = User.registerNew(
                userId,
                "kc-001",
                "verified@techi.test",
                "verified.user",
                "+51911111111",
                "Verified",
                "User",
                Instant.parse("2026-04-04T20:00:00Z")
        );
        user.markActive(Instant.parse("2026-04-04T20:30:00Z"));

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVerificationStatusService service = new UpdateVerificationStatusService(userRepositoryPort, clock);

        UserStatusView result = service.update(new UpdateVerificationStatusCommand(
                userId,
                VerificationStatus.VERIFIED,
                VerificationLevel.ENHANCED,
                Instant.parse("2026-04-04T20:45:00Z")
        ));

        assertThat(result.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(result.verificationLevel()).isEqualTo(VerificationLevel.ENHANCED);
        assertThat(result.canOperate()).isTrue();
    }
}
