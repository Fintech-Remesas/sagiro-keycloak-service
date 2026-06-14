package com.sagiro.iamservice.application.port.output;

public interface IamEventPublisherPort {
    void publishCardAdded(String userId, java.util.UUID cardId, String last4);
    void publishCardDeleted(String userId, java.util.UUID cardId);
    void publishBankAccountAdded(String userId, java.util.UUID bankAccountId, String last4);
    void publishBankAccountDeleted(String userId, java.util.UUID bankAccountId);
}
