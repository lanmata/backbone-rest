FROM amazoncorretto:21-alpine3.20
LABEL version="0.0.4"
LABEL description="PRX Backbone REST"
LABEL mantainer="Luis Mata luis.antonio.mata@gmail.com"

ARG LATINHUB_DIR=/opt/images/LTHB
ARG IMG_DIR=/opt/images
ARG TARGET_FILE=target/
ARG JAR_FILE=backbone-rest.jar
ARG KEYSTORE_FILE=backbone
ARG RESOURCE_PATH=src/main/resources/
WORKDIR /usr/local/runme
COPY ${TARGET_FILE}${JAR_FILE} ${JAR_FILE}
COPY ${RESOURCE_PATH}${KEYSTORE_FILE}.jks ${KEYSTORE_FILE}.jks
COPY ${RESOURCE_PATH}prx-truststore.jks prx-truststore.jks
COPY ${RESOURCE_PATH}prx-local-ca.crt prx-local-ca.crt

RUN addgroup -S appmng && adduser -S jvapps -G appmng
RUN chown -R jvapps:appmng .
RUN chmod -R 740 .
RUN keytool -importcert -alias prx-local-ca \
               -file prx-local-ca.crt \
               -keystore $JAVA_HOME/lib/security/cacerts \
               -storepass changeit -noprompt && \
    rm prx-local-ca.crt

# Crear el directorio y asignar permisos y usuario
RUN mkdir ${IMG_DIR} && \
        chown jvapps:appmng ${IMG_DIR} && \
        chmod 740 ${IMG_DIR} && \
    mkdir ${LATINHUB_DIR} && \
    chown jvapps:appmng ${LATINHUB_DIR} && \
    chmod 740 ${LATINHUB_DIR}

USER jvapps:appmng

EXPOSE 8084
CMD ["java", "-Dspring.cloud.vault.enabled=${VAULT_ENABLED}", "-Dapi.info.version=1.0.0", "-Dspring.application.name=backbone-rest", "-jar", "backbone-rest.jar" ]
