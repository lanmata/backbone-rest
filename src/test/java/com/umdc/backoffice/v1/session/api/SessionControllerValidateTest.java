package com.umdc.backoffice.v1.session.api;

import com.umdc.backoffice.v1.session.services.SessionService;
import com.umdc.backoffice.v1.session.to.SessionResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The session token travels in {@code session-token} ({@code Authorization} is a deprecated fallback for validate and
 * renew), which leaves {@code Authorization: Bearer} free for the managed client of the front end.
 */
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
    void renewReadsTheSessionTokenHeaderAndFallsBackToAuthorization() throws Exception {
        when(service.renewToken("good")).thenReturn(ResponseEntity.ok(new SessionResponse("new")));
        when(service.renewToken("legacy")).thenReturn(ResponseEntity.ok(new SessionResponse("new")));

        mvc.perform(get("/api/v1/session/renew").header("session-token", "good").header("Authorization", "Bearer m2m"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/session/renew").header("Authorization", "legacy")).andExpect(status().isOk());

        verify(service, never()).renewToken("Bearer m2m");
    }

    @Test
    void logoutRevokesTheSessionTokenAndTheRefreshTokenOfTheBody() throws Exception {
        when(service.logout("good", "refresh")).thenReturn(ResponseEntity.noContent().build());
        when(service.logout("good", null)).thenReturn(ResponseEntity.noContent().build());

        mvc.perform(post("/api/v1/session/logout").header("session-token", "good")
                        .contentType("application/json").content("{\"refreshToken\":\"refresh\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/session/logout").header("session-token", "good")).andExpect(status().isNoContent());

        verify(service).logout("good", "refresh");
        verify(service).logout("good", null);
    }

    @Test
    void logoutWithoutTokenIsABadRequestFromTheService() throws Exception {
        when(service.logout(null, null)).thenReturn(ResponseEntity.badRequest().build());

        mvc.perform(post("/api/v1/session/logout")).andExpect(status().isBadRequest());
    }

    @Test
    void noHeaderAtAllIsABadRequestFromTheService() throws Exception {
        when(service.validateToken(any())).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE));

        mvc.perform(get("/api/v1/session/validate")).andExpect(status().isBadRequest());
        verify(service).validateToken(null);
    }
}
