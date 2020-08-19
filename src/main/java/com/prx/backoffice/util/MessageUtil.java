package com.prx.backoffice.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.prx.commons.util.JsonUtil.toJson;

/**
 * Clase utilitaria para gestion de mensajes
 *
 * @author Luis Antonio Mata
 */
@Getter
@Component
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



    @Override
    public String toString(){
        return toJson(this);
    }

}
