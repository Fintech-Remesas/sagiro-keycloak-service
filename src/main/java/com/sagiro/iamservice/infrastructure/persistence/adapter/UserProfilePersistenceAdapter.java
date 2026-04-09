package com.sagiro.iamservice.infrastructure.persistence.adapter;

import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.domain.model.UserProfile;
import com.sagiro.iamservice.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.sagiro.iamservice.infrastructure.persistence.repository.SpringDataUserProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserProfilePersistenceAdapter implements UserProfileRepositoryPort {

    private final SpringDataUserProfileRepository springDataUserProfileRepository;

    public UserProfilePersistenceAdapter(SpringDataUserProfileRepository springDataUserProfileRepository) {
        this.springDataUserProfileRepository = springDataUserProfileRepository;
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        return UserPersistenceMapper.toDomain(
                springDataUserProfileRepository.save(UserPersistenceMapper.toEntity(userProfile))
        );
    }

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        return springDataUserProfileRepository.findByUserId(userId).map(UserPersistenceMapper::toDomain);
    }
}
