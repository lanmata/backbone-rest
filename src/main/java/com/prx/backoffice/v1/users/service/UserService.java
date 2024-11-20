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

package com.prx.backoffice.v1.users.service;

import com.prx.backoffice.services.CrudService;
import com.prx.backoffice.v1.session.to.UserAliasTO;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.commons.pojo.User;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * UserService.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 27-10-2020
 */
public interface UserService extends CrudService<UserTO> {

    /**
     * Realiza la b&uacute;squeda de un usuario a trav&eacute;s del identificador de usuario. Retorna un objeto de tipo
     * {@link }
     *
     * @param userId {@link Long}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<UserTO> findUserById(String userId) {
        throw new NotImplementedException();
    }

    /**
     * Realiza la b&uacute;squeda de un usuario a trav&eacute;s del alias de usuario. Retorna un objeto de tipo
     * {@link }
     *
     * @param alias {@link String}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<UserTO> findUserByAlias(String alias) {
        throw new NotImplementedException();
    }

    /**
     * Realiza la b&uacute;squeda de un usuario a trav&eacute;s del alias de usuario. Retorna un objeto de tipo
     * {@link }
     *
     * @param alias {@link String}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<UserAliasTO> findUserAliasByAlias(String alias) {
        throw new NotImplementedException();
    }

    /**
     * Realiza la b&uacute;squeda del usuario requerido, valida los datos, si los datos son corrector,
     * retorna un token, en caso contrario, informa el motivo de rechazo.
     *
     * @param alias    Objeto de tipo String
     * @param password Objeto de tipo String
     * @return Objeto de tipo {@link ResponseEntity}<{@link UserTO}>
     */
    default ResponseEntity<UserTO> access(String alias, String password) {
        throw new NotImplementedException();
    }

    /**
     * Busca todos los usuarios existentes.
     *
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<List<UserTO>> findAll() {
        throw new NotImplementedException();
    }

    /**
     * Realiza la creacion de un usuario
     *
     * @param user Objeto de tipo {@link User}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<UserTO> create(UserTO user) {
        throw new NotImplementedException();
    }

    /**
     * Realiza la desvinculación de un rol a un usuario especifico.
     *
     * @param userId {@link Long}
     * @param rolId  {@link Integer}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<UserTO> unlink(String userId, String rolId) {
        throw new NotImplementedException();
    }

    /**
     * Realiza la vinculación de un rol a un usuario especifico.
     *
     * @param userId {@link Long}
     * @param rolId  {@link Integer}
     * @return Objeto de tipo {@link ResponseEntity}
     */
    default ResponseEntity<UserTO> link(String userId, String rolId) {
        throw new NotImplementedException();
    }

    /**
     * Validate the user alias is available.
     *
     * @param alias {@link String} Object type.
     * @return {@link String} Object type.
     */
    default ResponseEntity<String> validateAlias(String alias) {
        throw new NotImplementedException();
    }
}
