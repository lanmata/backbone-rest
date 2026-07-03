package com.umdc.backoffice.v1.session.services;

import com.umdc.backoffice.v1.session.to.SessionEmailRequest;
import com.umdc.backoffice.v1.session.to.SessionRequest;
import com.umdc.backoffice.v1.session.to.SessionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionServiceTest {

    private final SessionService sessionService = new SessionService() {
        @Override
        public String generateSessionToken(String username, Map<String, String> parameters) {
            return "";
        }

        @Override
        public String getUsernameFromToken(String token) {
            return "";
        }
    };

    @Test
    @DisplayName("Load session with valid alias and password")
    void loadSessionWithValidAliasAndPassword() {
        SessionRequest sessionRequest = new SessionRequest("validAlias", "validPassword", UUID.randomUUID());

        ResponseEntity<SessionResponse> response = sessionService.loadSession(sessionRequest);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    @DisplayName("Load session with invalid alias and password")
    void loadSessionWithInvalidAliasAndPassword() {
        SessionRequest sessionRequest = new SessionRequest("invalidAlias", "invalidPassword", UUID.randomUUID());

        ResponseEntity<SessionResponse> response = sessionService.loadSession(sessionRequest);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    @DisplayName("Load session with invalid email and password")
    void loadSessionWithInvalidEmailAndPassword() {
        SessionEmailRequest sessionEmailRequest = new SessionEmailRequest("validEmail@example.com", "validPassword", UUID.randomUUID());

        ResponseEntity<SessionResponse> response = sessionService.loadSession(sessionEmailRequest);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    @DisplayName("Load session with missing alias")
    void loadSessionWithMissingAlias() {
        SessionRequest sessionRequest = new SessionRequest(null, "validPassword", UUID.randomUUID());

        ResponseEntity<SessionResponse> response = sessionService.loadSession(sessionRequest);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    @DisplayName("Load session with missing password")
    void loadSessionWithMissingPassword() {
        SessionRequest sessionRequest = new SessionRequest("validAlias", null, UUID.randomUUID());

        ResponseEntity<SessionResponse> response = sessionService.loadSession(sessionRequest);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }
}
