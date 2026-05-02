/*
 *  @(#)RoleMessageKey.java
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
 * Enum representing various role message keys used in the application.
 * Implements the MessageType interface.
 * Provides message codes and status descriptions for different role-related operations.
 *
 * @version 1.0.0, 12-02-2021
 */
public enum RoleMessageKey implements MessageType {
    /**
     * Error during role creation.
     */
    ROL_ERROR_CREATED(1, "Error durante la creación del rol"),

    /**
     * Operation successful.
     */
    ROL_OK(200, "Ok"),

    /**
     * Role created successfully.
     */
    ROL_CREATED(201, "Rol creado"),

    /**
     * Role updated successfully.
     */
    ROL_UPDATE(202, "Rol actualizado"),

    /**
     * Role not found.
     */
    ROL_NOT_FOUND(404, "Rol no encontrado"),

    /**
     * Error during role creation.
     */
    ROL_CREATE_ERROR(404, "Error durante la creación del rol"),

    /**
     * Role name already exists.
     */
    ROL_PREVIOUS_EXIST(409, "Nombre de rol ya se encuentra ocupado, ingrese un nombre de rol diferente");

    private final int code;
    private final String status;

    /**
     * Constructor for RolMessageKey enum.
     *
     * @param code the code of the message
     * @param status the status description of the message
     */
    RoleMessageKey(int code, String status) {
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
