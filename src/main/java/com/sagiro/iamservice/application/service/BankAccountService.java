package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.AddBankAccountCommand;
import com.sagiro.iamservice.application.port.input.ManageBankAccountsUseCase;
import com.sagiro.iamservice.application.port.output.BankAccountRepositoryPort;
import com.sagiro.iamservice.application.port.output.IamEventPublisherPort;
import com.sagiro.iamservice.domain.model.BankAccount;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BankAccountService implements ManageBankAccountsUseCase {

    private final BankAccountRepositoryPort bankAccountRepository;
    private final IamEventPublisherPort eventPublisher;

    public BankAccountService(BankAccountRepositoryPort bankAccountRepository, IamEventPublisherPort eventPublisher) {
        this.bankAccountRepository = bankAccountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public BankAccount addBankAccount(AddBankAccountCommand command) {
        boolean isPrimary = bankAccountRepository.findPrimaryByUserId(command.userId()).isEmpty();

        BankAccount bankAccount = BankAccount.create(
            command.userId(),
            command.bankName(),
            command.accountNumber(),
            command.accountType(),
            command.currency(),
            command.country(),
            command.alias(),
            isPrimary
        );

        BankAccount savedAccount = bankAccountRepository.save(bankAccount);
        eventPublisher.publishBankAccountAdded(savedAccount.getUserId(), savedAccount.getId(), savedAccount.getLast4());
        
        return savedAccount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccount> getBankAccounts(String userId) {
        return bankAccountRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteBankAccount(String userId, UUID bankAccountId) {
        BankAccount targetAccount = bankAccountRepository.findById(bankAccountId)
            .filter(a -> a.getUserId().equals(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        targetAccount.delete();
        bankAccountRepository.save(targetAccount);
        
        eventPublisher.publishBankAccountDeleted(userId, bankAccountId);

        if (targetAccount.isPrimary()) {
            bankAccountRepository.findByUserId(userId).stream()
                .filter(a -> !a.isDeleted() && !a.getId().equals(bankAccountId))
                .findFirst()
                .ifPresent(newPrimary -> {
                    newPrimary.markAsPrimary();
                    bankAccountRepository.save(newPrimary);
                });
        }
    }
}
