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
COPY docker-entrypoint.sh docker-entrypoint.sh

RUN addgroup -S appmng && adduser -S jvapps -G appmng \
&& chown -R jvapps:appmng . \
&& chmod -R 740 . \
&& chmod 750 docker-entrypoint.sh

USER jvapps:appmng

ENV SSL_KEYSTORE_LOCATION=backbone.jks \
    SSL_KEYSTORE_TYPE=JKS \
    SSL_TRUSTSTORE_LOCATION=umdc-truststore.jks \
    SSL_TRUSTSTORE_TYPE=JKS

EXPOSE 8084
CMD ["./docker-entrypoint.sh"]
