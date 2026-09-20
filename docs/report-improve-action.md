# Improvement & Action Plan Report - Security and Risk Mitigation

## Purpose
This document outlines the strategic actions required to enhance security, reduce operational risk, and address technical debt within the **backbone-rest** project based on the initial system audit.

## 1. Critical Security Actions (Immediate Priority)

### 1.1. Identity & Session Management
*   **JTI Deny-list Cleanup Policy:** Implement a worker task or a scheduled cron job to purge expired JTI entries from the `JtiDenyListService`. Currently, memory/storage could leak if old records aren't periodically cleared.
    *   *Action:* Create a `@Scheduled` task in `com.umdc.backoffice.service.session` to prune items older than 7 days (the current grace period).
*   **Secret Rotation Policy:** Ensure that `APP_TOKEN_SECRET` is rotated via the Vault provider regularly. 
    *   *Action:* Document the rotation procedure in the internal deployment guide and ensure it's fetched from a secure environment variable in production.

### 1.2. Vulnerability Hardening
*   **Password Hashing Verification:** Complete an audit of all entry points accepting passwords to confirm `PasswordEncoder` is applied consistently across all registration/update workflows.
    *   *Action:* Cross-reference `UserUpdateService` and `ProfileUpdateService` with the `passwordEncoder.encode()` implementation.

## 2. Performance & Scalability Risk Mitigation

### 2.1. Query Optimization (N+1 Problems)
*   **Entity Graph Implementation:** Replace standard JPA fetches in high-traffic routes with `@EntityGraph`.
    *   *Target:* `UserRepository.loadUserAlias` and related roles/alliance lookups.
    *   *Action:* Update the Repository layer to fetch associated entities in a single join where possible to reduce DB roundtrips.

### 2.2. Caching Strategy
*   **Configuration Cache:** Identify frequently accessed, rarely changed configuration data (e.g., system status flags, department mappings).
    *   *Action:* Implement `@Cacheable` for these specific lookups to reduce repeated database hits and improve latency.

## 3. Structural & Architectural Debt Reduction

### 3.1. Complexity Management (God Class Refactoring)
*   **Refactor `SessionServiceImpl`:** The class is currently marked with `@SuppressWarnings("PMD.GodClass")`.
    *   *Action:* Decomposition of the logic into smaller, focused services (e.g., `SessionValidator`, `TokenGenerator`) to improve maintainability and testability.

### 3.2. Observability Standards
*   **Standardized Logging:** Ensure every service method utilizing a business transaction is decorated with `@LogDefault`.
    *   *Action:* Systematic pass through all `.ServiceImpl` files to ensure uniform log-start/log-end tracking.

## 4. Summary of Proposed Action Items (Development Roadmap)

| Task ID | Module | Description | Priority | Target Status |
| :--- | :--- | :--- | :--- | :--- |
| **SEC-01** | `session` | Implement JTI cleanup task | High | Critical Security |
| **SEC-02** | `security`| Verify rotate policy for APP_TOKEN_SECRET | High | Risk Management |
| **PERF-01** | `user` | Map `@EntityGraph` for user fetching | Medium | Optimization |
| **ARCH-01** | `session` | Decompose `SessionServiceImpl` | Medium | Technical Debt |
| **ARCH-02** | `general` | Apply `@LogDefault` globally | Low | Quality of Life |

---
*Generated on: 2024-06-28*
*Status: Preliminary Investigation Complete*
