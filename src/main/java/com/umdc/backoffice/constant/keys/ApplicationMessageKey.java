/*
 *  @(#)ApplicationMessageKey.java
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


import com.umdc.commons.constants.httpstatus.type.MessageType;

/**
 * Enum representing various user message keys used in the application.
 * Implements the MessageType interface.
 * Provides message codes and status descriptions for different user-related operations.
 *
 * @version 1.0.1.20200904-01, 21-11-2020
 */
public enum ApplicationMessageKey implements MessageType {
    /**
     * Error during application creation.
     */
    APPLICATION_ERROR_CREATED(1,"Application creation failed."),

    /**
     * Operation successful.
     */
    APPLICATION_OK(200,"Ok"),

    /**
     * Application created successfully.
     */
    APPLICATION_CREATED(201,"Application created successfully."),

    /**
     * Application blocked or inactive.
     */
    APPLICATION_BLOCKED(403,"Application is blocked or inactive."),

    /**
     * Application found.
     */
    APPLICATION_FOUND(200,"Application found."),

    /**
     * Application not found.
     */
    APPLICATION_NOT_FOUND(404,"Application not found."),

    /**
     * Error during application creation.
     */
    APPLICATION_CREATE_ERROR(400,"Invalid application request. Verify that all required fields are present."),

    /**
     * Application name already in use.
     */
    APPLICATION_PREVIOUS_EXIST(409,"Application name is already in use. Please choose a different name."),

    /**
     * Application updated successfully.
     */
    APPLICATION_UPDATED(200,"Application updated successfully."),

    /**
     * Application deleted successfully.
     */
    APPLICATION_DELETED(200,"Application deleted successfully.");

    private final int code;
    private final String status;

    /**
     * Constructor for ApplicationMessageKey enum.
     *
     * @param code the code of the message
     * @param status the status description of the message
     */
    ApplicationMessageKey(int code, String status){
        this.code = code;
        this.status = status;
    }

    /**
     * Gets the code of the message.
     *
     * @return the code of the message
     */
    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getCodeToString() {
        return String.valueOf(code);
    }

    /**
     * Gets the status description of the message.
     *
     * @return the status description of the message
     */
    @Override
    public String getStatus() {
        return status;
    }
}
