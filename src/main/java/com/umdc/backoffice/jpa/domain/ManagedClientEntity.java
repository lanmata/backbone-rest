/*
 *  @(#)ManagedClientEntity.java
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/// JPA entity that maps to the {@code public.managed_client} PostgreSQL table.
/// <p>
/// Represents an M2M application credential registration managed by MCAM.
/// Secret fields ({@code secretHash}, {@code prevSecretHash}) are persisted
/// here but must never be exposed in API responses.
/// </p>
@Entity
@Table(schema = "general", name = "managed_client")
public class ManagedClientEntity {

    /// Primary key — caller-generated UUID.
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /// Human-readable name for the client. Unique within an application.
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /// Optional description of the client purpose.
    @Column(name = "description", length = 512)
    private String description;

    /// Logical FK to the application that owns this client.
    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    /// BCrypt hash of the current active client secret. Never expose via API.
    @Column(name = "secret_hash", nullable = false, length = 255)
    private String secretHash;

    /// BCrypt hash of the previous secret during rotation grace period. Nullable.
    @Column(name = "prev_secret_hash", length = 255)
    private String prevSecretHash;

    /// Authorised OAuth2 scopes stored as a native PostgreSQL TEXT[] array.
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scopes", nullable = false, columnDefinition = "TEXT[]")
    private List<String> scopes;

    /// Whether the client is allowed to issue tokens.
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /// UTC timestamp when the record was created.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /// UTC timestamp of the last metadata update.
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    /// UTC timestamp of the most recent secret rotation.
    @Column(name = "secret_last_rotated_at")
    private LocalDateTime secretLastRotatedAt;

    /// Default no-arg constructor required by JPA.
    public ManagedClientEntity() {
        // JPA
    }

    /// Sets {@code createdAt} to the current timestamp before the entity is
    /// first persisted, if it has not already been set.
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /// Sets {@code lastUpdatedAt} to the current timestamp before every update.
    @PreUpdate
    public void preUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    // CPD-OFF - entity getter/setter boilerplate intentionally mirrors DTO shapes
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

    /// Returns the client name.
    ///
    /// @return the name
    public String getName() {
        return name;
    }

    /// Sets the client name.
    ///
    /// @param name the name
    public void setName(String name) {
        this.name = name;
    }

    /// Returns the optional description.
    ///
    /// @return the description
    public String getDescription() {
        return description;
    }

    /// Sets the description.
    ///
    /// @param description the description
    public void setDescription(String description) {
        this.description = description;
    }

    /// Returns the owning application ID.
    ///
    /// @return the applicationId
    public UUID getApplicationId() {
        return applicationId;
    }

    /// Sets the owning application ID.
    ///
    /// @param applicationId the applicationId
    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    /// Returns the BCrypt hash of the current secret. Never expose in API responses.
    ///
    /// @return the secretHash
    public String getSecretHash() {
        return secretHash;
    }

    /// Sets the BCrypt hash of the current secret.
    ///
    /// @param secretHash the secretHash
    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    /// Returns the BCrypt hash of the previous secret (during grace period).
    ///
    /// @return the prevSecretHash, or {@code null} if no rotation is in progress
    public String getPrevSecretHash() {
        return prevSecretHash;
    }

    /// Sets the BCrypt hash of the previous secret.
    ///
    /// @param prevSecretHash the prevSecretHash
    public void setPrevSecretHash(String prevSecretHash) {
        this.prevSecretHash = prevSecretHash;
    }

    /// Returns the authorised scopes list.
    ///
    /// @return the scopes
    public List<String> getScopes() {
        return scopes;
    }

    /// Sets the authorised scopes list.
    ///
    /// @param scopes the scopes
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    /// Returns whether this client is active.
    ///
    /// @return {@code true} if active
    public boolean isActive() {
        return active;
    }

    /// Sets the active flag.
    ///
    /// @param active {@code true} to enable, {@code false} to disable
    public void setActive(boolean active) {
        this.active = active;
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

    /// Returns the last-updated timestamp.
    ///
    /// @return the lastUpdatedAt
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    /// Sets the last-updated timestamp.
    ///
    /// @param lastUpdatedAt the lastUpdatedAt
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /// Returns the timestamp of the most recent secret rotation.
    ///
    /// @return the secretLastRotatedAt
    public LocalDateTime getSecretLastRotatedAt() {
        return secretLastRotatedAt;
    }

    /// Sets the timestamp of the most recent secret rotation.
    ///
    /// @param secretLastRotatedAt the secretLastRotatedAt
    public void setSecretLastRotatedAt(LocalDateTime secretLastRotatedAt) {
        this.secretLastRotatedAt = secretLastRotatedAt;
    }
    // CPD-ON
}
