-- V2: Add password reset token support to iam_users
-- The token is a server-generated one-time code stored hashed or as UUID.
-- It expires after a short TTL (default 15 minutes) configured at application level.
-- A future communication-service will consume the Kafka event and send the token to the user.

ALTER TABLE iam_users
    ADD COLUMN password_reset_token    VARCHAR(100),
    ADD COLUMN password_reset_token_expires_at TIMESTAMP WITH TIME ZONE;

-- Fast lookup by token during the reset step
CREATE UNIQUE INDEX idx_iam_users_password_reset_token
    ON iam_users (password_reset_token)
    WHERE password_reset_token IS NOT NULL;
