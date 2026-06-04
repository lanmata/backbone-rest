/*
 *  @(#)ManagedClientAuditEventEntity.java
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
package com.umdc.backoffice.jpa.domain;

import com.umdc.backoffice.constant.types.AuditEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/// JPA entity that maps to the {@code public.managed_client_audit_event} table.
/// <p>
/// Audit events are append-only records. Once persisted they are never updated.
/// Reuses the {@link AuditEventType} enum — the 9 M2M constants are additive.
/// </p>
@Entity
@Table(schema = "general", name = "managed_client_audit_event")
public class ManagedClientAuditEventEntity {

    /// Primary key — caller-generated UUID.
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /// Logical FK to the managed client that generated this event.
    @Column(name = "client_id", nullable = false, updatable = false)
    private UUID clientId;

    /// Discriminator for the type of M2M lifecycle event.
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64, updatable = false)
    private AuditEventType eventType;

    /// IPv4 or IPv6 address of the client (max 45 chars to support IPv6).
    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    /// Result of the action: SUCCESS or FAILURE.
    @Column(name = "outcome", nullable = false, length = 16, updatable = false)
    private String outcome;

    /// Arbitrary JSON details about the event stored as {@code jsonb}.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb", updatable = false)
    private String details;

    /// Timestamp when the event occurred.
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    /// Default no-arg constructor required by JPA.
    public ManagedClientAuditEventEntity() {
        // JPA
    }

    /// Sets {@code occurredAt} to the current timestamp before the entity is
    /// first persisted, if it has not already been set.
    @PrePersist
    public void prePersist() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    /// Returns the primary key.
    ///
    /// @return the UUID id
    public UUID getId() {
        return id;
    }

    /// Sets the primary key.
    ///
    /// @param id the UUID id
    public void setId(UUID id) {
        this.id = id;
    }

    /// Returns the managed client ID that triggered this event.
    ///
    /// @return the clientId
    public UUID getClientId() {
        return clientId;
    }

    /// Sets the managed client ID.
    ///
    /// @param clientId the clientId
    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    /// Returns the event type discriminator.
    ///
    /// @return the eventType
    public AuditEventType getEventType() {
        return eventType;
    }

    /// Sets the event type discriminator.
    ///
    /// @param eventType the eventType
    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    /// Returns the client IP address.
    ///
    /// @return the ipAddress
    public String getIpAddress() {
        return ipAddress;
    }

    /// Sets the client IP address.
    ///
    /// @param ipAddress the ipAddress
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /// Returns the outcome string (SUCCESS or FAILURE).
    ///
    /// @return the outcome
    public String getOutcome() {
        return outcome;
    }

    /// Sets the outcome string.
    ///
    /// @param outcome the outcome
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    /// Returns the JSON details string.
    ///
    /// @return the details
    public String getDetails() {
        return details;
    }

    /// Sets the JSON details string.
    ///
    /// @param details the details
    public void setDetails(String details) {
        this.details = details;
    }

    /// Returns the timestamp when the event occurred.
    ///
    /// @return the occurredAt
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /// Sets the timestamp when the event occurred.
    ///
    /// @param occurredAt the occurredAt
    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}

