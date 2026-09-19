#!/bin/sh
set -e

exec java \
    -Dspring.cloud.vault.enabled="${VAULT_ENABLED:-false}" \
    -Dapi.info.version=1.0.0 \
    -Dspring.application.name=backbone-rest \
    -DSSL_KEYSTORE_LOCATION="${SSL_KEYSTORE_LOCATION}" \
    -DSSL_KEYSTORE_TYPE="${SSL_KEYSTORE_TYPE}" \
    -DSSL_TRUSTSTORE_LOCATION="${SSL_TRUSTSTORE_LOCATION}" \
    -DSSL_TRUSTSTORE_TYPE="${SSL_TRUSTSTORE_TYPE}" \
    -jar backbone-rest.jar
