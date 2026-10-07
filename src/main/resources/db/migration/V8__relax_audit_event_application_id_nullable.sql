-- =============================================================================
-- Migration : V8__relax_audit_event_application_id_nullable.sql
-- Description: Drops the NOT NULL constraint on general.audit_event.application_id.
--              AuditEventEntity (com.umdc.persistence) maps this column WITHOUT
--              nullable=false — unlike user_id, which does set it — because
--              several event types are user-scoped, not application-scoped:
--              PASSWORD_CHANGE, ROLE_ASSIGNED and ROLE_REVOKED are all recorded
--              by UserServiceImpl with applicationId=null. V1 wrongly declared
--              the column NOT NULL, so every one of those inserts was failing
--              with a ConstraintViolationException (caught and logged by
--              AuditEventServiceImpl#saveRecord, never surfaced to the caller)
--              — the audit trail was silently losing these events.
-- Schema     : general
-- Author     : backbone-rest
-- Date       : 2026-09-29
-- =============================================================================

ALTER TABLE general.audit_event
    ALTER COLUMN application_id DROP NOT NULL;

COMMENT ON COLUMN general.audit_event.application_id IS
    'Logical FK -> application entity. Nullable: PASSWORD_CHANGE, ROLE_ASSIGNED and ROLE_REVOKED are user-scoped, not application-scoped.';
