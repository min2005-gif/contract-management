CREATE TABLE alert (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id      UUID NOT NULL REFERENCES contract (id) ON DELETE CASCADE,
    owning_unit_id   UUID NOT NULL,
    type             VARCHAR(30) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    notified_user_id UUID,
    raised_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- At most one OPEN alert of a given type per contract → idempotent evaluation (FR-016).
CREATE UNIQUE INDEX uq_alert_open_per_type ON alert (contract_id, type) WHERE status = 'OPEN';
CREATE INDEX idx_alert_owning_unit ON alert (owning_unit_id);
