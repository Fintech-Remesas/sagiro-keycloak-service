package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.UserStatusView;

import java.util.UUID;

/**
 * Use case for claiming the one-time welcome bonus ($1 USDC).
 * The bonus is granted only when a new user has completed ALL registration steps:
 * - Account created (REGISTERED/ACTIVE)
 * - Identity verified (KYC: VERIFIED)
 * - At least one bank account registered
 * - At least one card registered
 */
public interface ClaimWelcomeBonusUseCase {
    UserStatusView claimBonus(UUID userId);
}
