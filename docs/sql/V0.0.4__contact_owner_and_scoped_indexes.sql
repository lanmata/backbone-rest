-- Scoped contacts and features (backbone-rest / persistence / commons 0.0.4)
--
-- hibernate.ddl-auto is 'none', so this script MUST be applied before deploying 0.0.4.
-- It is idempotent. Run it in a maintenance window: the plain CREATE INDEX statements below lock writes on
-- the table while they build; on a large table run them one by one with CREATE INDEX CONCURRENTLY outside
-- a transaction instead.

-- 1. External owners: a contact may belong to a subject that is neither a person nor an application
--    (e.g. a merchant of a commerce-index application), identified by (owner_type, owner_id) inside its
--    application_id.
ALTER TABLE general.contact
    ADD COLUMN IF NOT EXISTS owner_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS owner_id   UUID;

-- owner_type and owner_id are set together or not at all.
ALTER TABLE general.contact DROP CONSTRAINT IF EXISTS ck_contact_owner_pair;
ALTER TABLE general.contact
    ADD CONSTRAINT ck_contact_owner_pair CHECK ((owner_type IS NULL) = (owner_id IS NULL));

-- A contact has exactly one subject (person XOR external owner). Added NOT VALID so legacy rows do not block
-- the deployment; find them with the SELECT below, fix them, then run VALIDATE CONSTRAINT.
--   SELECT id FROM general.contact WHERE num_nonnulls(person_id, owner_id) <> 1;
ALTER TABLE general.contact DROP CONSTRAINT IF EXISTS ck_contact_single_subject;
ALTER TABLE general.contact
    ADD CONSTRAINT ck_contact_single_subject CHECK (num_nonnulls(person_id, owner_id) = 1) NOT VALID;
-- ALTER TABLE general.contact VALIDATE CONSTRAINT ck_contact_single_subject;

-- 2. Indexes backing the scoped queries.
-- Application directory (GET /contacts/?applicationId=...), filtered by type/status and ordered by type name.
CREATE INDEX IF NOT EXISTS ix_contact_application_type
    ON general.contact (application_id, contact_type_id);
-- A person's contacts, optionally within one application (GET /contacts/person/{id}?applicationId=...).
CREATE INDEX IF NOT EXISTS ix_contact_person_application
    ON general.contact (person_id, application_id);
-- An external owner's contacts (GET /contacts/owner/{type}/{id}); partial: person contacts do not pay for it.
CREATE INDEX IF NOT EXISTS ix_contact_owner_application
    ON general.contact (owner_type, owner_id, application_id) WHERE owner_id IS NOT NULL;

-- Feature -> roles reverse lookup (GET /features/{id}/roles). The primary key of role_feature is
-- (role_id, feature_id), so lookups by feature_id alone need their own index.
CREATE INDEX IF NOT EXISTS ix_role_feature_feature
    ON general.role_feature (feature_id);
-- Catalog search is 'contains' on lower(name): for a very large catalog add pg_trgm and a GIN index:
--   CREATE EXTENSION IF NOT EXISTS pg_trgm;
--   CREATE INDEX ix_feature_name_trgm ON general.feature USING gin (lower(name) gin_trgm_ops);
--   CREATE INDEX ix_contact_content_trgm ON general.contact USING gin (lower(content) gin_trgm_ops);
