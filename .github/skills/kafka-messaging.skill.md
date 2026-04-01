---
name: Spring Security OAuth2
description: Skill for Keycloak JWT resource-server configuration and app-specific session JWT
applies-to:
  - Developer
  - Security Reviewer
---

# Spring Security OAuth2 Skill

## Scope

This skill covers the two distinct authentication mechanisms in the **backbone-rest** project.

## Mechanism 1 — OAuth2 Resource Server (Keycloak)

Configured in `SecurityConfig.java`. All `/api/v1/**` requests require a valid Keycloak JWT.

### Role Extraction
`JwtConverter` extracts roles from the `resource_access.<clientId>.roles` claim and maps them to `ROLE_<name>` `GrantedAuthority` entries.

### JwtConverterProperties
- `resourceId`: Keycloak client ID (e.g., `backbone-rest-client`)
- `principalClaimName`: JWT claim used as principal (default `sub`)

### NimbusJwtDecoder
Configured with `${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}` and uses `KeystoreUtil.getSslBundle()` for mTLS RestTemplate.

```java
@Bean
public JwtDecoder jwtDecoder(RestTemplate restTemplate) {
    return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri)
        .restOperations(restTemplate).build();
}
```

### Excluded Paths
Session endpoints are excluded via `bootstrap.yml`:
```yaml
app.api.excludes: /v1/sessions/token, /v1/sessions/validate
```

## Mechanism 2 — App-Specific Session JWT (JJWT)

Minted and verified in `SessionServiceImpl` using HMAC-SHA key from `APP_TOKEN_SECRET`.

### Session Token Header
`session-token` (constant: `SessionJwtService.SESSION_TOKEN_KEY`)

### Token Claims
```
jti, type="session-token", iat, alias, uid, roles_id, firstname, lastname
```

### Validation
```java
// In SessionController
boolean isValid = SESSION_TOKEN_KEY.equals(
    sessionService.getTokenClaims(sessionToken).get("type")
) && !sessionService.isTokenExpired(sessionToken);
```

### UID Extraction Utility
`JwtUtil.getUidFromToken(token)` — decodes JWT Base64 payload, extracts `uid` claim as `UUID`.

## Security Config Overview

```
/swagger-ui/**, /v3/api-docs/** → permitAll
GET /api/v1/**                  → hasAnyRole(clientRoleList)
anyRequest                      → authenticated
Session policy                  → STATELESS
CSRF                            → disabled
```

## Testing

Mock `SecurityConfig` bean dependencies in tests via `SecurityKeycloakTestConfig`:
```java
src/test/java/com/prx/backoffice/config/SecurityKeycloakTestConfig.java
```
