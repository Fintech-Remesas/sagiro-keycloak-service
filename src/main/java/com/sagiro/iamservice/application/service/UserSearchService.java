package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UserSearchPage;
import com.sagiro.iamservice.application.port.input.SearchUsersUseCase;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserSearchService implements SearchUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserSearchService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
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
    public Optional<User> getPublicProfile(UUID userId) {
        return userRepositoryPort.findById(userId)
            .filter(user -> user.getVerificationStatus() == com.sagiro.iamservice.domain.enums.VerificationStatus.VERIFIED);
    }
}
