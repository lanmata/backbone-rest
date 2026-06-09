package com.umdc.backoffice.security.filter;

import com.umdc.backoffice.v1.session.services.SessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static com.umdc.backoffice.v1.session.services.SessionJwtService.AUTHORIZATION_HEADER;
import static com.umdc.backoffice.v1.session.services.SessionJwtService.BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SessionJwtAuthenticationFilterTest {

    private static final String VALID_TOKEN = "valid.token.here";
    private static final String INVALID_TOKEN = "invalid.token.here";
    private static final String TEST_SUBJECT = "testUser";

    @Mock
    private SessionService sessionService;

    @Mock
    private Claims claims;

    private SessionJwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new SessionJwtAuthenticationFilter(sessionService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String bearerToken(String token) {
        return BEARER_PREFIX + token;
    }

    // ── missing / blank header ─────────────────────────────────────────────────

    @Test
    void doFilterInternal_noHeader_passesRequestThroughUnchanged() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request, response, chain);

        // Assert — chain was invoked, status is default 200, no service call
        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
        verifyNoInteractions(sessionService);
    }

    @Test
    void doFilterInternal_blankHeader_passesRequestThroughUnchanged() throws ServletException, IOException {
        // Arrange — whitespace-only value is treated as absent
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
        verifyNoInteractions(sessionService);
    }

    // ── invalid token ─────────────────────────────────────────────────────────

    @Test
    void doFilterInternal_invalidToken_returns401AndClearsContext() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, bearerToken(INVALID_TOKEN));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(sessionService.isValid(INVALID_TOKEN)).thenReturn(false);

        // Act
        filter.doFilter(request, response, chain);

        // Assert — chain must NOT be invoked, 401 returned, context cleared
        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(sessionService).isValid(INVALID_TOKEN);
    }

    // ── valid token ───────────────────────────────────────────────────────────

    @Test
    void doFilterInternal_validTokenWithRoles_setsAuthenticationAndContinues()
            throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, bearerToken(VALID_TOKEN));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.get("roles")).thenReturn("[ROLE_ADMIN, ROLE_USER]");

        // Act
        filter.doFilter(request, response, chain);

        // Assert — chain invoked, authentication set with correct principal and authorities
        assertNotNull(chain.getRequest());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(TEST_SUBJECT, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(2, SecurityContextHolder.getContext().getAuthentication().getAuthorities().size());
    }

    @Test
    void doFilterInternal_validTokenWithNoRolesClaim_setsAuthenticationWithEmptyAuthorities()
            throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, bearerToken(VALID_TOKEN));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.get("roles")).thenReturn(null);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertNotNull(chain.getRequest());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }

    @Test
    void doFilterInternal_validTokenWithEmptyRolesClaim_setsAuthenticationWithEmptyAuthorities()
            throws ServletException, IOException {
        // Arrange — roles stored as "[]" (empty list representation)
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, bearerToken(VALID_TOKEN));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.get("roles")).thenReturn("[]");

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertNotNull(chain.getRequest());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }

    @Test
    void doFilterInternal_validTokenWithSingleRole_setsSingleAuthority()
            throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, bearerToken(VALID_TOKEN));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.get("roles")).thenReturn("[ROLE_USER]");

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertNotNull(chain.getRequest());
        assertEquals(1, SecurityContextHolder.getContext().getAuthentication().getAuthorities().size());
    }
}

