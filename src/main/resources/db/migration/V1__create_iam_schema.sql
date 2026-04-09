CREATE TABLE iam_users (
    id UUID PRIMARY KEY,
    keycloak_user_id VARCHAR(100) UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(30),
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    account_status VARCHAR(30) NOT NULL,
    verification_status VARCHAR(30) NOT NULL,
    verification_level VARCHAR(30) NOT NULL,
    verification_updated_at TIMESTAMP WITH TIME ZONE,
    can_operate BOOLEAN NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_iam_users_keycloak_user_id ON iam_users (keycloak_user_id);
CREATE INDEX idx_iam_users_account_status ON iam_users (account_status);
CREATE INDEX idx_iam_users_verification_status ON iam_users (verification_status);

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    country VARCHAR(3) NOT NULL,
    preferred_language VARCHAR(10) NOT NULL,
    blockchain_visibility_enabled BOOLEAN NOT NULL,
    dark_mode_enabled BOOLEAN NOT NULL,
    CONSTRAINT fk_user_profiles_user_id
        FOREIGN KEY (user_id) REFERENCES iam_users (id)
);

CREATE INDEX idx_user_profiles_country ON user_profiles (country);
