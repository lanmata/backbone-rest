-- =============================================================================
-- Migration : V6__add_creator_to_application_role_user.sql
-- Description: Adds creator_id and created_date columns to
--              general.application_role_user.
--              creator_id  — UUID of the user who created the assignment.
--              created_date — UTC timestamp of when the assignment was created.
--              Existing rows are backfilled with a NULL creator and the current
--              UTC instant respectively.
-- Schema     : general
-- Table      : application_role_user
-- Author     : backbone-rest
-- Date       : 2026-07-11
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Add columns
--    creator_id   : nullable — backfill leaves historical rows as NULL.
--    created_date : defaults to current UTC time so future inserts are covered
--                   without application-layer changes.
-- ---------------------------------------------------------------------------
ALTER TABLE general.application_role_user
    ADD COLUMN IF NOT EXISTS creator_id   UUID,
    ADD COLUMN IF NOT EXISTS created_date TIMESTAMP WITHOUT TIME ZONE
        DEFAULT (NOW() AT TIME ZONE 'UTC');

-- ---------------------------------------------------------------------------
-- 2. Backfill existing rows
--    created_date receives the current UTC instant as a reasonable sentinel.
--    creator_id is left NULL — no reliable way to derive the original actor.
-- ---------------------------------------------------------------------------
UPDATE general.application_role_user
SET created_date = (NOW() AT TIME ZONE 'UTC')
WHERE created_date IS NULL;

-- ---------------------------------------------------------------------------
-- 3. Tighten the NOT NULL constraint on created_date now that backfill is done
-- ---------------------------------------------------------------------------
ALTER TABLE general.application_role_user
    ALTER COLUMN created_date SET NOT NULL;

-- ---------------------------------------------------------------------------
-- 4. Indexes
--    created_date — supports chronological listing and range queries.
--    creator_id   — supports lookups of assignments made by a specific user.
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_application_role_user_created_date
    ON general.application_role_user (created_date DESC);

CREATE INDEX IF NOT EXISTS idx_application_role_user_creator_id
    ON general.application_role_user (creator_id)
    WHERE creator_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- 5. Column comments
-- ---------------------------------------------------------------------------
COMMENT ON COLUMN general.application_role_user.creator_id
    IS 'UUID of the user who created this role assignment; logical FK to the user entity. NULL for assignments that predate this migration.';

COMMENT ON COLUMN general.application_role_user.created_date
    IS 'UTC timestamp when the role assignment was created; format: YYYY-MM-DD HH:MI:SS. Historical rows are backfilled with the migration run time.';
