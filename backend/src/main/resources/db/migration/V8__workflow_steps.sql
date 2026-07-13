CREATE TABLE workflow_step (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL REFERENCES contract (id) ON DELETE CASCADE,
    action      VARCHAR(30) NOT NULL,
    from_status VARCHAR(30) NOT NULL,
    to_status   VARCHAR(30) NOT NULL,
    actor_id    UUID NOT NULL,
    reason      VARCHAR(1024),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_workflow_step_contract ON workflow_step (contract_id);
