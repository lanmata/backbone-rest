package com.umdc.backoffice.v1.iam.permissions.service;

import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckRequest;
import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckResponse;
import com.umdc.backoffice.v1.session.services.SessionService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PermissionCheckServiceImplTest {

    private static final String VALID_TOKEN = "valid.session.token";
    private static final String INVALID_TOKEN = "invalid.session.token";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_SUPERUSER = "ROLE_SUPERUSER";

    @Mock
    private SessionService sessionService;

    @Mock
    private Claims claims;

    private PermissionCheckServiceImpl permissionCheckService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        permissionCheckService = new PermissionCheckServiceImpl(sessionService);
    }

    // ── invalid token ─────────────────────────────────────────────────────────

    @Test
    void check_invalidToken_returns401WithGrantedFalse() {
        // Arrange
        PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), INVALID_TOKEN);
        when(sessionService.isValid(INVALID_TOKEN)).thenReturn(false);

        // Act
        ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().granted());
    }

    // ── valid token, role scenarios ────────────────────────────────────────────

    @Test
    void check_validToken_noRolesClaim_returns200WithGrantedFalse() {
        // Arrange — token is valid but carries no roles claim
        PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.get("roles")).thenReturn(null);

        // Act
        ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().granted());
        assertEquals(ROLE_ADMIN, response.getBody().permission());
    }

    @Test
    void check_validToken_permissionPresentInRoles_returnsGrantedTrue() {
        // Arrange
        PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.get("roles")).thenReturn("[ROLE_ADMIN, ROLE_USER]");

        // Act
        ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().granted());
        assertEquals(ROLE_ADMIN, response.getBody().permission());
    }

    @Test
    void check_validToken_permissionAbsentFromRoles_returnsGrantedFalse() {
        // Arrange
        PermissionCheckRequest request = new PermissionCheckRequest(ROLE_SUPERUSER, UUID.randomUUID(), VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.get("roles")).thenReturn("[ROLE_ADMIN, ROLE_USER]");

        // Act
        ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().granted());
        assertEquals(ROLE_SUPERUSER, response.getBody().permission());
    }

    @Test
    void check_validToken_singleRoleMatchesExactly_returnsGrantedTrue() {
        // Arrange — single role in brackets
        PermissionCheckRequest request = new PermissionCheckRequest(ROLE_USER, UUID.randomUUID(), VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.get("roles")).thenReturn("[ROLE_USER]");

        // Act
        ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().granted());
    }

    @Test
    void check_validToken_partialRoleNameDoesNotMatch_returnsGrantedFalse() {
        // Arrange — "ROLE_ADMIN_EXTRA" should not match "ROLE_ADMIN"
        PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.get("roles")).thenReturn("[ROLE_ADMIN_EXTRA, ROLE_USER]");

        // Act
        ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().granted());
    }

    @Test
    void check_response_containsReasonString() {
        // Arrange — granted path should carry a non-null reason
        PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.get("roles")).thenReturn("[ROLE_ADMIN]");

        // Act
        ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().reason());
    }
}

