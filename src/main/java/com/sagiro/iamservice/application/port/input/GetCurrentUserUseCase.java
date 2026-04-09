package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.UserView;

public interface GetCurrentUserUseCase {

    UserView getCurrentUser();
}
