CREATE TABLE contract (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_number       VARCHAR(100) NOT NULL,
    name                  VARCHAR(500) NOT NULL,
    type                  VARCHAR(30)  NOT NULL,
    party_a               VARCHAR(500) NOT NULL,
    party_b               VARCHAR(500) NOT NULL,
    value                 NUMERIC(18, 2) NOT NULL,
    sign_date             DATE NOT NULL,
    term_end              DATE NOT NULL,
    person_in_charge_id   UUID NOT NULL,
    status                VARCHAR(30) NOT NULL,
    is_official           BOOLEAN NOT NULL DEFAULT FALSE,
    owning_unit_id        UUID NOT NULL REFERENCES organizational_unit (id),
    signed                BOOLEAN NOT NULL DEFAULT FALSE,
    payment_status        VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    progress_pct          INTEGER NOT NULL DEFAULT 0,
    expected_progress_pct INTEGER NOT NULL DEFAULT 0,
    extra_fields          JSONB NOT NULL DEFAULT '{}'::jsonb,
    version               BIGINT NOT NULL DEFAULT 0,
    created_by            UUID,
    updated_by            UUID,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_contract_number_per_unit UNIQUE (owning_unit_id, contract_number)
);

CREATE INDEX idx_contract_unit_status ON contract (owning_unit_id, status);
CREATE INDEX idx_contract_term_end ON contract (term_end);
CREATE INDEX idx_contract_payment_status ON contract (payment_status);
CREATE INDEX idx_contract_extra_fields ON contract USING GIN (extra_fields);
