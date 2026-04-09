package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UpdateVerificationStatusCommand;
import com.sagiro.iamservice.application.dto.UserStatusView;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.mapper.UserViewMapper;
import com.sagiro.iamservice.application.port.input.UpdateVerificationStatusUseCase;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;

import java.time.Clock;
import java.time.Instant;

public class UpdateVerificationStatusService implements UpdateVerificationStatusUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final Clock clock;

    public UpdateVerificationStatusService(UserRepositoryPort userRepositoryPort, Clock clock) {
        this.userRepositoryPort = userRepositoryPort;
        this.clock = clock;
    }

    @Override
    public UserStatusView update(UpdateVerificationStatusCommand command) {
        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));

        Instant now = Instant.now(clock);
        user.updateVerification(
                command.verificationStatus(),
                command.verificationLevel(),
                command.verificationUpdatedAt(),
                now
        );
        User savedUser = userRepositoryPort.save(user);
        return UserViewMapper.toStatusView(savedUser);
    }
}
