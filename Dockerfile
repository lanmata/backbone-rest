FROM amazoncorretto:21-alpine3.20
LABEL version="0.0.3"
LABEL description="PRX Backbone REST"
LABEL mantainer="Luis Mata luis.antonio.mata@gmail.com"

ARG LATINHUB_DIR=/opt/images/LTHB
ARG IMG_DIR=/opt/images
ARG TARGET_FILE=target/
ARG KEYSTORE_FILE=keystore
ARG CNFS_CRT_NAME=prx-qa.config-server
ARG AUTH_CRT_NAME=prx-qa.manager
ARG SRMN_CRT_FILE_NAME=srmn
ARG SRMN_CRT_ALIAS=servicemonitor
ARG APP_CRT_NAME=prx-qa
ARG RESOURCE_PATH=/
WORKDIR /usr/local/runme
COPY ${TARGET_FILE}${JAR_FILE} ${JAR_FILE}
COPY ${RESOURCE_PATH}${KEYSTORE_FILE}.jks ${KEYSTORE_FILE}.jks
COPY ${RESOURCE_PATH}${CNFS_CRT_NAME}.crt ${CNFS_CRT_NAME}.crt
COPY ${RESOURCE_PATH}${AUTH_CRT_NAME}.crt ${AUTH_CRT_NAME}.crt
COPY ${RESOURCE_PATH}${SRMN_CRT_FILE_NAME}.crt ${SRMN_CRT_FILE_NAME}.crt
COPY ${RESOURCE_PATH}${APP_CRT_NAME}.crt ${APP_CRT_NAME}.crt

RUN addgroup -S appmng && adduser -S jvapps -G appmng
RUN chown -R jvapps:appmng .
RUN chmod -R 740 .
RUN keytool -import -alias ${APP_CRT_NAME} -keystore /usr/lib/jvm/default-jvm/jre/lib/security/cacerts -file ${APP_CRT_NAME}.crt -storepass changeit -noprompt && \
    keytool -import -alias ${AUTH_CRT_NAME}.tst -keystore /usr/lib/jvm/default-jvm/jre/lib/security/cacerts -file ${AUTH_CRT_NAME}.crt -storepass changeit -noprompt && \
    keytool -import -alias ${SRMN_CRT_FILE_NAME} -keystore /usr/lib/jvm/default-jvm/jre/lib/security/cacerts -file ${SRMN_CRT_FILE_NAME}.crt -storepass changeit -noprompt && \
    keytool -import -alias ${CNFS_CRT_NAME} -keystore /usr/lib/jvm/default-jvm/jre/lib/security/cacerts -file ${CNFS_CRT_NAME}.crt -storepass changeit -noprompt && \
    rm *.crt

# Crear el directorio y asignar permisos y usuario
RUN mkdir ${IMG_DIR} && \
        chown jvapps:appmng ${IMG_DIR} && \
        chmod 740 ${IMG_DIR} && \
    mkdir ${LATINHUB_DIR} && \
    chown jvapps:appmng ${LATINHUB_DIR} && \
    chmod 740 ${LATINHUB_DIR}

USER jvapps:appmng

EXPOSE 8082
CMD ["java", "-Dspring.cloud.vault.enabled=${VAULT_ENABLED}", "-Dapi.info.version=1.0.0", "-Dspring.application.name=backbone-rest", "-jar", "backbone-rest.jar" ]
