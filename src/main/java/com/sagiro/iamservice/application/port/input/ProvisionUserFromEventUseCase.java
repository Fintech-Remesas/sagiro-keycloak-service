package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.ProvisionUserCommand;

public interface ProvisionUserFromEventUseCase {

    void provision(ProvisionUserCommand command);
}
