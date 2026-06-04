---
name: Keytool
description: Tool for managing the JVM keystore, truststore, and SSL certificates in backbone-rest
type: terminal
command-prefix: keytool
used-by: [DevOps Engineer]
---

# Keytool Tool

## Purpose

Manage the JVM keystore (`keystore.jks`), truststore, and SSL certificates for
the **backbone-rest** microservice. Used during Docker image builds and local SSL setup.

> **Never modify `keystore.jks` directly** — always use `keytool` commands.  
> Passwords come from environment variables — never hardcode them.

## Certificate Files

| File | Alias | Usage |
|------|-------|-------|
| `src/main/resources/wildcard.tst.crt` | `wildcard-tst` | Wildcard cert for `*.tst` domain |
| `src/main/resources/keystore.jks` | (multiple) | Application keystore |

## Commands

### View Certificate Content

```bash
# View CRT file content
openssl x509 -in src/main/resources/wildcard.tst.crt -text -noout

# List all entries in keystore
keytool -list -keystore src/main/resources/keystore.jks \
  -storepass $SSL_KEYSTORE_PASSWORD

# Verbose list with full certificate details
keytool -list -v -keystore src/main/resources/keystore.jks \
  -storepass $SSL_KEYSTORE_PASSWORD
```

### Import Certificate into Keystore

```bash
# Import a CRT into keystore.jks
keytool -importcert \
  -alias wildcard-tst \
  -file src/main/resources/wildcard.tst.crt \
  -keystore src/main/resources/keystore.jks \
  -storepass $SSL_KEYSTORE_PASSWORD \
  -noprompt

# Import into JVM cacerts (used in Dockerfile at image build time)
keytool -importcert \
  -alias wildcard-tst \
  -file /certs/wildcard.tst.crt \
  -keystore $JAVA_HOME/lib/security/cacerts \
  -storepass changeit \
  -noprompt
```

### Export Certificate from Keystore

```bash
# Export a cert from keystore
keytool -exportcert \
  -alias backbone-rest \
  -keystore src/main/resources/keystore.jks \
  -storepass $SSL_KEYSTORE_PASSWORD \
  -file backbone-rest-exported.crt

# Convert to readable PEM format
openssl x509 -in backbone-rest-exported.crt -text -noout
```

### Delete Entry from Keystore

```bash
keytool -delete \
  -alias old-alias \
  -keystore src/main/resources/keystore.jks \
  -storepass $SSL_KEYSTORE_PASSWORD
```

### Check Certificate Expiry

```bash
# Check expiry of a CRT file
openssl x509 -in src/main/resources/wildcard.tst.crt -noout -dates

# Check expiry of a keystore entry
keytool -list -v \
  -keystore src/main/resources/keystore.jks \
  -storepass $SSL_KEYSTORE_PASSWORD \
  -alias backbone-rest | grep "Valid from"
```

## Dockerfile Pattern

```dockerfile
RUN keytool -importcert \
    -alias wildcard-tst \
    -file /certs/wildcard.tst.crt \
    -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit \
    -noprompt
```

## Notes

- `keystore.jks` stores: `backbone-rest` (app key), `mercury-api-client`, `prx-realm`, `prx-qa.manager.tst`
- Keystore type: `JKS` (set via `SSL_KEYSTORE_TYPE` env var)
- Do NOT commit keystore passwords to source control
- `wildcard.tst.crt` covers domains: `*.tst`, `prx-qa.tst`, `prx.test`, `latinub-qa.tst`, and others

