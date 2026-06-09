package com.umdc.backoffice.v1.managedclient;

import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.managedclient.api.controller.ManagedClientController;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientErrorResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenIntrospectResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenResponse;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientRotationService;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientService;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/// Integration-style controller tests for M2M token endpoints on
/// {@link ManagedClientController} (AC-TOK-01 – AC-TOK-04, AC-REV-01, AC-INT-01 – AC-INT-02).
@ExtendWith(MockitoExtension.class)
class ManagedClientTokenControllerTest {

    // PMD AvoidDuplicateLiterals constants
    private static final String TEST_SECRET  = "rawSecret";
    private static final String TEST_JWT     = "eyJ.eyJ.sig";
    private static final String TEST_SCOPE   = "read:data";
    private static final String ERR_INVALID  = "invalid_client";
    private static final String ERR_SCOPE    = "invalid_scope";
    private static final String TOKEN_BEARER = "Bearer";

    @Mock
    private ManagedClientService managedClientService;

    @Mock
    private ManagedClientTokenService managedClientTokenService;

    @Mock
    private ManagedClientRotationService managedClientRotationService;

    private ManagedClientController controller;

    @BeforeEach
    void setUp() {
        controller = new ManagedClientController(
                managedClientService, managedClientTokenService, managedClientRotationService);
    }

    // ── issueToken ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /token — valid credentials return 200 with access_token (AC-TOK-01)")
    void issueToken_valid_returns200WithJwt() {
        ManagedClientTokenRequest request = buildTokenRequest();
        ManagedClientTokenResponse tokenResp = buildTokenResponse();
        doReturn(ResponseEntity.ok(tokenResp)).when(managedClientTokenService).issueToken(request);

        ResponseEntity<?> response = controller.issueToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ManagedClientTokenResponse body = (ManagedClientTokenResponse) response.getBody();
        assertNotNull(body);
        assertNotNull(body.getAccessToken());
        assertEquals(TOKEN_BEARER, body.getTokenType());
        verify(managedClientTokenService).issueToken(request);
    }

    @Test
    @DisplayName("POST /token — wrong secret returns 401 (AC-TOK-02)")
    void issueToken_wrongSecret_returns401() {
        ManagedClientTokenRequest request = buildTokenRequest();
        doReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ManagedClientErrorResponse(ERR_INVALID, "Invalid credentials")))
                .when(managedClientTokenService).issueToken(request);

        ResponseEntity<?> response = controller.issueToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ERR_INVALID, ((ManagedClientErrorResponse) response.getBody()).getError());
    }

    @Test
    @DisplayName("POST /token — inactive client returns 401 (AC-TOK-03)")
    void issueToken_inactiveClient_returns401() {
        ManagedClientTokenRequest request = buildTokenRequest();
        doReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ManagedClientErrorResponse(ERR_INVALID, "Client inactive")))
                .when(managedClientTokenService).issueToken(request);

        ResponseEntity<?> response = controller.issueToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /token — scope overflow returns 400 (AC-TOK-04)")
    void issueToken_scopeOverflow_returns400() {
        ManagedClientTokenRequest request = buildTokenRequest();
        doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ManagedClientErrorResponse(ERR_SCOPE, "Scope overflow")))
                .when(managedClientTokenService).issueToken(request);

        ResponseEntity<?> response = controller.issueToken(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ERR_SCOPE, ((ManagedClientErrorResponse) response.getBody()).getError());
    }

    // ── revokeAllTokens then introspect ───────────────────────────────────────

    @Test
    @DisplayName("DELETE /{id}/tokens then introspect returns active=false (AC-REV-01)")
    void revokeAll_then_introspect_returnsActiveFalse() {
        UUID clientId = UUID.randomUUID();
        ManagedClientTokenIntrospectResponse inactiveResp = buildInactiveIntrospectResponse();
        doReturn(ResponseEntity.noContent().build()).when(managedClientTokenService).revokeAllTokens(clientId);
        doReturn(ResponseEntity.ok(inactiveResp)).when(managedClientTokenService).introspectToken(eq(TEST_JWT));

        ResponseEntity<?> revokeResp = controller.revokeAllTokens(clientId);
        assertEquals(HttpStatus.NO_CONTENT, revokeResp.getStatusCode());

        ResponseEntity<?> introspectResp = controller.introspectToken(new TokenIntrospectRequest(TEST_JWT));
        assertEquals(HttpStatus.OK, introspectResp.getStatusCode());
        assertFalse(((ManagedClientTokenIntrospectResponse) introspectResp.getBody()).isActive());
    }

    // ── introspectToken ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /introspect — valid token returns active=true (AC-INT-01)")
    void introspect_validToken_returnsActiveTrue() {
        ManagedClientTokenIntrospectResponse activeResp = new ManagedClientTokenIntrospectResponse();
        activeResp.setActive(true);
        activeResp.setClientId(UUID.randomUUID().toString());
        doReturn(ResponseEntity.ok(activeResp)).when(managedClientTokenService).introspectToken(any());

        ResponseEntity<?> response = controller.introspectToken(new TokenIntrospectRequest(TEST_JWT));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((ManagedClientTokenIntrospectResponse) response.getBody()).isActive());
    }

    @Test
    @DisplayName("POST /introspect — expired token returns active=false (AC-INT-02)")
    void introspect_expiredToken_returnsActiveFalse() {
        doReturn(ResponseEntity.ok(buildInactiveIntrospectResponse()))
                .when(managedClientTokenService).introspectToken(any());

        ResponseEntity<?> response = controller.introspectToken(new TokenIntrospectRequest(TEST_JWT));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(((ManagedClientTokenIntrospectResponse) response.getBody()).isActive());
    }

    @Test
    @DisplayName("POST /introspect — malformed token returns active=false, not 4xx (AC-INT-02)")
    void introspect_malformedToken_returnsActiveFalse() {
        doReturn(ResponseEntity.ok(buildInactiveIntrospectResponse()))
                .when(managedClientTokenService).introspectToken("garbage");

        ResponseEntity<?> response = controller.introspectToken(new TokenIntrospectRequest("garbage"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(((ManagedClientTokenIntrospectResponse) response.getBody()).isActive());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ManagedClientTokenRequest buildTokenRequest() {
        ManagedClientTokenRequest req = new ManagedClientTokenRequest();
        req.setClientId(UUID.randomUUID());
        req.setClientSecret(TEST_SECRET);
        req.setScopes(List.of(TEST_SCOPE));
        return req;
    }

    private ManagedClientTokenResponse buildTokenResponse() {
        ManagedClientTokenResponse resp = new ManagedClientTokenResponse();
        resp.setAccessToken(TEST_JWT);
        resp.setTokenType(TOKEN_BEARER);
        resp.setExpiresIn(3600L);
        resp.setScopes(List.of(TEST_SCOPE));
        resp.setIssuedAt(LocalDateTime.now());
        return resp;
    }

    private ManagedClientTokenIntrospectResponse buildInactiveIntrospectResponse() {
        ManagedClientTokenIntrospectResponse resp = new ManagedClientTokenIntrospectResponse();
        resp.setActive(false);
        return resp;
    }
}
