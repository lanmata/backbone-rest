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

/**
 * Represents a managed client entity within a specific application context.
 *
 * This entity conforms to JPA specifications and is mapped to the
 * "managed_client" table in the "general" schema. Each instance of this
 * class provides metadata about a managed client, including its unique
 * identifier, associated application, OAuth2 scopes, and secret management
 * details.
 *
 * The entity supports lifecycle hooks such as {@link #prePersist()} and
 * {@link #preUpdate()} for automatically handling timestamps during persistence
 * and updates.
 *
 * Thread Safety: Instances of this class are not thread-safe and are intended
 * to be used within a transactional context managed by JPA.
 *
 * Constraints and Notes:
 * - The `id` field must be a caller-generated UUID and cannot be updated post-creation.
 * - The `name` field is human-readable, unique within the context of the application,
 *   and must not exceed 128 characters.
 * - OAuth2 `scopes` are stored as a PostgreSQL `TEXT[]` array and must be non-null.
 * - The previous secret hash (`prevSecretHash`) is nullable and relevant during
 *   secret rotation grace periods.
 * - Secret hashes should use BCrypt and are intended to remain private, not exposed
 *   through APIs.
 * - Timestamps (`createdAt`, `lastUpdatedAt`, `secretLastRotatedAt`) follow a UTC standard.
 */
@Entity
@Table(schema = "general", name = "managed_client")
public class ManagedClientEntity {

    /**
     * The unique identifier for the entity.
     * This value is a UUID and serves as the primary key.
     * It is non-nullable and cannot be updated once set.
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Represents the name of an entity. This field is mapped to the "name" column
     * in the database. It cannot be null and has a maximum length of 128 characters.
     */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * Represents a textual description with a maximum length of 512 characters.
     * This field is mapped to the "description" column in the database.
     */
    @Column(name = "description", length = 512)
    private String description;

    /**
     * Represents the unique identifier for an application.
     * This field is mapped to the "application_id" column in the database table.
     * It is a required field and cannot be null.
     */
    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    /**
     * Represents a hashed value intended to securely store sensitive information.
     * This field is mapped to the "secret_hash" column in the database.
     * The column is non-nullable and can store up to 255 characters.
     */
    @Column(name = "secret_hash", nullable = false, length = 255)
    private String secretHash;

    /**
     * Represents the hash value of the previously stored secret.
     * This field is mapped to the "prev_secret_hash" column in the database.
     *
     * The value is stored as a string with a maximum length of 255 characters.
     */
    @Column(name = "prev_secret_hash", length = 255)
    private String prevSecretHash;

    /**
     * Represents a list of scope identifiers associated with a specific context.
     * The scopes are stored as an array of text values in the database.
     * This field is mapped to a column in the database using the @JdbcTypeCode annotation
     * with a SQL type of ARRAY and is configured to not allow null values.
     *
     * Key characteristics:
     * - Database Column: "scopes"
     * - SQL Type: TEXT[]
     * - Nullability: Not nullable
     * - Maps to: List of strings
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scopes", nullable = false, columnDefinition = "TEXT[]")
    private List<String> scopes;

    /**
     * Indicates whether an entity is active or not.
     * This field is mapped to the "active" column in the database
     * and is marked as non-nullable.
     * By default, the value is set to {@code true}.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * The timestamp indicating when the entity was created.
     * This field is non-nullable and cannot be updated after the entity is created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Represents the timestamp indicating the last time an entity was updated.
     * This field is mapped to the "last_updated_at" column in the database.
     * It stores the date and time when the entity was last modified.
     */
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    /**
     * Represents the timestamp of the last rotation event for a secret.
     * This field is mapped to the "secret_last_rotated_at" column in the database.
     * It is used to track when the secret was last rotated for security purposes.
     */
    @Column(name = "secret_last_rotated_at")
    private LocalDateTime secretLastRotatedAt;

    /**
     * Default constructor for the ManagedClientEntity class.
     *
     * This constructor is provided for use by the JPA (Java Persistence API)
     * and should not be called directly in application logic. It initializes
     * an instance of the ManagedClientEntity without setting any state or properties.
     */
    public ManagedClientEntity() {
        // JPA
    }

    /**
     * Callback method to be invoked before the entity is persisted.
     * This method initializes the createdAt field with the current timestamp
     * if it has not already been set. It ensures that the creation time
     * is recorded for new entities.
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Callback method invoked before an update operation on the entity.
     * Sets the {@code lastUpdatedAt} field to the current date and time.
     *
     * Annotated with {@code @PreUpdate}, this method is automatically triggered
     * by the persistence provider before the entity is updated in the database.
     */
    @PreUpdate
    public void preUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    // CPD-OFF - entity getter/setter boilerplate intentionally mirrors DTO shapes

    /**
     * Retrieves the unique identifier associated with this object.
     *
     * @return the UUID representing the unique identifier of this object
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the object.
     *
     * @param id the UUID to be set as the unique identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns the client name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the client name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the optional description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the owning application ID.
     *
     * @return the applicationId
     */
    public UUID getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the owning application ID.
     *
     * @param applicationId the applicationId
     */
    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the BCrypt hash of the current secret. Never expose in API responses.
     *
     * @return the secretHash
     */
    public String getSecretHash() {
        return secretHash;
    }

    /**
     * Sets the BCrypt hash of the current secret.
     *
     * @param secretHash the secretHash
     */
    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    /**
     * Returns the BCrypt hash of the previous secret (during grace period).
     *
     * @return the prevSecretHash, or {@code null} if no rotation is in progress
     */
    public String getPrevSecretHash() {
        return prevSecretHash;
    }

    /**
     * Sets the BCrypt hash of the previous secret.
     *
     * @param prevSecretHash the prevSecretHash
     */
    public void setPrevSecretHash(String prevSecretHash) {
        this.prevSecretHash = prevSecretHash;
    }

    /**
     * Returns the authorised scopes list.
     *
     * @return the scopes
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Sets the authorised scopes list.
     *
     * @param scopes the scopes
     */
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    /**
     * Returns whether this client is active.
     *
     * @return {@code true} if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active flag.
     *
     * @param active {@code true} to enable, {@code false} to disable
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the record creation timestamp.
     *
     * @return the createdAt
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the record creation timestamp.
     *
     * @param createdAt the createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the last-updated timestamp.
     *
     * @return the lastUpdatedAt
     */
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    /**
     * Sets the last-updated timestamp.
     *
     * @param lastUpdatedAt the lastUpdatedAt
     */
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /**
     * Returns the timestamp of the most recent secret rotation.
     *
     * @return the secretLastRotatedAt
     */
    public LocalDateTime getSecretLastRotatedAt() {
        return secretLastRotatedAt;
    }

    /**
     * Sets the timestamp of the most recent secret rotation.
     *
     * @param secretLastRotatedAt the secretLastRotatedAt
     */
    public void setSecretLastRotatedAt(LocalDateTime secretLastRotatedAt) {
        this.secretLastRotatedAt = secretLastRotatedAt;
    }
    // CPD-ON
}
