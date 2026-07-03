#!/usr/bin/env sh
# after-session.sh — prints a pre-commit checklist at the end of every session.
# backbone-rest | Java 21 / Spring Boot 4.0.6 / Maven

cat <<'CHECKLIST'

╔══════════════════════════════════════════════════════════════════════════════╗
║              backbone-rest — Pre-Commit Checklist                          ║
╚══════════════════════════════════════════════════════════════════════════════╝

 1. COMPILE (fast syntax check)
    mvn -DskipTests compile

 2. STATIC ANALYSIS — PMD (zero violations required)
    mvn pmd:check

 3. FULL TEST SUITE (PMD + JaCoCo + JUnit)
    mvn test

 4. COVERAGE REPORT (review if you added new service logic)
    open target/site/jacoco/index.html

 5. SECRETS SCAN — check no credentials in new/changed files
    grep -rEn "(password|secret|token)\s*=\s*['\"][^\$\{]" src/main/
    git ls-files | grep -E "\.(jks|p12|pem|crt|key)$"

 6. ENV-VAR DOCS — if you added a new ${ENV_VAR} in bootstrap.yml:
    a. Add the variable to .opencode/AGENTS.md §9 devops table
    b. Add a stub entry to src/main/resources/default.env (key only, no real value)
    c. Add to docs/env/ if that directory is maintained

 7. SQL MIGRATION — if you changed any JPA entity or database schema:
    a. Create src/main/resources/db/migration/V{n+1}__{description}.sql
    b. File must use general. schema prefix, UUID PKs, IF NOT EXISTS DDL
    c. Test against PostgreSQL (not only H2)
    Current migrations: V1__create_audit_event through V4__extend_audit_event_check

 8. OPENAPI YAML — if you changed any *Api.java contract:
    Update src/main/resources/META-INF/api.yaml to match

 9. PII CHECK — confirm no names, emails, phone numbers, or birth dates
    appear in log statements or exception messages

10. DOCKER (if building an image):
    mvn -DskipTests package
    docker build -t backbone-rest:local .

──────────────────────────────────────────────────────────────────────────────
CHECKLIST
