package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.UpdateUserProfileCommand;
import com.sagiro.iamservice.application.dto.UserView;

public interface UpdateUserProfileUseCase {

    UserView updateCurrentUserProfile(UpdateUserProfileCommand command);
}
