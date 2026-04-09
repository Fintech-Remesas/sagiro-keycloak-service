package com.sagiro.iamservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String country;

    @Column(name = "preferred_language", nullable = false)
    private String preferredLanguage;

    @Column(name = "blockchain_visibility_enabled", nullable = false)
    private boolean blockchainVisibilityEnabled;

    @Column(name = "dark_mode_enabled", nullable = false)
    private boolean darkModeEnabled;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean isBlockchainVisibilityEnabled() {
        return blockchainVisibilityEnabled;
    }

    public void setBlockchainVisibilityEnabled(boolean blockchainVisibilityEnabled) {
        this.blockchainVisibilityEnabled = blockchainVisibilityEnabled;
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }
}
