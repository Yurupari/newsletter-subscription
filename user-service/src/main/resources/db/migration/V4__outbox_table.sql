CREATE TYPE outbox_aggregate_type AS ENUM (
    'SUBSCRIPTION',
    'USER'
);

CREATE TYPE outbox_event_type AS ENUM (
    'NEWSLETTER_SUBSCRIBED',
    'NEWSLETTER_UNSUBSCRIBED',
    'USER_CREATED',
    'USER_DELETED'
);

CREATE TYPE outbox_status AS ENUM (
    'PENDING',
    'PROCESSED',
    'FAILED'
);

CREATE TABLE outbox_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aggregate_type outbox_aggregate_type NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type outbox_event_type NOT NULL,
    payload JSONB NOT NULL,
    status outbox_status NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL
);

CREATE INDEX idx_outbox_pending ON outbox_events (status, created_at) WHERE status = 'PENDING';