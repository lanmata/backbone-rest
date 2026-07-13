/*
 *  @(#)ManagedClientUpdateRequest.java
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
package com.umdc.backoffice.v1.managedclient.api.to;

import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for partially updating a managed client.
 * <p>
 * All fields are nullable — only non-null fields will be applied.
 * Used as the request body for {@code PUT /api/v1/managed-clients/{clientId}}.
 * </p>
 *
 * @author Luis Antonio Mata
 */
public class ManagedClientUpdateRequest {

    /**
     * New name for the client. Null means no change. Max 128 characters.
     */
    @Size(max = 128)
    private String name;

    /**
     * New description. Null means no change. Max 512 characters.
     */
    @Size(max = 512)
    private String description;

    /**
     * New set of authorised scopes. Null means no change.
     */
    private List<String> scopes;

    /**
     * New active flag. Null means no change.
     */
    private Boolean active;

    /**
     * Default constructor.
     */
    public ManagedClientUpdateRequest() {
        // Default constructor
    }

    /**
     * Returns the updated name, or null if unchanged.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the updated name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the updated description, or null if unchanged.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the updated description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the updated scopes list, or null if unchanged.
     *
     * @return the scopes
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Sets the updated scopes list.
     *
     * @param scopes the scopes
     */
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    /**
     * Returns the updated active flag, or null if unchanged.
     *
     * @return the active flag
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Sets the updated active flag.
     *
     * @param active the active flag
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
}

