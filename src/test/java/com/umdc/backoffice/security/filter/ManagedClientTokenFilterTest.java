package com.umdc.backoffice.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientTokenService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/// Unit tests for {@link ManagedClientTokenFilter} verifying M2M token interception,
/// valid/invalid token handling, and pass-through for non-M2M requests.
@ExtendWith(MockitoExtension.class)
class ManagedClientTokenFilterTest {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SCOPE_READ    = "read:data";

    @Mock
    private ManagedClientTokenService tokenService;

    private ManagedClientTokenFilter filter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new ManagedClientTokenFilter(tokenService, objectMapper);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── valid M2M token ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid M2M token — sets SecurityContext and passes filter chain")
    void m2mToken_valid_setsSecurityContext() throws Exception {
        UUID clientId = UUID.randomUUID();
        String rawToken = buildM2mTokenString(clientId, List.of(SCOPE_READ));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", BEARER_PREFIX + rawToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.isTokenActive(rawToken)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest(), "Filter chain must be invoked for valid M2M token");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ── revoked / invalid M2M token ───────────────────────────────────────────

    @Test
    @DisplayName("Revoked M2M token — returns 401 and does not invoke filter chain")
    void m2mToken_revoked_returns401() throws Exception {
        UUID clientId = UUID.randomUUID();
        String rawToken = buildM2mTokenString(clientId, List.of(SCOPE_READ));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", BEARER_PREFIX + rawToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.isTokenActive(rawToken)).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertNull(chain.getRequest(), "Chain must NOT be invoked for revoked token");
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("invalid_token"));
    }

    @Test
    @DisplayName("Expired M2M token — returns 401")
    void m2mToken_expired_returns401() throws Exception {
        UUID clientId = UUID.randomUUID();
        String rawToken = buildM2mTokenString(clientId, List.of(SCOPE_READ));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", BEARER_PREFIX + rawToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.isTokenActive(rawToken)).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    // ── non-M2M / no auth header ──────────────────────────────────────────────

    @Test
    @DisplayName("Non-M2M Bearer token — passes to next filter without touching SecurityContext")
    void nonM2mToken_passesToNextFilter() throws Exception {
        String rawToken = buildNonM2mTokenString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", BEARER_PREFIX + rawToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest(), "Filter chain must be invoked for non-M2M token");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("No Authorization header — passes to next filter")
    void noAuthHeader_passesToNextFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest(), "Filter chain must be invoked when no auth header present");
    }

    @Test
    @DisplayName("session-token header only (no Bearer) — passes to next filter")
    void sessionTokenRequest_notIntercepted() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("session-token", "some-session-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest(), "Filter chain must be invoked when no Bearer header present");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String buildM2mTokenString(UUID clientId, List<String> scopes) throws Exception {
        Map<String, Object> payload = Map.of(
                "type", "M2M",
                "sub", clientId.toString(),
                "scopes", scopes);
        String payloadJson = objectMapper.writeValueAsString(payload);
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return "eyJhbGciOiJSUzI1NiJ9." + encoded + ".fakesignature";
    }

    private String buildNonM2mTokenString() throws Exception {
        Map<String, Object> payload = Map.of("sub", UUID.randomUUID().toString());
        String payloadJson = objectMapper.writeValueAsString(payload);
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return "eyJhbGciOiJSUzI1NiJ9." + encoded + ".fakesignature";
    }
}
