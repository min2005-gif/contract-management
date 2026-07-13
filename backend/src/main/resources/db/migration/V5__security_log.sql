CREATE TABLE security_event (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id   UUID,
    type       VARCHAR(64) NOT NULL,
    detail     VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_security_event_actor ON security_event (actor_id);
CREATE INDEX idx_security_event_type ON security_event (type);
