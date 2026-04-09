package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.UserStatusView;

import java.util.UUID;

public interface GetUserStatusUseCase {

    UserStatusView getUserStatus(UUID userId);
}
