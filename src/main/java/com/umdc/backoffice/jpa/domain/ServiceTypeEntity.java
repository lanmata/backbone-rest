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

/// JPA entity that maps to the {@code general.service_type} PostgreSQL table.
/// <p>
/// Represents a catalog entry for service types.
/// </p>
@Entity
@Table(schema = "general", name = "service_type")
public class ServiceTypeEntity {

    /// Primary key — caller-generated UUID.
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /// Human-readable name for the service type. Unique.
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /// Optional description of the service type.
    @Column(name = "description", length = 512)
    private String description;

    /// Whether this service type is active.
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /// Default no-arg constructor required by JPA.
    public ServiceTypeEntity() {
        // JPA
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

    /// Returns the service type name.
    ///
    /// @return the name
    public String getName() {
        return name;
    }

    /// Sets the service type name.
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

    /// Returns whether this service type is active.
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
    // CPD-ON
}
