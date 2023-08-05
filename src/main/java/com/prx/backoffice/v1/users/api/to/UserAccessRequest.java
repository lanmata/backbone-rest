/*
 * @(#)$file.className.java.
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
package com.prx.backoffice.v1.users.api.to;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.prx.commons.to.Request;
import jakarta.validation.constraints.NotNull;

@JsonNaming
public class UserAccessRequest extends Request {
    @NotNull
    @JsonProperty("alias")
    private String alias;
    @NotNull
    @JsonProperty("password")
    private String password;

    /**
     * Default Constructor.
     */
    public UserAccessRequest() {
        super();
        // Default Constructor.
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserAccessRequest{" +
                "alias='" + alias + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
