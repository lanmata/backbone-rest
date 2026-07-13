/*
 *  @(#)ServiceTypeMessageKey.java
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
 * Enum representing message keys for service type operations.
 * Implements the MessageType interface.
 *
 * @version 1.0.0, 11-07-2026
 */
public enum ServiceTypeMessageKey implements MessageType {

    /**
     * Operation successful.
     */
    SERVICE_TYPE_OK(200, "Ok"),

    /**
     * Service type created successfully.
     */
    SERVICE_TYPE_CREATED(201, "Service type created"),

    /**
     * Service type found.
     */
    SERVICE_TYPE_FOUND(200, "Service type found"),

    /**
     * Service type not found.
     */
    SERVICE_TYPE_NOT_FOUND(404, "Service type not found"),

    /**
     * Service type name already in use.
     */
    SERVICE_TYPE_ALREADY_EXISTS(409, "Service type name already in use"),

    /**
     * Error creating service type.
     */
    SERVICE_TYPE_CREATE_ERROR(400, "Error creating service type"),

    /**
     * Error updating service type.
     */
    SERVICE_TYPE_UPDATE_ERROR(400, "Error updating service type");

    private final int code;
    private final String status;

    /**
     * Constructor for ServiceTypeMessageKey enum.
     *
     * @param code   the code of the message
     * @param status the status description of the message
     */
    ServiceTypeMessageKey(int code, String status) {
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
