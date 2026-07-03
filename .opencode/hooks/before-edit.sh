#!/usr/bin/env sh
# before-edit.sh — warns when the file being edited is a build output,
# binary, secret/keystore, or vendor directory.
# $1 = FILE (path about to be edited)
# Always exits 0 — never blocks the edit.

FILE="${1:-}"

warn() {
    echo "[WARN] before-edit: $1 — $2" >&2
}

# Build output directories
case "${FILE}" in
    target/*|*/target/*)
        warn "${FILE}" "this is a Maven build output; edits here will be overwritten on next build"
        exit 0
        ;;
esac

# Binary / generated files
case "${FILE}" in
    *.class|*.jar|*.war|*.ear|*.pyc|*.o)
        warn "${FILE}" "binary or compiled artifact — edit the source file instead"
        exit 0
        ;;
    package-lock.json|yarn.lock|pnpm-lock.yaml)
        warn "${FILE}" "auto-generated lock file — let the package manager update it"
        exit 0
        ;;
esac

# Infrastructure secrets, keystores, and certificates
case "${FILE}" in
    bootstrap.yml|*/bootstrap.yml)
        warn "${FILE}" "central config file — changes affect startup credential resolution; review carefully"
        exit 0
        ;;
    application-prod.yml|*/application-prod.yml|application-production.yml|*/application-production.yml)
        warn "${FILE}" "production profile config — changes affect live environment"
        exit 0
        ;;
    *.jks|*.p12|*.pem|*.crt|*.cer|*.key|*.der)
        warn "${FILE}" "keystore or certificate — do NOT commit real key material; use placeholder or Vault"
        exit 0
        ;;
    prod-ca-2021.crt|*/prod-ca-2021.crt)
        warn "${FILE}" "production CA certificate — do not modify"
        exit 0
        ;;
esac

# Vendor / dependency directories
case "${FILE}" in
    node_modules/*|.gradle/*|.mvn/*)
        warn "${FILE}" "vendor/dependency directory — modify the build file instead"
        exit 0
        ;;
esac

exit 0
