package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.AddBankAccountCommand;
import com.sagiro.iamservice.domain.model.BankAccount;
import java.util.List;
import java.util.UUID;

public interface ManageBankAccountsUseCase {
    BankAccount addBankAccount(AddBankAccountCommand command);
    List<BankAccount> getBankAccounts(String userId);
    void deleteBankAccount(String userId, UUID bankAccountId);
}
