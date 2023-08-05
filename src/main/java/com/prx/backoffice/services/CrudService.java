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

package com.prx.backoffice.services;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CrudService <T> {

    /**
     * Crea un registro de persona
     *
     * @param t Objeto de tipo {@link T}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    ResponseEntity<T> create(T t);

    ResponseEntity<T> update(String id, T t);

    ResponseEntity<T> delete(String id, T t);

    /**
     * Realiza la busqueda de una persona
     *
     * @param id Objeto de tipo {@link T}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    ResponseEntity<T> find(String id);

    ResponseEntity<List<T>> list(String... id);

}
