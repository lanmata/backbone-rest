/*
 * @(#)SessionRequest.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */
package com.prx.backoffice.v1.session.to;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.prx.commons.to.Request;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for session requests.
 * Contains user alias and password for session creation.
 *
 * @version 1.0.0, 20-10-2020
 */
@JsonNaming
public class SessionRequest extends Request {
    @NotNull
    @JsonProperty("alias")
    private String alias;
    @NotNull
    @JsonProperty("password")
    private String password;

    /**
     * Default Constructor.
     */
    public SessionRequest() {
        super();
        // Default Constructor.
    }

    /**
     * Gets the user alias.
     *
     * @return the user alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the user alias.
     *
     * @param alias the user alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets the user password.
     *
     * @return the user password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user password.
     *
     * @param password the user password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns a string representation of the session request.
     *
     * @return a string representation of the session request
     */
    @Override
    public String toString() {
        return "UserAccessRequest{" +
                "alias='" + alias + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
