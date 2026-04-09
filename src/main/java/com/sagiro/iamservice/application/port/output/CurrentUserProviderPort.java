package com.sagiro.iamservice.application.port.output;

import com.sagiro.iamservice.domain.valueobject.AuthenticatedUser;

public interface CurrentUserProviderPort {

    AuthenticatedUser getCurrentUser();
}
