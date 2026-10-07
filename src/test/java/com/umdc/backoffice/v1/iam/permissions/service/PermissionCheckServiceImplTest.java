package com.umdc.backoffice.v1.iam.permissions.service;

import com.umdc.backoffice.constant.keys.AuthKey;
import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckRequest;
import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckResponse;
import com.umdc.backoffice.v1.session.services.SessionService;
import com.umdc.backoffice.v1.users.service.ApplicationRoleUserGraphLookupService;
import com.umdc.persistence.general.domains.ApplicationRoleUserEntity;
import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.RoleFeatureEntity;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.Set;
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
    private ApplicationRoleUserGraphLookupService applicationRoleUserGraphLookupService;

    @Mock
    private Claims claims;

    private PermissionCheckServiceImpl permissionCheckService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        permissionCheckService = new PermissionCheckServiceImpl(sessionService, applicationRoleUserGraphLookupService);
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

    // ── global fallback: no applicationId, or token carries no resolvable uid ──
    // These requests all pass a random applicationId but never stub the "uid" claim,
    // so extractUserId() returns null and the service falls back to the legacy
    // flattened-roles-claim check — exercising the exact same path as before.

    @Nested
    class GlobalRolesClaimFallback {

        @Test
        void check_validToken_noRolesClaim_returns200WithGrantedFalse() {
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), VALID_TOKEN);
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get("roles")).thenReturn(null);

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().granted());
            assertEquals(ROLE_ADMIN, response.getBody().permission());
        }

        @Test
        void check_validToken_permissionPresentInRoles_returnsGrantedTrue() {
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), VALID_TOKEN);
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get("roles")).thenReturn("[ROLE_ADMIN, ROLE_USER]");

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().granted());
            assertEquals(ROLE_ADMIN, response.getBody().permission());
        }

        @Test
        void check_validToken_permissionAbsentFromRoles_returnsGrantedFalse() {
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_SUPERUSER, UUID.randomUUID(), VALID_TOKEN);
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get("roles")).thenReturn("[ROLE_ADMIN, ROLE_USER]");

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().granted());
            assertEquals(ROLE_SUPERUSER, response.getBody().permission());
        }

        @Test
        void check_validToken_singleRoleMatchesExactly_returnsGrantedTrue() {
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_USER, UUID.randomUUID(), VALID_TOKEN);
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get("roles")).thenReturn("[ROLE_USER]");

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().granted());
        }

        @Test
        void check_validToken_partialRoleNameDoesNotMatch_returnsGrantedFalse() {
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), VALID_TOKEN);
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get("roles")).thenReturn("[ROLE_ADMIN_EXTRA, ROLE_USER]");

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().granted());
        }

        @Test
        void check_response_containsReasonString() {
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, UUID.randomUUID(), VALID_TOKEN);
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get("roles")).thenReturn("[ROLE_ADMIN]");

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertNotNull(response.getBody());
            assertNotNull(response.getBody().reason());
        }

        @Test
        void check_nullApplicationId_usesGlobalClaimEvenWithUidPresent() {
            // applicationId absent entirely — must always use the global path, regardless of uid.
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, null, VALID_TOKEN);
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get("roles")).thenReturn("[ROLE_ADMIN]");

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertTrue(response.getBody().granted());
        }
    }

    // ── application-scoped: applicationId present AND token carries a resolvable uid ──

    @Nested
    class ApplicationScopedCheck {

        private final UUID userId = UUID.randomUUID();
        private final UUID applicationId = UUID.randomUUID();

        private void stubValidTokenWithUid() {
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get(AuthKey.USER_ID.value)).thenReturn(userId.toString());
        }

        private RoleEntity activeRole(String name) {
            RoleEntity role = new RoleEntity();
            role.setId(UUID.randomUUID());
            role.setName(name);
            role.setDescription("test role");
            role.setActive(true);
            return role;
        }

        private ApplicationRoleUserEntity link(RoleEntity role, boolean active) {
            ApplicationRoleUserEntity entity = new ApplicationRoleUserEntity();
            entity.setRole(role);
            entity.setActive(active);
            return entity;
        }

        @Test
        void check_noAclLink_returnsGrantedFalseWithSpecificReason() {
            stubValidTokenWithUid();
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.empty());

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertFalse(response.getBody().granted());
            assertEquals("User has no active role in this application", response.getBody().reason());
        }

        @Test
        void check_inactiveAclLink_returnsGrantedFalse() {
            stubValidTokenWithUid();
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.of(link(activeRole(ROLE_ADMIN), false)));

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertFalse(response.getBody().granted());
        }

        @Test
        void check_activeLink_roleNameMatchesPermission_returnsGrantedTrue() {
            stubValidTokenWithUid();
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.of(link(activeRole(ROLE_ADMIN), true)));

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertTrue(response.getBody().granted());
            assertEquals("Permission granted", response.getBody().reason());
        }

        @Test
        void check_activeLink_roleNameMatchesCaseInsensitively_returnsGrantedTrue() {
            stubValidTokenWithUid();
            PermissionCheckRequest request = new PermissionCheckRequest("role_admin", applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.of(link(activeRole(ROLE_ADMIN), true)));

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertTrue(response.getBody().granted());
        }

        @Test
        void check_activeLink_roleInactive_returnsGrantedFalseEvenIfNameMatches() {
            stubValidTokenWithUid();
            RoleEntity inactiveRole = activeRole(ROLE_ADMIN);
            inactiveRole.setActive(false);
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.of(link(inactiveRole, true)));

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertFalse(response.getBody().granted());
        }

        @Test
        void check_activeLink_roleNameDoesNotMatch_returnsGrantedFalse() {
            stubValidTokenWithUid();
            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_SUPERUSER, applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.of(link(activeRole(ROLE_ADMIN), true)));

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertFalse(response.getBody().granted());
        }

        @Test
        void check_activeLink_grantedThroughActiveRoleFeature_returnsGrantedTrue() {
            stubValidTokenWithUid();
            RoleEntity role = activeRole(ROLE_ADMIN);

            FeatureEntity feature = new FeatureEntity();
            feature.setId(UUID.randomUUID());
            feature.setName("TEMPLATE_MANAGE");
            feature.setActive(true);

            RoleFeatureEntity roleFeature = new RoleFeatureEntity();
            roleFeature.setRole(role);
            roleFeature.setFeature(feature);
            roleFeature.setActive(true);
            role.setRoleFeatures(Set.of(roleFeature));

            PermissionCheckRequest request = new PermissionCheckRequest("TEMPLATE_MANAGE", applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.of(link(role, true)));

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertTrue(response.getBody().granted());
        }

        @Test
        void check_activeLink_inactiveRoleFeatureDoesNotGrant() {
            stubValidTokenWithUid();
            RoleEntity role = activeRole(ROLE_ADMIN);

            FeatureEntity feature = new FeatureEntity();
            feature.setId(UUID.randomUUID());
            feature.setName("TEMPLATE_MANAGE");
            feature.setActive(true);

            RoleFeatureEntity roleFeature = new RoleFeatureEntity();
            roleFeature.setRole(role);
            roleFeature.setFeature(feature);
            roleFeature.setActive(false);
            role.setRoleFeatures(Set.of(roleFeature));

            PermissionCheckRequest request = new PermissionCheckRequest("TEMPLATE_MANAGE", applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.of(link(role, true)));

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertFalse(response.getBody().granted());
        }

        @Test
        void check_activeLink_noRoleFeatures_doesNotThrow_returnsGrantedFalse() {
            stubValidTokenWithUid();
            RoleEntity role = activeRole(ROLE_ADMIN);
            role.setRoleFeatures(null);

            PermissionCheckRequest request = new PermissionCheckRequest("TEMPLATE_MANAGE", applicationId, VALID_TOKEN);
            when(applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId))
                    .thenReturn(Optional.of(link(role, true)));

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertFalse(response.getBody().granted());
        }

        @Test
        void check_uidClaimNotWellFormedUuid_fallsBackToGlobalClaim() {
            when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
            when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
            when(claims.get(AuthKey.USER_ID.value)).thenReturn("not-a-uuid");
            when(claims.get("roles")).thenReturn("[ROLE_ADMIN]");

            PermissionCheckRequest request = new PermissionCheckRequest(ROLE_ADMIN, applicationId, VALID_TOKEN);

            ResponseEntity<PermissionCheckResponse> response = permissionCheckService.check(request);

            assertTrue(response.getBody().granted());
        }
    }
}
