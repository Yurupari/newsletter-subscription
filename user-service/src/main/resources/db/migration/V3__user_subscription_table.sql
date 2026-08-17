CREATE TYPE subscription_status AS ENUM (
    'PENDING_CONFIRMATION',
    'CONFIRMED',
    'UNSUBSCRIBED'
);

CREATE TABLE user_subscription (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    newsletter_id BIGINT NOT NULL REFERENCES newsletter(id) ON DELETE CASCADE,
    status subscription_status NOT NULL DEFAULT 'PENDING_CONFIRMATION',
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_user_newsletter UNIQUE (user_id, newsletter_id)
);

CREATE INDEX idx_user_subscription_user_id ON user_subscription (user_id);