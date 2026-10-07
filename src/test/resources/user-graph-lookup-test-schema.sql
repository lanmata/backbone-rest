-- Minimal H2-native schema for UserGraphLookupServiceImplIntegrationTest.
--
-- Hand-written rather than Hibernate-auto-generated (ddl-auto=none): the real entities carry
-- Postgres-specific @ColumnDefault expressions (e.g. "general.uuid_generate_v4()",
-- "'N/A'::character varying") that H2 cannot parse, and "user" is an H2 reserved word — both
-- sidestepped here (no defaults needed for values this test always sets explicitly; "user" is
-- un-reserved via the NON_KEYWORDS=USER connection parameter instead of quoting).
CREATE SCHEMA IF NOT EXISTS general;

CREATE TABLE general.application (
    id              UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    code_name       VARCHAR(20)  NOT NULL,
    description     VARCHAR(255) NOT NULL,
    service_type_id UUID         NOT NULL,
    active          BOOLEAN      NOT NULL,
    created_date    TIMESTAMP,
    last_update     TIMESTAMP
);

CREATE TABLE general.role (
    id             UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    VARCHAR(255) NOT NULL,
    active         BOOLEAN      NOT NULL,
    application_id UUID         NOT NULL REFERENCES general.application (id)
);

CREATE TABLE general.feature (
    id          UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN      NOT NULL
);

-- Empty on purpose: RoleEntity.roleFeatures is mapped EAGER on the real entity, so Hibernate
-- always issues a SELECT against this table when a role loads, even though this test never
-- populates or asserts on it.
CREATE TABLE general.role_feature (
    role_id    UUID    NOT NULL REFERENCES general.role (id),
    feature_id UUID    NOT NULL REFERENCES general.feature (id),
    active     BOOLEAN NOT NULL,
    PRIMARY KEY (role_id, feature_id)
);

CREATE TABLE general.contact_type (
    id          UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN      NOT NULL
);

CREATE TABLE general.person (
    id          UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    first_name  VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    last_name   VARCHAR(255),
    gender      VARCHAR(255) NOT NULL,
    birthdate   DATE         NOT NULL
);

CREATE TABLE general.user (
    id                         UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    alias                      VARCHAR(255) NOT NULL,
    password                   VARCHAR(255) NOT NULL,
    email_account              VARCHAR(255) NOT NULL,
    display_name               VARCHAR(255) NOT NULL,
    active                     BOOLEAN      NOT NULL,
    created_date               TIMESTAMP    NOT NULL,
    last_update                TIMESTAMP    NOT NULL,
    notification_email_active  BOOLEAN      NOT NULL,
    notification_sms_active    BOOLEAN      NOT NULL,
    privacy_data_out_active    BOOLEAN      NOT NULL,
    person_id                  UUID REFERENCES general.person (id),
    application_id             UUID NOT NULL REFERENCES general.application (id)
);

CREATE TABLE general.contact (
    id              UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    content         VARCHAR(255) NOT NULL,
    active          BOOLEAN      NOT NULL,
    contact_type_id UUID REFERENCES general.contact_type (id),
    person_id       UUID REFERENCES general.person (id),
    application_id  UUID REFERENCES general.application (id)
);

CREATE TABLE general.application_role_user (
    user_id          UUID NOT NULL REFERENCES general.user (id),
    role_id          UUID NOT NULL REFERENCES general.role (id),
    application_id   UUID NOT NULL REFERENCES general.application (id),
    active           BOOLEAN NOT NULL,
    profile_image_ref VARCHAR(255),
    creator_id       UUID,
    created_date     TIMESTAMP,
    PRIMARY KEY (user_id, role_id, application_id)
);
