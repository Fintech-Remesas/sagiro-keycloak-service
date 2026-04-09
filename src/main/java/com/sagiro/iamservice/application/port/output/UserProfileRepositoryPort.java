package com.sagiro.iamservice.application.port.output;

import com.sagiro.iamservice.domain.model.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepositoryPort {

    UserProfile save(UserProfile userProfile);

    Optional<UserProfile> findByUserId(UUID userId);
}
