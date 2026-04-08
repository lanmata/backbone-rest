package com.prx.backoffice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageUtilTest {

    private MessageUtil messageUtil;

    @BeforeEach
    void setUp() {
        messageUtil = new MessageUtil();
    }

    @Test
    void testGetUserSolicitudNulaVacia() {
        messageUtil.setUserSolicitudNulaVacia("Test message");
        assertEquals("Test message", messageUtil.getUserSolicitudNulaVacia());
    }

    @Test
    void testGetUserAliasNuloVacio() {
        messageUtil.setUserAliasNuloVacio("Alias null or empty");
        assertEquals("Alias null or empty", messageUtil.getUserAliasNuloVacio());
    }

    @Test
    void testGetUserClaveNulaVacia() {
        messageUtil.setUserClaveNulaVacia("Password null or empty");
        assertEquals("Password null or empty", messageUtil.getUserClaveNulaVacia());
    }

    @Test
    void testGetUserCreado() {
        messageUtil.setUserCreado("User created");
        assertEquals("User created", messageUtil.getUserCreado());
    }

    @Test
    void testGetUserExiste() {
        messageUtil.setUserExiste("User exists");
        assertEquals("User exists", messageUtil.getUserExiste());
    }

    @Test
    void testGetUserInvalido() {
        messageUtil.setUserInvalido("Invalid user");
        assertEquals("Invalid user", messageUtil.getUserInvalido());
    }

    @Test
    void testGetUserClaveNoPermitida() {
        messageUtil.setUserClaveNoPermitida("Password not allowed");
        assertEquals("Password not allowed", messageUtil.getUserClaveNoPermitida());
    }

    @Test
    void testGetUserCorreoNoValido() {
        messageUtil.setUserCorreoNoValido("Invalid email");
        assertEquals("Invalid email", messageUtil.getUserCorreoNoValido());
    }

    @Test
    void testGetUserCorreoNoExiste() {
        messageUtil.setUserCorreoNoExiste("Email does not exist");
        assertEquals("Email does not exist", messageUtil.getUserCorreoNoExiste());
    }

    @Test
    void testGetUserCorreoVacio() {
        messageUtil.setUserCorreoVacio("Empty email");
        assertEquals("Empty email", messageUtil.getUserCorreoVacio());
    }

    @Test
    void testGetSinDatos() {
        messageUtil.setSinDatos("No data");
        assertEquals("No data", messageUtil.getSinDatos());
    }

    @Test
    void testGetSolicitudExitosa() {
        messageUtil.setSolicitudExitosa("Successful request");
        assertEquals("Successful request", messageUtil.getSolicitudExitosa());
    }

    @Test
    void testConstants() {
        assertEquals("|", MessageUtil.LOG_PATH_SEPARATOR);
        assertEquals("Termina llamado al método ", MessageUtil.LOG_END_MSG);
        assertEquals("Inicia llamado al método ", MessageUtil.LOG_START_MSG);
        assertEquals("Message-header", MessageUtil.MESSAGE_HEADER_STR);
    }

    @Test
    void testToString() {
        messageUtil.setUserSolicitudNulaVacia("solicitud");
        messageUtil.setUserAliasNuloVacio("alias");
        messageUtil.setUserClaveNulaVacia("clave");
        messageUtil.setUserCreado("creado");
        messageUtil.setUserExiste("existe");
        messageUtil.setUserInvalido("invalido");
        messageUtil.setUserClaveNoPermitida("no permitida");
        messageUtil.setUserCorreoNoValido("correo no valido");
        messageUtil.setUserCorreoNoExiste("correo no existe");
        messageUtil.setUserCorreoVacio("correo vacio");
        messageUtil.setSinDatos("sin datos");
        messageUtil.setSolicitudExitosa("exitosa");

        String result = messageUtil.toString();

        assertNotNull(result);
        assertTrue(result.contains("solicitud"));
        assertTrue(result.contains("alias"));
        assertTrue(result.contains("clave"));
        assertTrue(result.contains("creado"));
        assertTrue(result.contains("existe"));
        assertTrue(result.contains("invalido"));
        assertTrue(result.contains("no permitida"));
        assertTrue(result.contains("correo no valido"));
        assertTrue(result.contains("correo no existe"));
        assertTrue(result.contains("correo vacio"));
        assertTrue(result.contains("sin datos"));
        assertTrue(result.contains("exitosa"));
    }

    @Test
    void testDefaultConstructor() {
        MessageUtil newMessageUtil = new MessageUtil();
        assertNotNull(newMessageUtil);
    }

    @Test
    void testSettersWithNullValues() {
        messageUtil.setUserSolicitudNulaVacia(null);
        messageUtil.setUserAliasNuloVacio(null);
        messageUtil.setUserClaveNulaVacia(null);
        messageUtil.setUserCreado(null);
        messageUtil.setUserExiste(null);
        messageUtil.setUserInvalido(null);
        messageUtil.setUserClaveNoPermitida(null);
        messageUtil.setUserCorreoNoValido(null);
        messageUtil.setUserCorreoNoExiste(null);
        messageUtil.setUserCorreoVacio(null);
        messageUtil.setSinDatos(null);
        messageUtil.setSolicitudExitosa(null);

        assertNull(messageUtil.getUserSolicitudNulaVacia());
        assertNull(messageUtil.getUserAliasNuloVacio());
        assertNull(messageUtil.getUserClaveNulaVacia());
        assertNull(messageUtil.getUserCreado());
        assertNull(messageUtil.getUserExiste());
        assertNull(messageUtil.getUserInvalido());
        assertNull(messageUtil.getUserClaveNoPermitida());
        assertNull(messageUtil.getUserCorreoNoValido());
        assertNull(messageUtil.getUserCorreoNoExiste());
        assertNull(messageUtil.getUserCorreoVacio());
        assertNull(messageUtil.getSinDatos());
        assertNull(messageUtil.getSolicitudExitosa());
    }
}

