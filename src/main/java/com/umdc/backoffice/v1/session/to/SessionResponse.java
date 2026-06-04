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

package com.umdc.backoffice.v1.session.to;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Data Transfer Object for session responses.
 * Contains the access token and an optional refresh token.
 *
 * @version 1.1.0, 20-10-2020
 */
@JsonPropertyOrder({
        "token",
        "refreshToken"
})
public class SessionResponse {
    @JsonProperty("token")
    private String token;

    @JsonProperty("refreshToken")
    private String refreshToken;

    /**
     * Default Constructor.
     */
    public SessionResponse() {
        super();
        // Default Constructor.
    }

    /**
     * Constructor with access token parameter.
     *
     * @param token the session access token
     */
    public SessionResponse(String token) {
        super();
        this.token = token;
    }

    /**
     * Constructor with access token and refresh token.
     *
     * @param token        the session access token
     * @param refreshToken the refresh token (longer-lived)
     */
    public SessionResponse(String token, String refreshToken) {
        super();
        this.token = token;
        this.refreshToken = refreshToken;
    }

    /**
     * Gets the session access token.
     *
     * @return the session access token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the session access token.
     *
     * @param token the session access token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the refresh token.
     *
     * @return the refresh token, or {@code null} if not set
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets the refresh token.
     *
     * @param refreshToken the refresh token
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Returns a string representation of the session response.
     *
     * @return a string representation of the session response
     */
    @Override
    public String toString() {
        return "SessionResponse{" +
                "token='" + token + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
