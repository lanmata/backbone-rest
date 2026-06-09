/*
 *  @(#)AuditEventType.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.umdc.backoffice.constant.types;

/// Enumeration of supported security audit event types.
/// <p>
/// These values map directly to the {@code CHECK} constraint defined on the
/// {@code general.audit_event.event_type} column in PostgreSQL.
/// </p>
public enum AuditEventType {

    /// Successful user authentication.
    LOGIN_SUCCESS,

    /// Failed authentication attempt.
    LOGIN_FAILURE,

    /// User-initiated password change.
    PASSWORD_CHANGE,

    /// A role was assigned to a user.
    ROLE_ASSIGNED,

    /// A role was removed from a user.
    ROLE_REVOKED,

    /// User explicitly logged out.
    LOGOUT,

    /// Account locked due to too many failed attempts.
    ACCOUNT_LOCKED,

    /// Account unlocked by an administrator.
    ACCOUNT_UNLOCKED,

    /// Session token was refreshed / renewed.
    TOKEN_REFRESH,

    /// A password reset request was initiated.
    PASSWORD_RESET_REQUEST,
    /// Managed client registered by an admin.
    CLIENT_REGISTERED,

    /// Managed client metadata updated.
    CLIENT_UPDATED,

    /// Managed client deactivated (active = false).
    CLIENT_DEACTIVATED,

    /// Managed client record deleted.
    CLIENT_DELETED,

    /// Client secret rotated; grace period started.
    CLIENT_SECRET_ROTATED,

    /// M2M access token issued successfully.
    CLIENT_TOKEN_ISSUED,

    /// M2M token issuance failed (bad credentials or inactive client).
    CLIENT_TOKEN_ISSUE_FAILED,

    /// All active tokens revoked for a client.
    CLIENT_TOKEN_REVOKED,

    /// Token introspection endpoint called.
    CLIENT_INTROSPECTION_CALLED

}

