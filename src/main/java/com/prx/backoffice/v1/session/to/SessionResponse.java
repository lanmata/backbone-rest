/*
 *  @(#)SessionResponse.java
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

package com.prx.backoffice.v1.session.to;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Data Transfer Object for session responses.
 * Contains the session token.
 *
 * @version 1.0.0, 20-10-2020
 */
@JsonPropertyOrder({
        "token"
})
public class SessionResponse {
    @JsonProperty("token")
    private String token;

    /**
     * Default Constructor.
     */
    public SessionResponse() {
        super();
        // Default Constructor.
    }

    /**
     * Constructor with token parameter.
     *
     * @param token the session token
     */
    public SessionResponse(String token) {
        super();
        this.token = token;
    }

    /**
     * Gets the session token.
     *
     * @return the session token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the session token.
     *
     * @param token the session token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns a string representation of the session response.
     *
     * @return a string representation of the session response
     */
    @Override
    public String toString() {
        return "UserAccessResponse{" +
                "token='" + token + '\'' +
                '}';
    }
}
