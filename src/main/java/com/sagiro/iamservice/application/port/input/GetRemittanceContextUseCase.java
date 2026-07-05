package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.RemittanceContextView;

import java.util.UUID;

public interface GetRemittanceContextUseCase {
    RemittanceContextView getRemittanceContext(UUID userId);
}
