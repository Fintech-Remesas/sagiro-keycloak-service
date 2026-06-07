package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.UserStatusView;

public interface SimulateKycVerificationUseCase {
    UserStatusView simulateKycForCurrentUser();
}
