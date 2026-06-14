CREATE TABLE cards (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    cardholder_name VARCHAR(255) NOT NULL,
    last4 VARCHAR(4) NOT NULL,
    card_brand VARCHAR(50) NOT NULL,
    card_type VARCHAR(50) NOT NULL,
    expiry_month INT NOT NULL,
    expiry_year INT NOT NULL,
    alias VARCHAR(255),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_deleted_at ON cards(deleted_at);


CREATE TABLE bank_accounts (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number_encrypted TEXT NOT NULL,
    last4 VARCHAR(4) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    country VARCHAR(10) NOT NULL,
    alias VARCHAR(255),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_bank_accounts_user_id ON bank_accounts(user_id);
CREATE INDEX idx_bank_accounts_deleted_at ON bank_accounts(deleted_at);
