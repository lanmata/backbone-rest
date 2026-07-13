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

/**
 * Entity representing an audit event for logging and security purposes.
 * This class maps to the {@code general.audit_event} table in the database.
 *
 * Each instance of this entity records information about a specific security-
 * related event triggered by a user or within an application context.
 * It includes details such as the type of event, the user and application
 * involved, the client's IP address, and a timestamp when the event occurred.
 *
 * Key Features:
 * - Stores a caller-generated UUID as the primary key.
 * - Includes information about the user, application, and type of event.
 * - Captures client metadata such as IP address and user-agent.
 * - Provides a JSON field for event-specific details.
 * - Automatically sets timestamps for event occurrence and record creation.
 *
 * This class leverages JPA annotations for ORM and Hibernate-specific
 * annotations for JSON support.
 */
@Entity
@Table(schema = "general", name = "audit_event")
public class AuditEventEntity {

    /**
     * Unique identifier for the entity.
     *
     * This field is annotated with {@code @Id} to mark it as the primary key
     * and with {@code @Column} to specify database column constraints. It is
     * of type {@code UUID} to ensure globally unique identification.
     *
     * Characteristics:
     * - Non-nullable: This field must always have a value.
     * - Non-updatable: The value cannot be changed after insertion.
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Represents the unique identifier for a user in the system.
     * <ul>
     *   <li>Mapped to the "user_id" column in the database.</li>
     *   <li>Cannot be null, ensuring every user is associated with a unique identifier.</li>
     *   <li>Immutable after creation; the value cannot be updated once set.</li>
     * </ul>
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /**
     * Represents the unique identifier of the application to which an audit event is associated.
     *
     * This field is used as a foreign key in database operations to link audit events to a specific application.
     * It is marked as non-updatable to ensure the integrity of the relationship between audit events and applications.
     */
    @Column(name = "application_id", updatable = false)
    private UUID applicationId;

    /**
     * The type of audit event associated with the entity.
     *
     * This variable represents a specific type of event that is being audited,
     * such as CREATE, UPDATE, DELETE, or any other event applicable to the business domain.
     * It is stored as a String in the database with constraints on length and immutability.
     *
     * Constraints:
     * - Column name: "event_type".
     * - Cannot be null.
     * - Maximum length: 64 characters.
     * - Non-updatable after entity creation.
     *
     * Mapped using JPA's @Enumerated annotation to ensure
     * that the enumeration constant is stored as a string value in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64, updatable = false)
    private AuditEventType eventType;

    /**
     * Represents the IP address associated with an audit event.
     *
     * This field is stored in the "ip_address" column of the database,
     * has a maximum length of 45 characters, and is not updatable
     * once the entity is persisted.
     */
    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    /**
     * Represents the user agent string associated with a specific audit event.
     * The user agent string typically contains information about the client
     * application making the request, such as the browser type and version,
     * operating system, and device details.
     *
     * <ul>
     * - Mapped to the "user_agent" column in the database.
     * - Restricted to a maximum length of 512 characters.
     * - Field is non-updatable once it has been set.
     * </ul>
     */
    @Column(name = "user_agent", length = 512, updatable = false)
    private String userAgent;

    /**
     * The date and time when the event occurred.
     *
     * <ul>
     *     <li>Mapped to the {@code occurred_at} column in the database.</li>
     *     <li>Cannot be null.</li>
     *     <li>Immutable after creation (not updatable).</li>
     * </ul>
     */
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    /**
     * Represents a JSON-formatted string storing additional details for the entity.
     *
     * This field is mapped to a database column of type JSONB and is not updatable
     * after the entity is persisted. It uses a Hibernate-specific annotation to indicate
     * the JSON data type for database interactions.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb", updatable = false)
    private String details;

    /**
     * Records the timestamp when the entity was created.
     * This field is automatically populated and is immutable once set.
     * It is useful for auditing purposes to determine the creation time of the entity.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Represents an entity for auditing events in the system.
     *
     * This no-argument constructor is provided to support JPA requirements.
     * It allows the persistence framework to create instances of this class
     * through reflection.
     */
    public AuditEventEntity() {
        // JPA
    }

    /**
     * Represents an audit event entity with details about a specific action or occurrence.
     *
     * @param id            the unique identifier for the audit event
     * @param userId        the unique identifier of the user associated with the audit event
     * @param applicationId the unique identifier of the application associated with the audit event
     * @param eventType     the type of audit event
     * @param ipAddress     the IP address from where the audit event originated
     * @param userAgent     the user agent information from the client triggering the audit event
     * @param details       additional details or metadata about the audit event
     */
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

    /**
     * Callback method invoked before the entity is persisted.
     *
     * This method is annotated with {@code @PrePersist}, which ensures it is executed
     * automatically by the persistence provider before the entity is inserted into the database.
     * It is used to initialize the following timestamps:
     *
     * - {@code occurredAt}: Set to the current timestamp if it is not already populated.
     * - {@code createdAt}: Set to the current timestamp if it is not already populated.
     *
     * This method ensures that these timestamp fields are properly initialized
     * if they have not already been set prior to persistence.
     */
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

    /**
     * Retrieves the unique identifier associated with this entity.
     *
     * @return the UUID representing the unique identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the primary key.
     *
     * @param id the UUID id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns the user ID.
     *
     * @return the userId
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId the userId
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Returns the application ID.
     *
     * @return the applicationId
     */
    public UUID getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID.
     *
     * @param applicationId the applicationId
     */
    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the event type.
     *
     * @return the eventType
     */
    public AuditEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the event type.
     *
     * @param eventType the eventType
     */
    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the client IP address.
     *
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the client IP address.
     *
     * @param ipAddress the ipAddress
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Returns the client user-agent string.
     *
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the client user-agent string.
     *
     * @param userAgent the userAgent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Returns the timestamp when the event occurred.
     *
     * @return the occurredAt
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * Sets the timestamp when the event occurred.
     *
     * @param occurredAt the occurredAt
     */
    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    /**
     * Returns the JSON details string.
     *
     * @return the details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the JSON details string.
     *
     * @param details the details
     */
    public void setDetails(String details) {
        this.details = details;
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
}

