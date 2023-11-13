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
package com.prx.backoffice.v1.roles.service;

import com.prx.backoffice.services.CrudService;
import com.prx.commons.pojo.Role;
import org.apache.commons.lang.NotImplementedException;
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
     * @param roleId {@link String}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<Role> find(String roleId) {
        throw new NotImplementedException();
    }

    /**
     * Realiza la creación de un rol.
     * @param role {@link Role}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    @Override
    default ResponseEntity<Role> create(Role role) {
        throw new NotImplementedException();
    }

    /**
     * Actualiza los campos de nombre y descripción, y activa o inactiva el estado del {@link Role}.
     * @param rolId {@link String}
     * @param role {@link Role}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    @Override
    default ResponseEntity<Role> update(String rolId, Role role) {
        throw new NotImplementedException();
    }

    /**
     * Lista un conjunto de roles vinculados a un id de usuario
     * @param userId {@link String}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<List<Role>> listByUser(String userId) {
        throw new NotImplementedException();
    }


    /**
     * Lista un conjunto de roles.
     * @param inactiveIncluded {@link Boolean}
     * @param roleIds {@link List<String>}
     * @return Objeto de tipo {@link ResponseEntity<List<Role>>}
     */
    default ResponseEntity<List<Role>> list(Boolean inactiveIncluded, List<String> roleIds) {
        throw new NotImplementedException();
    }

    /**
     * Lista un conjunto de roles.
     * @return Objeto de tipo {@link ResponseEntity<List<Role>>}
     */
    default ResponseEntity<List<Role>> list() {
        throw new NotImplementedException();
    }

    @Override
    default ResponseEntity<Role> delete(String id, Role role) {
        throw new NotImplementedException();
    }
}
