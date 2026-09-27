package com.sagiro.iamservice.application.port.output;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Output port for depositing funds into the Ledger service.
 * Used by the welcome bonus flow to credit the user's ledger account.
 */
public interface LedgerDepositPort {
    void depositFunds(UUID userId, BigDecimal amount, String currency, String idempotencyKey, String description);
    void claimInitialBonus(UUID userId, String idempotencyKey);
}
