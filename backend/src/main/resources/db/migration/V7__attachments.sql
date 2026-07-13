CREATE TABLE attachment (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id  UUID NOT NULL REFERENCES contract (id) ON DELETE CASCADE,
    filename     VARCHAR(500) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    kind         VARCHAR(20)  NOT NULL,
    size_bytes   BIGINT NOT NULL,
    object_key   VARCHAR(500) NOT NULL,
    uploaded_by  UUID,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_attachment_contract ON attachment (contract_id);
