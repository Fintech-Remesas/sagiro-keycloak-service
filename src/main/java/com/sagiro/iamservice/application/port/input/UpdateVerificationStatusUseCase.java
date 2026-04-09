package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.UpdateVerificationStatusCommand;
import com.sagiro.iamservice.application.dto.UserStatusView;

public interface UpdateVerificationStatusUseCase {

    UserStatusView update(UpdateVerificationStatusCommand command);
}
