FROM openjdk:14-alpine
LABEL version="1.0"
LABEL description="Componente para configuración de servicios"
LABEL mantainer="Luis Mata luis.antonio.mata@gmail.com"

ARG JAR_FILE=target/prx-backbone-rest.jar
RUN addgroup -S appmng && adduser -S jvapps -G appmng
USER jvapps:appmng

EXPOSE 8083
WORKDIR /usr/local/runme
COPY ${JAR_FILE} "prx-backbone-rest.jar"
COPY src/main/resources/prx_backbone_rest.jks prx_backbone_rest.jks

ENTRYPOINT ["java", "-jar", "prx-backbone-rest.jar"]