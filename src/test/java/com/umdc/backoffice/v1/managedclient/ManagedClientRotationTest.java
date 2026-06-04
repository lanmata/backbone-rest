package com.umdc.backoffice.v1.managedclient;

import com.umdc.backoffice.v1.managedclient.api.controller.ManagedClientController;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientErrorResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientSecretRotateResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenResponse;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientRotationService;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientService;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientTokenService;
import jakarta.servlet.http.HttpServletRequest;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Integration-style controller tests for secret rotation on
/// {@link ManagedClientController} (AC-ROT-01).
@ExtendWith(MockitoExtension.class)
class ManagedClientRotationTest {

    // PMD AvoidDuplicateLiterals constants
    private static final String NEW_SECRET    = "new-plaintext-secret";
    private static final String OLD_SECRET    = "old-plaintext-secret";
    private static final String TEST_SCOPE    = "read:data";
    private static final String ERR_NOT_FOUND = "not_found";
    private static final long   GRACE_PERIOD  = 300L;

    @Mock
    private ManagedClientService managedClientService;

    @Mock
    private ManagedClientTokenService managedClientTokenService;

    @Mock
    private ManagedClientRotationService managedClientRotationService;

    @Mock
    private HttpServletRequest httpServletRequest;

    private ManagedClientController controller;

    @BeforeEach
    void setUp() {
        controller = new ManagedClientController(
                managedClientService, managedClientTokenService, managedClientRotationService);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    @DisplayName("POST /{id}/rotate-secret — returns 200 with new clientSecret (AC-ROT-01)")
    void rotateSecret_returns200WithNewSecret() {
        UUID clientId = UUID.randomUUID();
        ManagedClientSecretRotateResponse rotateResp = buildRotateResponse(clientId, NEW_SECRET);
        doReturn(ResponseEntity.ok(rotateResp))
                .when(managedClientRotationService).rotateSecret(clientId, "127.0.0.1");

        ResponseEntity<?> response = controller.rotateManagedClientSecret(clientId, httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ManagedClientSecretRotateResponse body = (ManagedClientSecretRotateResponse) response.getBody();
        assertNotNull(body);
        assertNotNull(body.getClientSecret());
        assertTrue(body.getGracePeriodSeconds() > 0);
        verify(managedClientRotationService).rotateSecret(clientId, "127.0.0.1");
    }

    @Test
    @DisplayName("POST /{id}/rotate-secret then POST /token — old secret valid during grace (AC-ROT-01)")
    void rotateSecret_oldSecretStillValidDuringGrace() {
        UUID clientId = UUID.randomUUID();
        doReturn(ResponseEntity.ok(buildRotateResponse(clientId, NEW_SECRET)))
                .when(managedClientRotationService).rotateSecret(clientId, "127.0.0.1");

        ManagedClientTokenRequest oldSecretReq = buildTokenRequest(clientId, OLD_SECRET);
        ManagedClientTokenResponse tokenResp = new ManagedClientTokenResponse();
        tokenResp.setAccessToken("signed-jwt");
        tokenResp.setTokenType("Bearer");
        tokenResp.setExpiresIn(3600L);
        doReturn(ResponseEntity.ok(tokenResp)).when(managedClientTokenService).issueToken(oldSecretReq);

        ResponseEntity<?> rotateResponse = controller.rotateManagedClientSecret(clientId, httpServletRequest);
        assertEquals(HttpStatus.OK, rotateResponse.getStatusCode());

        ResponseEntity<?> tokenResponse = controller.issueToken(oldSecretReq);
        assertEquals(HttpStatus.OK, tokenResponse.getStatusCode());
    }

    @Test
    @DisplayName("POST /unknown-id/rotate-secret — not found returns 404")
    void rotateSecret_notFound_returns404() {
        UUID clientId = UUID.randomUUID();
        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ManagedClientErrorResponse(ERR_NOT_FOUND, "Not found")))
                .when(managedClientRotationService).rotateSecret(clientId, "127.0.0.1");

        ResponseEntity<?> response = controller.rotateManagedClientSecret(clientId, httpServletRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ERR_NOT_FOUND, ((ManagedClientErrorResponse) response.getBody()).getError());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ManagedClientSecretRotateResponse buildRotateResponse(UUID clientId, String secret) {
        ManagedClientSecretRotateResponse resp = new ManagedClientSecretRotateResponse();
        resp.setClientId(clientId);
        resp.setClientSecret(secret);
        resp.setGracePeriodSeconds(GRACE_PERIOD);
        resp.setRotatedAt(LocalDateTime.now());
        return resp;
    }

    private ManagedClientTokenRequest buildTokenRequest(UUID clientId, String secret) {
        ManagedClientTokenRequest req = new ManagedClientTokenRequest();
        req.setClientId(clientId);
        req.setClientSecret(secret);
        req.setScopes(List.of(TEST_SCOPE));
        return req;
    }
}
