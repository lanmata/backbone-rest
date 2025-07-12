/*
 *  @(#)UserMessageKey.java
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


import com.prx.commons.constants.httpstatus.type.MessageType;

/**
 * Enum representing various user message keys used in the application.
 * Implements the MessageType interface.
 * Provides message codes and status descriptions for different user-related operations.
 *
 * @version 1.0.1.20200904-01, 21-11-2020
 */
public enum UserMessageKey implements MessageType {
    /**
     * Error during user creation.
     */
    USER_ERROR_CREATED(1,"Error durante la creación de usuario"),

    /**
     * Operation successful.
     */
    USER_OK(200,"Ok"),

    /**
     * User created successfully.
     */
    USER_CREATED(201,"Usuario creado"),

    /**
     * Invalid password.
     */
    USER_PASSWORD_WRONG(401,"Clave invalida"),

    /**
     * User blocked or inactive.
     */
    USER_BLOCKED(403,"Usuario bloqueado o inactivo"),

    /**
     * User found.
     */
    USER_FOUND(200,"Usuario encontrado"),

    /**
     * User not found.
     */
    USER_NOT_FOUND(404,"Usuario no encontrado"),

    /**
     * Error during user creation.
     */
    USER_CREATE_ERROR(404,"Error durante la creación de usuario"),

    /**
     * Username already exists.
     */
    USER_PREVIOUS_EXIST(409,"Nombre de usuario ya se encuentra ocupado, ingrese un nombre de usuario diferente");

    private final int code;
    private final String status;

    /**
     * Constructor for UserMessageKey enum.
     *
     * @param code the code of the message
     * @param status the status description of the message
     */
    UserMessageKey(int code, String status){
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
