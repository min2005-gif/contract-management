CREATE TABLE organizational_unit (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(50)  NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(20)  NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Seed the parent corporation (TCT) and 10 subordinate units.
INSERT INTO organizational_unit (code, name, type) VALUES
    ('TCT', 'Tổng công ty Quản lý bay Việt Nam', 'TCT'),
    ('U01', 'Đơn vị trực thuộc 01', 'SUBORDINATE'),
    ('U02', 'Đơn vị trực thuộc 02', 'SUBORDINATE'),
    ('U03', 'Đơn vị trực thuộc 03', 'SUBORDINATE'),
    ('U04', 'Đơn vị trực thuộc 04', 'SUBORDINATE'),
    ('U05', 'Đơn vị trực thuộc 05', 'SUBORDINATE'),
    ('U06', 'Đơn vị trực thuộc 06', 'SUBORDINATE'),
    ('U07', 'Đơn vị trực thuộc 07', 'SUBORDINATE'),
    ('U08', 'Đơn vị trực thuộc 08', 'SUBORDINATE'),
    ('U09', 'Đơn vị trực thuộc 09', 'SUBORDINATE'),
    ('U10', 'Đơn vị trực thuộc 10', 'SUBORDINATE');
