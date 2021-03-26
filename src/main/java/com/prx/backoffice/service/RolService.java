/*
 * @(#)RolService.java.
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
package com.prx.backoffice.service;

import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Rol;
import com.prx.commons.util.MessageActivityUtil;

import java.util.List;

/**
 * RolService.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
public interface RolService {

    /**
     * Realiza la búsqueda de un usuario a través del identificador de usuario. Retorna un objeto de tipo
     * {@link }
     *
     * @param rolId {@link Integer}
     * @return Objeto de tipo {@link MessageActivityUtil}
     */
    MessageActivity<Rol> find(Integer rolId);

    /**
     * Realiza la creación de un rol.
     * @param rol {@link Rol}
     * @return Objeto de tipo {@link MessageActivityUtil}
     */
    MessageActivity<Rol> create(Rol rol);

    /**
     * Vincula un rol con uno o mas features
     *
     * @param rolId {@link Integer}
     * @param featureIdList {@link List} de tipo {@link Long}
     * @return Objeto de tipo {@link MessageActivity}
     */
    MessageActivity<Rol> link(Integer rolId, List<Long> featureIdList);

    /**
     * Actualiza los campos de nombre y descripción, y activa o inactiva el estado del {@link Rol}.
     * @param rol {@link Rol}
     * @return Objeto de tipo {@link MessageActivity}
     */
    MessageActivity<Rol> update(Rol rol);

    /**
     * Lista un conjunto de roles en base a los id recibidos, el parametro booleano determina la obtención de roles
     * activos y/o inactivos. Si el parametro {@code roles} es nulo o vacio, obtiene todos los roles existentes en base
     * al parametro {@code includeActive}.
     *
     * @param includeActive {@link boolean}
     * @param roles {@link List} de tipo {@link Rol}
     * @return
     */
    MessageActivity<List<Rol>> list(boolean includeActive, List<Integer> roles);

    /**
     * Desvincula un rol con uno o mas features
     *
     * @param rolId {@link Integer}
     * @param featureIdList {@link List} de tipo {@link Long}
     * @return Objeto de tipo {@link MessageActivity}
     */
    MessageActivity<Rol> unlink(Integer rolId, List<Long> featureIdList);

    /**
     * Lista un conjunto de roles vinculados a un id de usuario
     * @param userId {@link long}
     * @return Objeto de tipo {@link MessageActivity}
     */
    MessageActivity<List<Rol>> list(long userId);

}
