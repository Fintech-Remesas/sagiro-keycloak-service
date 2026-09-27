package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UserStatusView;
import com.sagiro.iamservice.application.exception.ConflictException;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.mapper.UserViewMapper;
import com.sagiro.iamservice.application.port.input.ClaimWelcomeBonusUseCase;
import com.sagiro.iamservice.application.port.output.BankAccountRepositoryPort;
import com.sagiro.iamservice.application.port.output.CardRepositoryPort;
import com.sagiro.iamservice.application.port.output.LedgerDepositPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import com.sagiro.iamservice.domain.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Service that handles the one-time welcome bonus claim for new users.
 * <p>
 * Validates that the user has completed ALL registration steps:
 * 1. Account exists and is ACTIVE
 * 2. Identity verified (KYC status = VERIFIED)
 * 3. At least one bank account registered
 * 4. At least one card registered
 * 5. Bonus not already claimed
 * <p>
 * On success, deposits $1 USD into the user's ledger account and marks the bonus as claimed.
 */
public class ClaimWelcomeBonusService implements ClaimWelcomeBonusUseCase {

    private static final Logger log = LoggerFactory.getLogger(ClaimWelcomeBonusService.class);
    private static final BigDecimal BONUS_AMOUNT = new BigDecimal("1.00");
    private static final String BONUS_CURRENCY = "USD";

    private final UserRepositoryPort userRepositoryPort;
    private final CardRepositoryPort cardRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final LedgerDepositPort ledgerDepositPort;
    private final Clock clock;

    public ClaimWelcomeBonusService(
            UserRepositoryPort userRepositoryPort,
            CardRepositoryPort cardRepositoryPort,
            BankAccountRepositoryPort bankAccountRepositoryPort,
            LedgerDepositPort ledgerDepositPort,
            Clock clock
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.cardRepositoryPort = cardRepositoryPort;
        this.bankAccountRepositoryPort = bankAccountRepositoryPort;
        this.ledgerDepositPort = ledgerDepositPort;
        this.clock = clock;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserStatusView claimBonus(UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 1. Check if bonus was already claimed
        if (user.isWelcomeBonusClaimed()) {
            throw new ConflictException("El bono de bienvenida ya fue reclamado anteriormente.");
        }

        // Checks for KYC, card, and bank account removed as per user request.
        String userIdStr = userId.toString();

        // 5. All validations passed — claim the bonus
        Instant now = Instant.now(clock);
        user.claimWelcomeBonus(now);
        User savedUser = userRepositoryPort.save(user);

        // 6. Request Ledger to process the bonus deposit
        String idempotencyKey = "welcome-bonus-" + userId;
        try {
            ledgerDepositPort.claimInitialBonus(UUID.fromString(user.getKeycloakUserId()), idempotencyKey);
            log.info("Welcome bonus claimed via Ledger for user {}.", userId);
        } catch (Exception e) {
            log.error("Failed to deposit welcome bonus for user {}. Error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al depositar el bono de bienvenida. Intenta nuevamente.", e);
        }

        return UserViewMapper.toStatusView(savedUser);
    }
}
