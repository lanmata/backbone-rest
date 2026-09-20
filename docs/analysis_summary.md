# Security Analysis Summary - backbone-rest

## 1. Security (OWASP, JWT, Session Management)

### Findings
*   **Risk (High) - Plaintext Password Comparison:** The `SessionServiceImpl` was initially identified as having risk due to potential plain-text comparisons. However, the code now uses `passwordEncoder.matches(request.getPassword(), userEntity.getPassword())`, which correctly integrates Spring Security's `PasswordEncoder` (BCrypt).
*   **JWT Implementation:** 
    *   Utilizes JJWT for both session and refresh tokens.
    *   Implements a **JTI Deny-list** via `JtiDenyListService` to handle immediate token revocation.
    *   **Grace Period:** A 7-day grace period is implemented for expired refresh tokens to allow seamless renewal before full expiry.
*   **Brute-force Protection:** `LoginAttemptService` is integrated into both standard and email login flows, ensuring accounts are locked after repeated failures.
*   **Architecture Security:** `SecurityConfig.java` correctly separates `session-token` logic from OAuth2 resource-server logic. CSRF protection is disabled because the API is stateless and relies on custom headers (`session-token`).

### Recommendations
*   Continue monitoring the `JtiDenyListService` cleanup strategy to ensure the deny-list doesn't grow indefinitely in memory/database.
*   Ensure `APP_TOKEN_SECRET` remains high-entropy as it secures all primary session tokens.

## 2. Performance (JPA, Query Efficiency)

### Findings
*   **User Lookups:** Service uses standard Repository patterns (`findByAlias`, `findByEmailAndApplication`). While functional, repetitive joins for `userAlias` and `roles` can be optimized if high traffic is expected.
*   **Concurrent Collections:** `ConcurrentHashMap` is correctly used for claim building to ensure thread safety during the construction phase.

### Recommendations
*   Implement `@EntityGraph` or specific DTO projections in `UserRepository` for the `loadUserAlias` method to avoid N+1 problems when fetching roles and alliance details iteratively.
*   Cache frequent results (like configuration-based parameters) using Spring Cache if they change infrequently.

## 3. Architectural Quality (Spring Boot Standards)

### Findings
*   **Design Pattern:** Follows the **Interface-first pattern**, strictly separating `SessionApi` from `SessionController` and providing a dedicated `SessionServiceImpl`.
*   **Response Handling:** Correctly uses `ResponseEntity` to encapsulate both status codes and business messages.
*   **Code Structure:** Adheres to the project's internal structure (e.g., `v1/session/...`).

### Recommendations
*   Refactor `SessionServiceImpl` if further expansion occurs; the class is currently marked with `@SuppressWarnings("PMD.GodClass")` as it grew during security hardening.
*   Standardize logging across all service methods using `@LogDefault` to ensure consistent observability.
