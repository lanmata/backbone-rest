/*
 *  @(#)AuthKey.java
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

package com.umdc.backoffice.constant.keys;

/**
 * Enum representing various authentication keys used in the application.
 */
public enum AuthKey {
    /**
     * Key for features.
     */
    FEATURES_ID("features"),

    /**
     * Key for session ID.
     */
    SESSION_ID("sessionId"),

    /**
     * Key for username.
     */
    USERNAME("username"),

    /**
     * Key for first name.
     */
    FIRSTNAME("firstname"),

    /**
     * Key for last name.
     */
    LASTNAME("lastname"),

    /**
     * Key for roles.
     */
    ROLES_ID("roles"),

    /**
     * Key for type.
     */
    TYPE("type"),

    /**
     * Key for issued at time.
     */
    IAT("iat"),

    /**
     * Key for JWT ID.
     */
    JTI("jti"),

    /**
     * Key for user ID.
     */
    USER_ID("uid"),

    /**
     * Key for alias.
     */
    ALIAS("alias"),

    /**
     * Application the session was opened for ({@code applicationId} of the login request). A resource server that
     * belongs to one application must require it to equal its own id: every service shares the signing secret, so
     * without this claim a session of application A would be accepted by application B.
     */
    APPLICATION_ID("aid");

    /**
     * The value of the key.
     */
    public final String value;

    /**
     * Constructor for AuthKey enum.
     *
     * @param key the value of the key
     */
    AuthKey(String key) {
        this.value = key;
    }
}
