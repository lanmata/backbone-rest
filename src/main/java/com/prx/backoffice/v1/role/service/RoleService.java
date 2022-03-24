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
package com.prx.backoffice.v1.role.service;

import com.prx.backoffice.services.CrudService;
import com.prx.commons.pojo.Role;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * RolService.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
public interface RoleService extends CrudService<Role> {

    /**
     * Realiza la búsqueda de un usuario a través del identificador de usuario. Retorna un objeto de tipo
     * {@link }
     *
     * @param roleId {@link Long}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    ResponseEntity<Role> find(Long roleId);

    /**
     * Realiza la creación de un rol.
     * @param role {@link Role}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    ResponseEntity<Role> create(Role role);

    /**
     * Vincula un rol con uno o mas features
     *
     * @param roleId {@link Long}
     * @param featureIdList {@link List} de tipo {@link Long}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    ResponseEntity<Role> link(Long roleId, List<Long> featureIdList);

    /**
     * Actualiza los campos de nombre y descripción, y activa o inactiva el estado del {@link Role}.
     * @param rolId {@link Long}
     * @param role {@link Role}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    ResponseEntity<Role> update(Long rolId, Role role);

    /**
     * Lista un conjunto de roles en base a los id recibidos, el parametro booleano determina la obtención de roles
     * activos y/o inactivos. Si el parametro {@code roles} es nulo o vacio, obtiene todos los roles existentes en base
     * al parametro {@code includeActive}.
     *
     * @param includeActive {@link boolean}
     * @param roles {@link List} de tipo {@link Long}
     * @return
     */
    ResponseEntity<List<Role>> list(boolean includeActive, List<Long> roles);

    /**
     * Desvincula un rol con uno o mas features
     *
     * @param rolId {@link Long}
     * @param featureIdList {@link List} de tipo {@link Long}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    ResponseEntity<Role> unlink(Long rolId, List<Long> featureIdList);

    /**
     * Lista un conjunto de roles vinculados a un id de usuario
     * @param userId {@link long}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    ResponseEntity<List<Role>> list(long userId);

}
