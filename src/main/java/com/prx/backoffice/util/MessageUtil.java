/*
 *
 *  * @(#)MessageUtil.java.
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

package com.prx.backoffice.util;

import com.prx.commons.util.JsonUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Clase utilitaria para gestion de mensajes
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 2019-10-14
 */
@Getter
@Service
@NoArgsConstructor
public class MessageUtil {
    @Value("${messages.user.solicitud-nula-vacia}")
    private String userSolicitudNulaVacia;
    @Value("${messages.user.login-vacio-nulo}")
    private String userAliasNuloVacio;
    @Value("${messages.user.clave-vacia-nula}")
    private String userClaveNulaVacia;
    @Value("${messages.user.usuario-creado}")
    private String userCreado;
    @Value("${messages.user.usuario-existe}")
    private String userExiste;
    @Value("${messages.user.usuario-invalido}")
    private String userInvalido;
    @Value("${messages.user.clave-no-permitida}")
    private String userClaveNoPermitida;
    @Value("${messages.user.correo-no-valido}")
    private String userCorreoNoValido;
    @Value("${messages.user.correo-existe}")
    private String userCorreoNoExiste;
    @Value("${messages.user.correo-vacio}")
    private String userCorreoVacio;
    @Value("${messages.user.sin-datos}")
    private String sinDatos;
    @Value("${messages.user.solicitud-exitosa}")
    private String solicitudExitosa;

    /**
     *
     * @return Objeto de tipo {@link String}
     */
    @Override
    public String toString(){
        return JsonUtil.toJson(this);
    }

}
