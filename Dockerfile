FROM amazoncorretto:17.0.8-alpine
LABEL version="1.1"
LABEL description="Componente para configuración de servicios"
LABEL mantainer="Luis Mata luis.antonio.mata@gmail.com"

ENV SPRING_BOOT_PROFILE_ACTIVE local
ENV SPRING_CLOUD_CONFIG_LABEL Develop
ENV CONFIG_SERVER_URL http://prx.test/config-server
ARG TARGET_FILE=target/
ARG CRT_QA_DOCKER_FILE=prx.env-qa-docker
ARG RESOURCE_PATH=src/main/resources/
WORKDIR /usr/local/runme
COPY ${TARGET_FILE}${JAR_FILE} ${JAR_FILE}
COPY ${RESOURCE_PATH}${CRT_QA_DOCKER_FILE}.crt ${CRT_QA_DOCKER_FILE}.crt
COPY ${RESOURCE_PATH}${CRT_QA_DOCKER_FILE}.p12 ${CRT_QA_DOCKER_FILE}.p12

EXPOSE 8084
RUN addgroup -S appmng && adduser -S jvapps -G appmng
RUN chown -R jvapps:appmng .
RUN chmod -R 740 .
RUN keytool -import -alias ${CRT_QA_DOCKER_FILE} -keystore /usr/lib/jvm/default-jvm/jre/lib/security/cacerts \
    -file ${CRT_QA_DOCKER_FILE}.crt -storepass changeit -noprompt
RUN rm *.crt

USER jvapps:appmng

EXPOSE 8082
ENTRYPOINT ["java", "-Dspring.application.name=backbone-rest", "-jar", "backbone-rest.jar" ]
