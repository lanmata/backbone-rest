-- =============================================================================
-- Migration : V5__add_application_timestamps.sql
-- Description: Adds created_date and last_update columns to general.application.
--              Both columns use TIMESTAMP WITHOUT TIME ZONE to match the format
--              expected by the Application POJO (e.g. 2020-12-06 12:12:30).
--              Existing rows are backfilled with the current UTC time.
-- Schema     : general
-- Table      : application
-- Author     : backbone-rest
-- Date       : 2026-07-11
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Add columns
--    created_date : set once at insert — defaults to current UTC time.
--    last_update  : nullable; populated on every UPDATE by the application layer.
-- ---------------------------------------------------------------------------
ALTER TABLE general.application
    ADD COLUMN IF NOT EXISTS created_date TIMESTAMP WITHOUT TIME ZONE
        DEFAULT (NOW() AT TIME ZONE 'UTC'),
    ADD COLUMN IF NOT EXISTS last_update  TIMESTAMP WITHOUT TIME ZONE;

-- ---------------------------------------------------------------------------
-- 2. Backfill existing rows
--    created_date receives the current UTC instant as a reasonable sentinel.
--    last_update is left NULL — it will be set on the next actual update.
-- ---------------------------------------------------------------------------
UPDATE general.application
SET created_date = (NOW() AT TIME ZONE 'UTC')
WHERE created_date IS NULL;

-- ---------------------------------------------------------------------------
-- 3. Tighten the NOT NULL constraint on created_date now that backfill is done
-- ---------------------------------------------------------------------------
ALTER TABLE general.application
    ALTER COLUMN created_date SET NOT NULL;

-- ---------------------------------------------------------------------------
-- 4. Index on created_date — supports chronological listing and range queries
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_application_created_date
    ON general.application (created_date DESC);

-- ---------------------------------------------------------------------------
-- 5. Column comments
-- ---------------------------------------------------------------------------
COMMENT ON COLUMN general.application.created_date IS 'UTC timestamp when the application was registered; format: YYYY-MM-DD HH:MI:SS.';
COMMENT ON COLUMN general.application.last_update  IS 'UTC timestamp of the last metadata update; NULL until the first update.';
