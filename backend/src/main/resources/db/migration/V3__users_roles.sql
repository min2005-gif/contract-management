CREATE TABLE app_user (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_subject VARCHAR(255) NOT NULL UNIQUE,
    full_name        VARCHAR(255),
    email            VARCHAR(255),
    unit_id          UUID NOT NULL REFERENCES organizational_unit (id),
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_role (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES app_user (id),
    role          VARCHAR(30) NOT NULL,
    scope_unit_id UUID REFERENCES organizational_unit (id)
);

CREATE INDEX idx_user_role_user ON user_role (user_id);
