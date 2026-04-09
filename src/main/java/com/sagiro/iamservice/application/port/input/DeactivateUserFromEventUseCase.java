package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.DeactivateUserCommand;

public interface DeactivateUserFromEventUseCase {

    void deactivate(DeactivateUserCommand command);
}
