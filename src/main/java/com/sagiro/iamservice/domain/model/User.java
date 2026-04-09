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
            Instant updatedAt
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
                now
        );
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
}
