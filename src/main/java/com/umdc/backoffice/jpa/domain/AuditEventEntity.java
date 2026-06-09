/*
 *  @(#)AuditEventEntity.java
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

/// JPA entity that maps to the {@code general.audit_event} PostgreSQL table.
/// <p>
/// Audit events are append-only records. Once persisted they are never updated.
/// The {@code details} column stores free-form JSON data as a {@code String};
/// Hibernate writes it using the {@code jsonb} column type via
/// {@link JdbcTypeCode} with {@link SqlTypes#JSON}.
/// </p>
@Entity
@Table(schema = "general", name = "audit_event")
public class AuditEventEntity {

    /// Primary key — caller-generated UUID.
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /// ID of the user who triggered the event.
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /// ID of the application context in which the event occurred.
    @Column(name = "application_id", updatable = false)
    private UUID applicationId;

    /// Discriminator for the type of security event.
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64, updatable = false)
    private AuditEventType eventType;

    /// IPv4 or IPv6 address of the client (max 45 chars to support IPv6).
    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    /// User-Agent header provided by the client (max 512 chars).
    @Column(name = "user_agent", length = 512, updatable = false)
    private String userAgent;

    /// Timestamp when the event actually occurred.
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    /// Arbitrary JSON details about the event stored as {@code jsonb}.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb", updatable = false)
    private String details;

    /// Timestamp when the record was inserted.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /// Default no-arg constructor required by JPA.
    public AuditEventEntity() {
        // JPA
    }

    /// Full constructor for programmatic creation.
    ///
    /// @param id            the UUID primary key
    /// @param userId        the user who triggered the event
    /// @param applicationId the application context (may be {@code null})
    /// @param eventType     the event discriminator
    /// @param ipAddress     client IP address (may be {@code null})
    /// @param userAgent     client user-agent string (may be {@code null})
    /// @param details       JSON string with extra details (may be {@code null})
    public AuditEventEntity(UUID id, UUID userId, UUID applicationId,
                             AuditEventType eventType,
                             String ipAddress, String userAgent, String details) {
        this.id = id;
        this.userId = userId;
        this.applicationId = applicationId;
        this.eventType = eventType;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
    }

    /// Sets {@code occurredAt} and {@code createdAt} to the current timestamp
    /// before the entity is first persisted, if they have not already been set.
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (occurredAt == null) {
            occurredAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
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

    /// Returns the user ID.
    ///
    /// @return the userId
    public UUID getUserId() {
        return userId;
    }

    /// Sets the user ID.
    ///
    /// @param userId the userId
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /// Returns the application ID.
    ///
    /// @return the applicationId
    public UUID getApplicationId() {
        return applicationId;
    }

    /// Sets the application ID.
    ///
    /// @param applicationId the applicationId
    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    /// Returns the event type.
    ///
    /// @return the eventType
    public AuditEventType getEventType() {
        return eventType;
    }

    /// Sets the event type.
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

    /// Returns the client user-agent string.
    ///
    /// @return the userAgent
    public String getUserAgent() {
        return userAgent;
    }

    /// Sets the client user-agent string.
    ///
    /// @param userAgent the userAgent
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

    /// Returns the record creation timestamp.
    ///
    /// @return the createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /// Sets the record creation timestamp.
    ///
    /// @param createdAt the createdAt
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

