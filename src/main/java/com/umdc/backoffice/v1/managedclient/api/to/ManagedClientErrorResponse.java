/*
 *  @(#)ManagedClientErrorResponse.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublogue, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.umdc.backoffice.v1.managedclient.api.to;

/// Structured error response DTO for MCAM endpoints.
/// <p>
/// All MCAM error responses use this DTO to provide machine-readable error codes
/// compatible with OAuth2 error response conventions (RFC 6749 §5.2).
/// </p>
///
/// @author Luis Antonio Mata
public class ManagedClientErrorResponse {

    /// Machine-readable error code (e.g. "invalid_client", "invalid_scope").
    private String error;

    /// Human-readable description of the error.
    private String errorDescription;

    /// Optional: the client UUID involved (omitted when not applicable).
    private String clientId;

    /// Default constructor.
    public ManagedClientErrorResponse() {
        // Default constructor
    }

    /// Convenience constructor for error + description only.
    ///
    /// @param error            the error code
    /// @param errorDescription the human-readable description
    public ManagedClientErrorResponse(String error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
    }

    /// Convenience constructor for all three fields.
    ///
    /// @param error            the error code
    /// @param errorDescription the human-readable description
    /// @param clientId         the client UUID (as string)
    public ManagedClientErrorResponse(String error, String errorDescription, String clientId) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.clientId = clientId;
    }

    /// Returns the error code.
    ///
    /// @return the error
    public String getError() {
        return error;
    }

    /// Sets the error code.
    ///
    /// @param error the error
    public void setError(String error) {
        this.error = error;
    }

    /// Returns the error description.
    ///
    /// @return the errorDescription
    public String getErrorDescription() {
        return errorDescription;
    }

    /// Sets the error description.
    ///
    /// @param errorDescription the errorDescription
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /// Returns the optional client UUID.
    ///
    /// @return the clientId, or {@code null} if not applicable
    public String getClientId() {
        return clientId;
    }

    /// Sets the optional client UUID.
    ///
    /// @param clientId the clientId
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}

