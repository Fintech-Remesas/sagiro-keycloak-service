package com.sagiro.iamservice.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class UserProfile {

    private UUID id;
    private UUID userId;
    private String country;
    private String preferredLanguage;
    private boolean blockchainVisibilityEnabled;
    private boolean darkModeEnabled;

    public UserProfile(
            UUID id,
            UUID userId,
            String country,
            String preferredLanguage,
            boolean blockchainVisibilityEnabled,
            boolean darkModeEnabled
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.country = Objects.requireNonNull(country, "country is required");
        this.preferredLanguage = Objects.requireNonNull(preferredLanguage, "preferredLanguage is required");
        this.blockchainVisibilityEnabled = blockchainVisibilityEnabled;
        this.darkModeEnabled = darkModeEnabled;
    }

    public static UserProfile createDefault(UUID id, UUID userId, String country, String preferredLanguage) {
        return new UserProfile(id, userId, country, preferredLanguage, false, false);
    }

    public void update(String country, String preferredLanguage, boolean blockchainVisibilityEnabled, boolean darkModeEnabled) {
        this.country = Objects.requireNonNull(country, "country is required");
        this.preferredLanguage = Objects.requireNonNull(preferredLanguage, "preferredLanguage is required");
        this.blockchainVisibilityEnabled = blockchainVisibilityEnabled;
        this.darkModeEnabled = darkModeEnabled;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCountry() {
        return country;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public boolean isBlockchainVisibilityEnabled() {
        return blockchainVisibilityEnabled;
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }
}
