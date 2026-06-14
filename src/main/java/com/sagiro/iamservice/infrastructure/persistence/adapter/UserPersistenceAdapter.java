package com.sagiro.iamservice.infrastructure.persistence.adapter;

import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.sagiro.iamservice.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;

    public UserPersistenceAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        return UserPersistenceMapper.toDomain(
                springDataUserRepository.save(UserPersistenceMapper.toEntity(user))
        );
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByKeycloakUserId(String keycloakUserId) {
        return springDataUserRepository.findByKeycloakUserId(keycloakUserId).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByPasswordResetToken(String token) {
        return springDataUserRepository.findByPasswordResetToken(token).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public com.sagiro.iamservice.application.dto.UserSearchPage searchVerifiedUsers(String query, String type, int page, int size) {
        org.springframework.data.domain.Page<com.sagiro.iamservice.infrastructure.persistence.entity.UserEntity> entityPage = 
            springDataUserRepository.searchVerifiedUsers(query, type, org.springframework.data.domain.PageRequest.of(page, size));
        
        java.util.List<User> users = entityPage.getContent().stream()
            .map(UserPersistenceMapper::toDomain)
            .collect(java.util.stream.Collectors.toList());
            
        return new com.sagiro.iamservice.application.dto.UserSearchPage(
            users,
            (int) entityPage.getTotalElements(),
            entityPage.getTotalPages(),
            page,
            size
        );
    }
}
