/*
 *  @(#)ServiceTypeEntity.java
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
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity representing a service type in the system.
 *
 * <p>
 * The {@code ServiceTypeEntity} class is a JPA entity mapped to the {@code general.service_type}
 * table. It holds data related to a specific type of service, such as its unique identifier,
 * name, description, and active state.
 * </p>
 *
 * <p>
 * This entity is primarily used for categorizing or grouping services within the
 * application, enabling structured management and identification of services.
 * </p>
 *
 * Annotations:
 * - {@code @Entity}: Marks this class as a JPA entity.
 * - {@code @Table}: Specifies the schema and table name ({@code general.service_type}).
 */
@Entity
@Table(schema = "general", name = "service_type")
public class ServiceTypeEntity {

    /**
     * Represents the unique identifier for the service type entity.
     *
     * This field serves as the primary key for the {@code ServiceTypeEntity} class
     * and is mapped to the {@code id} column in the database table.
     * It is immutable once set and cannot be updated.
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Represents the name of the service type.
     *
     * This field is mapped to the {@code name} column in the database table and is a mandatory field.
     * The name provides a human-readable identifier for the service type.
     * It cannot exceed 128 characters in length and must not be null.
     */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * Represents a textual description of the service type.
     *
     * This field is mapped to the {@code description} column in the database table.
     * It provides additional information about the service type and is optional.
     * The maximum length for this field is 512 characters.
     */
    @Column(name = "description", length = 512)
    private String description;

    /**
     * Indicates whether the service type is active or inactive.
     *
     * This field is mapped to the {@code active} column in the database and
     * is a required field (cannot be null).
     * By default, the value is set to {@code true}.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * Default constructor used by JPA.
     * <p>
     * Initializes a new instance of the {@code ServiceTypeEntity} class.
     * </p>
     */
    public ServiceTypeEntity() {
        // JPA
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    /**
     * Retrieves the unique identifier of the service type.
     *
     * @return the UUID representing the primary key of the service type
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the service type.
     *
     * @param id the UUID representing the primary key of the service type
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Retrieves the name of the service type.
     *
     * @return the human-readable name of the service type
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the service type.
     *
     * @param name the human-readable name for the service type
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the description of the service type.
     *
     * @return the description of the service type, or null if no description is set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the service type.
     *
     * @param description the description of the service type
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Determines whether the service type is active.
     *
     * @return {@code true} if the service type is active, {@code false} otherwise
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
    // CPD-ON
}
