package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.UserSearchPage;
import com.sagiro.iamservice.application.dto.UserView;
import java.util.Optional;
import java.util.UUID;

public interface SearchUsersUseCase {
    UserSearchPage searchUsers(String query, String type, int page, int size);
    Optional<UserView> getPublicProfile(UUID userId);
}
