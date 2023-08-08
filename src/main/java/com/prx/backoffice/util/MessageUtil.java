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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Clase utilitaria para gestion de mensajes
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 2019-10-14
 */
@Service
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

    public static final String OK = "200";
    public static final String CREATED = "201";
    public static final String ACCEPTED = "202";
    public static final String NOT_FOUND = "404";
    public static final String NOT_ACCEPTABLE = "408";

    public static final String LOG_PATH_SEPARATOR = "|";
    public static final String LOG_END_MSG = "Termina llamado al método ";
    public static final String LOG_START_MSG = "Inicia llamado al método ";
    public static final String MESSAGE_HEADER_STR = "Message-header";

    /**
     * Default Constructor
     */
    public MessageUtil() {
        // Default Constructor
    }

    public String getUserSolicitudNulaVacia() {
        return userSolicitudNulaVacia;
    }

    public void setUserSolicitudNulaVacia(String userSolicitudNulaVacia) {
        this.userSolicitudNulaVacia = userSolicitudNulaVacia;
    }

    public String getUserAliasNuloVacio() {
        return userAliasNuloVacio;
    }

    public void setUserAliasNuloVacio(String userAliasNuloVacio) {
        this.userAliasNuloVacio = userAliasNuloVacio;
    }

    public String getUserClaveNulaVacia() {
        return userClaveNulaVacia;
    }

    public void setUserClaveNulaVacia(String userClaveNulaVacia) {
        this.userClaveNulaVacia = userClaveNulaVacia;
    }

    public String getUserCreado() {
        return userCreado;
    }

    public void setUserCreado(String userCreado) {
        this.userCreado = userCreado;
    }

    public String getUserExiste() {
        return userExiste;
    }

    public void setUserExiste(String userExiste) {
        this.userExiste = userExiste;
    }

    public String getUserInvalido() {
        return userInvalido;
    }

    public void setUserInvalido(String userInvalido) {
        this.userInvalido = userInvalido;
    }

    public String getUserClaveNoPermitida() {
        return userClaveNoPermitida;
    }

    public void setUserClaveNoPermitida(String userClaveNoPermitida) {
        this.userClaveNoPermitida = userClaveNoPermitida;
    }

    public String getUserCorreoNoValido() {
        return userCorreoNoValido;
    }

    public void setUserCorreoNoValido(String userCorreoNoValido) {
        this.userCorreoNoValido = userCorreoNoValido;
    }

    public String getUserCorreoNoExiste() {
        return userCorreoNoExiste;
    }

    public void setUserCorreoNoExiste(String userCorreoNoExiste) {
        this.userCorreoNoExiste = userCorreoNoExiste;
    }

    public String getUserCorreoVacio() {
        return userCorreoVacio;
    }

    public void setUserCorreoVacio(String userCorreoVacio) {
        this.userCorreoVacio = userCorreoVacio;
    }

    public String getSinDatos() {
        return sinDatos;
    }

    public void setSinDatos(String sinDatos) {
        this.sinDatos = sinDatos;
    }

    public String getSolicitudExitosa() {
        return solicitudExitosa;
    }

    public void setSolicitudExitosa(String solicitudExitosa) {
        this.solicitudExitosa = solicitudExitosa;
    }

    /**
     *
     * @return Objeto de tipo {@link String}
     */
    @Override
    public String toString() {
        return "MessageUtil{" +
                "userSolicitudNulaVacia='" + userSolicitudNulaVacia + '\'' +
                ", userAliasNuloVacio='" + userAliasNuloVacio + '\'' +
                ", userClaveNulaVacia='" + userClaveNulaVacia + '\'' +
                ", userCreado='" + userCreado + '\'' +
                ", userExiste='" + userExiste + '\'' +
                ", userInvalido='" + userInvalido + '\'' +
                ", userClaveNoPermitida='" + userClaveNoPermitida + '\'' +
                ", userCorreoNoValido='" + userCorreoNoValido + '\'' +
                ", userCorreoNoExiste='" + userCorreoNoExiste + '\'' +
                ", userCorreoVacio='" + userCorreoVacio + '\'' +
                ", sinDatos='" + sinDatos + '\'' +
                ", solicitudExitosa='" + solicitudExitosa + '\'' +
                '}';
    }
}
