package com.sagiro.iamservice.domain.model;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import com.sagiro.iamservice.domain.service.UserOperationPolicy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class User {

    private UUID id;
    private String keycloakUserId;
    private String email;
    private String username;
    private String phone;
    private String firstName;
    private String lastName;
    private AccountStatus accountStatus;
    private VerificationStatus verificationStatus;
    private VerificationLevel verificationLevel;
    private Instant verificationUpdatedAt;
    private boolean canOperate;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private String passwordResetToken;
    private Instant passwordResetTokenExpiresAt;
    private boolean allowTestRecharge;
    private boolean welcomeBonusClaimed;

    public User(
            UUID id,
            String keycloakUserId,
            String email,
            String username,
            String phone,
            String firstName,
            String lastName,
            AccountStatus accountStatus,
            VerificationStatus verificationStatus,
            VerificationLevel verificationLevel,
            Instant verificationUpdatedAt,
            boolean canOperate,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt,
            String passwordResetToken,
            Instant passwordResetTokenExpiresAt,
            boolean allowTestRecharge,
            boolean welcomeBonusClaimed
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.keycloakUserId = keycloakUserId;
        this.email = Objects.requireNonNull(email, "email is required");
        this.username = Objects.requireNonNull(username, "username is required");
        this.phone = phone;
        this.firstName = Objects.requireNonNull(firstName, "firstName is required");
        this.lastName = Objects.requireNonNull(lastName, "lastName is required");
        this.accountStatus = Objects.requireNonNull(accountStatus, "accountStatus is required");
        this.verificationStatus = Objects.requireNonNull(verificationStatus, "verificationStatus is required");
        this.verificationLevel = Objects.requireNonNull(verificationLevel, "verificationLevel is required");
        this.verificationUpdatedAt = verificationUpdatedAt;
        this.canOperate = canOperate;
        this.enabled = enabled;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.passwordResetToken = passwordResetToken;
        this.passwordResetTokenExpiresAt = passwordResetTokenExpiresAt;
        this.allowTestRecharge = allowTestRecharge;
        this.welcomeBonusClaimed = welcomeBonusClaimed;
        refreshOperationalCapability();
    }

    public static User registerNew(
            UUID id,
            String keycloakUserId,
            String email,
            String username,
            String phone,
            String firstName,
            String lastName,
            Instant now
    ) {
        return new User(
                id,
                keycloakUserId,
                email,
                username,
                phone,
                firstName,
                lastName,
                AccountStatus.REGISTERED,
                VerificationStatus.NOT_STARTED,
                VerificationLevel.NONE,
                null,
                false,
                true,
                now,
                now,
                null,
                null,
                false,
                false
        );
    }

    /**
     * Stores a one-time password-reset token and its expiry on the user.
     * The plain token must be sent to the user via the communication-service (Kafka).
     * Fintech rule: tokens expire after a short configurable TTL (default 15 min).
     */
    public void requestPasswordReset(String token, Instant expiresAt, Instant now) {
        this.passwordResetToken = Objects.requireNonNull(token, "token is required");
        this.passwordResetTokenExpiresAt = Objects.requireNonNull(expiresAt, "expiresAt is required");
        touch(now);
    }

    /**
     * Validates and clears the password-reset token after a successful reset.
     * Throws if the token has expired.
     */
    public void clearPasswordResetToken(Instant now) {
        this.passwordResetToken = null;
        this.passwordResetTokenExpiresAt = null;
        touch(now);
    }

    public void markActive(Instant now) {
        this.accountStatus = AccountStatus.ACTIVE;
        touch(now);
    }

    public void markSuspended(Instant now) {
        this.accountStatus = AccountStatus.SUSPENDED;
        touch(now);
    }

    public void markBlocked(Instant now) {
        this.accountStatus = AccountStatus.BLOCKED;
        this.enabled = false;
        touch(now);
    }

    public void markEnabled(boolean enabled, Instant now) {
        this.enabled = enabled;
        touch(now);
    }

    public void syncIdentity(
            String keycloakUserId,
            String email,
            String username,
            String phone,
            String firstName,
            String lastName,
            Instant now
    ) {
        this.keycloakUserId = keycloakUserId;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        touch(now);
    }

    public void updatePersonalDetails(String phone, String firstName, String lastName, Instant now) {
        this.phone = phone;
        this.firstName = Objects.requireNonNull(firstName, "firstName is required");
        this.lastName = Objects.requireNonNull(lastName, "lastName is required");
        touch(now);
    }

    public void updateVerification(VerificationStatus status, VerificationLevel level, Instant verificationUpdatedAt, Instant now) {
        this.verificationStatus = Objects.requireNonNull(status, "verificationStatus is required");
        this.verificationLevel = Objects.requireNonNull(level, "verificationLevel is required");
        this.verificationUpdatedAt = verificationUpdatedAt;
        touch(now);
    }

    private void touch(Instant now) {
        this.updatedAt = Objects.requireNonNull(now, "updatedAt is required");
        refreshOperationalCapability();
    }

    private void refreshOperationalCapability() {
        this.canOperate = UserOperationPolicy.canOperate(accountStatus, verificationStatus, enabled);
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public VerificationLevel getVerificationLevel() {
        return verificationLevel;
    }

    public Instant getVerificationUpdatedAt() {
        return verificationUpdatedAt;
    }

    public boolean isCanOperate() {
        return canOperate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public Instant getPasswordResetTokenExpiresAt() {
        return passwordResetTokenExpiresAt;
    }

    public boolean isAllowTestRecharge() {
        return allowTestRecharge;
    }

    public boolean isWelcomeBonusClaimed() {
        return welcomeBonusClaimed;
    }

    /**
     * Claims the one-time welcome bonus. Throws if already claimed.
     */
    public void claimWelcomeBonus(Instant now) {
        if (this.welcomeBonusClaimed) {
            throw new IllegalStateException("Welcome bonus already claimed");
        }
        this.welcomeBonusClaimed = true;
        touch(now);
    }
}
