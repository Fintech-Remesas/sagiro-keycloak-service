package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.AccessContextView;

public interface GetAccessContextUseCase {

    AccessContextView getCurrentAccessContext();
}
