/*
 *  @(#)NoticeType.java
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
package com.umdc.backoffice.v1.noticetypes.api.to;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Data transfer object representing a notice type.
 * Mirrors the {@code NoticeType} schema declared in {@code api.yaml}.
 */
public class NoticeType implements Serializable {

    private UUID id;
    private String name;
    private String description;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Default constructor.
     */
    public NoticeType() {
        // Default constructor
    }

    /**
     * Gets the notice type id.
     *
     * @return the notice type id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the notice type id.
     *
     * @param id the notice type id to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets the notice type name.
     *
     * @return the notice type name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the notice type name.
     *
     * @param name the notice type name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the notice type description.
     *
     * @return the notice type description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the notice type description.
     *
     * @param description the notice type description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the notice type active flag.
     *
     * @return the notice type active flag
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Sets the notice type active flag.
     *
     * @param active the notice type active flag to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return the last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt the last update timestamp to set
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
