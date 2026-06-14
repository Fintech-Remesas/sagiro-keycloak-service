package com.sagiro.iamservice.infrastructure.persistence.adapter;

import com.sagiro.iamservice.application.port.output.BankAccountRepositoryPort;
import com.sagiro.iamservice.domain.model.BankAccount;
import com.sagiro.iamservice.infrastructure.persistence.mapper.BankAccountPersistenceMapper;
import com.sagiro.iamservice.infrastructure.persistence.repository.JpaBankAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BankAccountPersistenceAdapter implements BankAccountRepositoryPort {

    private final JpaBankAccountRepository repository;

    public BankAccountPersistenceAdapter(JpaBankAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public BankAccount save(BankAccount bankAccount) {
        return BankAccountPersistenceMapper.toDomain(repository.save(BankAccountPersistenceMapper.toEntity(bankAccount)));
    }

    @Override
    public Optional<BankAccount> findById(UUID id) {
        return repository.findById(id).map(BankAccountPersistenceMapper::toDomain);
    }

    @Override
    public List<BankAccount> findByUserId(String userId) {
        return repository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(BankAccountPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BankAccount> findPrimaryByUserId(String userId) {
        return repository.findByUserIdAndIsPrimaryTrueAndDeletedAtIsNull(userId)
                .map(BankAccountPersistenceMapper::toDomain);
    }
}
