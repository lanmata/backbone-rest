---
name: Keytool
description: Tool for managing JKS keystores and SSL certificates in backbone-rest (read-only operations only)
type: terminal
command-prefix: keytool
used-by:
  - devops-engineer
  - security-auditor
---

# Keytool Tool

## Purpose

Inspect JKS keystore and certificate files used by **backbone-rest** for SSL/TLS and JWT signing.

> **WARNING**: Do NOT modify, regenerate, or re-commit keystores or certificates unless explicitly requested by the user. These are production security assets.

## Keystores in This Project

| File | Purpose |
|------|---------|
| `src/main/resources/backbone.jks` | App keystore (JWT signing / TLS) |
| `src/main/resources/umdc-truststore.jks` | Truststore for Supabase DB TLS |
| `src/main/resources/prod-ca-2021.crt` | Supabase CA certificate |

## Read-Only Commands

```bash
# List entries in a JKS keystore
keytool -list -keystore src/main/resources/backbone.jks -storepass <pass>

# View certificate details
keytool -list -v -keystore src/main/resources/backbone.jks -storepass <pass>

# Inspect a .crt file
openssl x509 -in src/main/resources/prod-ca-2021.crt -text -noout

# Check certificate expiry
openssl x509 -in src/main/resources/prod-ca-2021.crt -noout -dates
```

## Notes

- Keystore passwords are stored in Vault — never hardcoded.
- The deleted `src/main/resources/keystore.jks` (per git status) was replaced by `backbone.jks` — do not restore it.
- Certificate `prod-ca-2021.crt` is the Supabase production CA — required for Supabase DB TLS connections.
- Never commit keystore files that have been regenerated without security team approval.
