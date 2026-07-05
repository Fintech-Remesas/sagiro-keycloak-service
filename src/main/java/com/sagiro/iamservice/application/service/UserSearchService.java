package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UserSearchPage;
import com.sagiro.iamservice.application.dto.UserView;
import com.sagiro.iamservice.application.mapper.UserViewMapper;
import com.sagiro.iamservice.application.port.input.SearchUsersUseCase;
import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserSearchService implements SearchUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserProfileRepositoryPort userProfileRepositoryPort;

    public UserSearchService(UserRepositoryPort userRepositoryPort,
                             UserProfileRepositoryPort userProfileRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.userProfileRepositoryPort = userProfileRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSearchPage searchUsers(String query, String type, int page, int size) {
        if (size > 20) {
            size = 20; // Max page size 20 per requirements
        }
        return userRepositoryPort.searchVerifiedUsers(query, type, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserView> getPublicProfile(UUID userId) {
        Optional<User> userOpt = userRepositoryPort.findById(userId)
            .filter(user -> user.getVerificationStatus() == com.sagiro.iamservice.domain.enums.VerificationStatus.VERIFIED);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        Optional<UserProfile> profileOpt = userProfileRepositoryPort.findByUserId(userId);
        UserProfile profile = profileOpt.orElse(null);
        return Optional.of(UserViewMapper.toView(user, profile));
    }
}
