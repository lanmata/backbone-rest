CREATE TABLE IF NOT EXISTS general.service_type (
    id          UUID         NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    active      BOOLEAN      NOT NULL DEFAULT true,
    CONSTRAINT service_type_pk PRIMARY KEY (id),
    CONSTRAINT service_type_name_uq UNIQUE (name)
);
CREATE INDEX IF NOT EXISTS idx_service_type_active ON general.service_type (active);
COMMENT ON TABLE general.service_type IS 'Catalog of service types.';
