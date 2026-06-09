/*
 *  @(#)ManagedClientCreateRequest.java
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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/// Request DTO for registering a new managed client.
/// <p>
/// Used as the request body for {@code POST /api/v1/managed-clients}.
/// </p>
///
/// @author Luis Antonio Mata
public class ManagedClientCreateRequest {

    /// Human-readable name for the client. Required. Max 128 characters.
    /// Must be unique within an application.
    @NotBlank
    @Size(max = 128)
    private String name;

    /// Optional description of the client purpose. Max 512 characters.
    @Size(max = 512)
    private String description;

    /// UUID of the application that owns this client. Required.
    @NotNull
    private UUID applicationId;

    /// Authorised OAuth2 scopes. Required. Must contain at least one scope.
    @NotEmpty
    private List<@NotBlank String> scopes;

    /// Whether the client is active upon registration. Defaults to true.
    private boolean active = true;

    /// Default constructor.
    public ManagedClientCreateRequest() {
        // Default constructor
    }

    // CPD-OFF - request DTO getter/setter boilerplate intentionally mirrors sibling DTOs
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

    /// Returns the description.
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

    /// Returns the owning application UUID.
    ///
    /// @return the applicationId
    public UUID getApplicationId() {
        return applicationId;
    }

    /// Sets the owning application UUID.
    ///
    /// @param applicationId the applicationId
    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
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

    /// Returns whether the client is active.
    ///
    /// @return {@code true} if active
    public boolean isActive() {
        return active;
    }

    /// Sets the active flag.
    ///
    /// @param active {@code true} to enable
    public void setActive(boolean active) {
        this.active = active;
    }
    // CPD-ON
}

