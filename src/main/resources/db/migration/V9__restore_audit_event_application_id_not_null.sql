-- =============================================================================
-- Migration : V9__restore_audit_event_application_id_not_null.sql
-- Description: Restores the NOT NULL constraint on general.audit_event.application_id
--              that V8 dropped. V8 was the wrong fix — the real defect was that
--              AuditEventServiceImpl callers in UserServiceImpl (PASSWORD_CHANGE,
--              ROLE_ASSIGNED x2, ROLE_REVOKED) were hardcoding applicationId=null
--              instead of resolving it from UserEntity.getApplication() (the
--              user's owning application, NOT NULL at the entity level) or from
--              the ApplicationRoleUserEntity being linked/unlinked. All four call
--              sites now resolve a real applicationId, so the column can safely
--              go back to NOT NULL.
--              Guarded: aborts with a clear error instead of corrupting data if
--              any row still has a NULL application_id (backfill/delete those
--              first, then re-run).
-- Schema     : general
-- Author     : backbone-rest
-- Date       : 2026-09-29
-- =============================================================================

DO $$
DECLARE
    null_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_count FROM general.audit_event WHERE application_id IS NULL;
    IF null_count > 0 THEN
        RAISE EXCEPTION
            'Cannot restore NOT NULL on general.audit_event.application_id: % row(s) still have a NULL value. Backfill or delete them first, then re-run this migration.',
            null_count;
    END IF;
END $$;

ALTER TABLE general.audit_event
    ALTER COLUMN application_id SET NOT NULL;

COMMENT ON COLUMN general.audit_event.application_id IS 'Logical FK -> application entity.';
