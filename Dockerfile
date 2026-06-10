FROM amazoncorretto:21-alpine3.20
LABEL version="0.0.4"
LABEL description="PRX Backbone REST"
LABEL mantainer="Luis Mata luis.antonio.mata@gmail.com"

ARG TARGET_FILE=target/
ARG JAR_FILE=backbone-rest.jar
ARG KEYSTORE_FILE=backbone
ARG TRUSTSTORE_FILE=umdc-truststore
ARG RESOURCE_PATH=src/main/resources/
WORKDIR /usr/local/runme
COPY ${TARGET_FILE}${JAR_FILE} ${JAR_FILE}
COPY ${RESOURCE_PATH}${KEYSTORE_FILE}.jks ${KEYSTORE_FILE}.jks
COPY ${RESOURCE_PATH}${TRUSTSTORE_FILE}.jks ${TRUSTSTORE_FILE}.jks

RUN addgroup -S appmng && adduser -S jvapps -G appmng \
&& chown -R jvapps:appmng . \
&& chmod -R 740 .

USER jvapps:appmng

ENV SSL_KEYSTORE_LOCATION=backbone.jks \
    SSL_KEYSTORE_TYPE=JKS \
    SSL_TRUSTSTORE_LOCATION=umdc-truststore.jks \
    SSL_TRUSTSTORE_TYPE=JKS

EXPOSE 8084
CMD exec java \
    -Dspring.cloud.vault.enabled="${VAULT_ENABLED:-false}" \
    -Dapi.info.version=1.0.0 \
    -Dspring.application.name=backbone-rest \
    -DSSL_KEYSTORE_LOCATION="${SSL_KEYSTORE_LOCATION}" \
    -DSSL_KEYSTORE_TYPE="${SSL_KEYSTORE_TYPE}" \
    -DSSL_TRUSTSTORE_LOCATION="${SSL_TRUSTSTORE_LOCATION}" \
    -DSSL_TRUSTSTORE_TYPE="${SSL_TRUSTSTORE_TYPE}" \
    -jar backbone-rest.jar
