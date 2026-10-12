package com.umdc.backoffice.v1.session.api;

import com.umdc.backoffice.v1.session.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET /api/v1/session/validate: the token travels in {@code session-token}; {@code Authorization} is a deprecated fallback. */
class SessionControllerValidateTest {

    private SessionService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(SessionService.class);
        mvc = MockMvcBuilders.standaloneSetup(new SessionController(service)).build();
    }

    @Test
    void readsTheSessionTokenHeader() throws Exception {
        when(service.validateToken("good")).thenReturn(ResponseEntity.ok(Boolean.TRUE));

        mvc.perform(get("/api/v1/session/validate").header("session-token", "good"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
    }

    @Test
    void sessionTokenWinsOverAuthorization() throws Exception {
        when(service.validateToken("good")).thenReturn(ResponseEntity.ok(Boolean.TRUE));

        mvc.perform(get("/api/v1/session/validate").header("session-token", "good").header("Authorization", "other"))
                .andExpect(status().isOk());

        verify(service, never()).validateToken("other");
    }

    @Test
    void fallsBackToTheLegacyAuthorizationHeader() throws Exception {
        when(service.validateToken("legacy")).thenReturn(ResponseEntity.ok(Boolean.TRUE));

        mvc.perform(get("/api/v1/session/validate").header("Authorization", "legacy")).andExpect(status().isOk());
    }

    @Test
    void passesTheServiceVerdictThrough() throws Exception {
        when(service.validateToken("revoked")).thenReturn(ResponseEntity.ok(Boolean.FALSE));
        when(service.validateToken("expired")).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Boolean.FALSE));

        mvc.perform(get("/api/v1/session/validate").header("session-token", "revoked"))
                .andExpect(status().isOk()).andExpect(content().string("false"));
        mvc.perform(get("/api/v1/session/validate").header("session-token", "expired")).andExpect(status().isUnauthorized());
    }

    @Test
    void noHeaderAtAllIsABadRequestFromTheService() throws Exception {
        when(service.validateToken(any())).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE));

        mvc.perform(get("/api/v1/session/validate")).andExpect(status().isBadRequest());
        verify(service).validateToken(null);
    }
}
