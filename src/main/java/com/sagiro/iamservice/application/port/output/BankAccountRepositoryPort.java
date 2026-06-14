package com.sagiro.iamservice.application.port.output;

import com.sagiro.iamservice.domain.model.BankAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepositoryPort {
    BankAccount save(BankAccount bankAccount);
    Optional<BankAccount> findById(UUID id);
    List<BankAccount> findByUserId(String userId);
    Optional<BankAccount> findPrimaryByUserId(String userId);
}
