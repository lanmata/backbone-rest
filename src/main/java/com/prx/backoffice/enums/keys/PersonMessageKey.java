/*
 *
 *  * @(#)MessageStatusKey.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */
package com.prx.backoffice.enums.keys;

import com.prx.commons.enums.types.MessageType;

/**
 * MessageStatusKey.
 *
 * @author Luis Antonio Mata.
 * @version 1.0.1.20200904-01, 21-11-2020
 */
public enum PersonMessageKey implements MessageType {
	PERSON_ERROR_CREATED(1, "Error durante la creación de persona"),
	PERSON_OK(200, "Ok"),
	PERSON_CREATED(201, "persona creada"),
	PERSON_NOT_FOUND(404, "Persona no encontrado"),
	PERSON_CREATE_ERROR(404, "Error durante la creación de persona");

	private final int code;
	private final String status;

	PersonMessageKey(int code, String status) {
		this.code = code;
		this.status = status;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getStatus() {
		return status;
	}
}
