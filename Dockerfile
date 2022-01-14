FROM openjdk:14-alpine
LABEL version="1.0"
LABEL description="Componente para configuración de servicios"
LABEL mantainer="Luis Mata luis.antonio.mata@gmail.com"

#RUN addgroup -S appmng && adduser -S jvapps -G appmng
#USER jvapps:appmng
WORKDIR /usr/local/runme
ARG JAR_FILE=target/prx-backbone-rest.jar
COPY ${JAR_FILE} "prx-backbone-rest.jar"
COPY prx_backbone_rest.jks prx_backbone_rest.jks
COPY prx_srv_monitor.jks prx_srv_monitor.jks

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "prx-backbone-rest.jar"]