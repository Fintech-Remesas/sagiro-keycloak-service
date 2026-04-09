package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.RegisterUserCommand;
import com.sagiro.iamservice.application.dto.UserView;

public interface RegisterUserUseCase {

    UserView register(RegisterUserCommand command);
}
