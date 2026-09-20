-- =============================================================================
-- Migration : V4__extend_audit_event_check.sql
-- Description: Extends the audit_event_type_ck CHECK constraint on the
--              general.audit_event table to include the 9 new M2M event type
--              values added to AuditEventType enum for MCAM.
--              Additive-only — all existing event type values are preserved.
-- Schema     : public
-- Author     : backbone-rest / MCAM Phase 1
-- Date       : 2026-06-03
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Step 1: Drop the existing check constraint.
--         The V1 migration comment explicitly designed this as ALTER-able:
--         "Declared as a CHECK list rather than a PG ENUM so that new values
--          can be added via ALTER TABLE without a full table rewrite."
-- ---------------------------------------------------------------------------
ALTER TABLE general.audit_event
    DROP CONSTRAINT IF EXISTS audit_event_type_ck;

-- ---------------------------------------------------------------------------
-- Step 2: Re-add the constraint with the original 10 values plus 9 new
--         M2M event type values for MCAM.
-- ---------------------------------------------------------------------------
ALTER TABLE general.audit_event
    ADD CONSTRAINT audit_event_type_ck CHECK (
        event_type IN (
            -- Original values (V1__create_audit_event.sql)
            'LOGIN_SUCCESS',
            'LOGIN_FAILURE',
            'PASSWORD_CHANGE',
            'ROLE_ASSIGNED',
            'ROLE_REVOKED',
            'LOGOUT',
            'ACCOUNT_LOCKED',
            'ACCOUNT_UNLOCKED',
            'TOKEN_REFRESH',
            'PASSWORD_RESET_REQUEST',

            -- M2M MCAM values (MCAM Phase 1)
            'CLIENT_REGISTERED',
            'CLIENT_UPDATED',
            'CLIENT_DEACTIVATED',
            'CLIENT_DELETED',
            'CLIENT_SECRET_ROTATED',
            'CLIENT_TOKEN_ISSUED',
            'CLIENT_TOKEN_ISSUE_FAILED',
            'CLIENT_TOKEN_REVOKED',
            'CLIENT_INTROSPECTION_CALLED'
        )
    );

