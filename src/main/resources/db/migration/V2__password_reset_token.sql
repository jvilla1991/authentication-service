-- One-time password-reset tokens, issued by an admin and handed to the user
-- out-of-band. Only the SHA-256 hash of the token is stored; the raw token
-- exists solely inside the link the admin copies. Single-use (used_at) and
-- TTL-bounded (expires_at). Schema comes from spring.flyway.default-schema.
CREATE TABLE IF NOT EXISTS password_reset_token (
    id          BIGSERIAL PRIMARY KEY,
    token_hash  TEXT NOT NULL UNIQUE,
    user_uuid   UUID NOT NULL REFERENCES _user(uuid) ON DELETE CASCADE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
