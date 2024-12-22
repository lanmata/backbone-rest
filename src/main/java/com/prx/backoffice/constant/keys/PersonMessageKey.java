/*
 *  @(#)PersonMessageKey.java
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
package com.prx.backoffice.constant.keys;

import com.prx.commons.enums.types.MessageType;

/**
 * Enum representing various person message keys used in the application.
 * Implements the MessageType interface.
 * Provides message codes and status descriptions for different person-related operations.
 *
 * @version 1.0.1.20200904-01, 21-11-2020
 */
public enum PersonMessageKey implements MessageType {
    /**
     * Error during person creation.
     */
    PERSON_ERROR_CREATED(1, "Error durante la creación de persona"),

    /**
     * Operation successful.
     */
    PERSON_OK(200, "Ok"),

    /**
     * Person created successfully.
     */
    PERSON_CREATED(201, "persona creada"),

    /**
     * Person not found.
     */
    PERSON_NOT_FOUND(404, "Persona no encontrado"),

    /**
     * Error during person creation.
     */
    PERSON_CREATE_ERROR(404, "Error durante la creación de persona");

    private final int code;
    private final String status;

    /**
     * Constructor for PersonMessageKey enum.
     *
     * @param code the code of the message
     * @param status the status description of the message
     */
    PersonMessageKey(int code, String status) {
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
