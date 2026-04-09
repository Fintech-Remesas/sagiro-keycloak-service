package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UserStatusView;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.mapper.UserViewMapper;
import com.sagiro.iamservice.application.port.input.GetUserStatusUseCase;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;

import java.util.UUID;

public class GetUserStatusService implements GetUserStatusUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetUserStatusService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UserStatusView getUserStatus(UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
        return UserViewMapper.toStatusView(user);
    }
}
