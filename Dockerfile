FROM amazoncorretto:17.0.8-alpine
LABEL version="1.1"
LABEL description="Componente para configuración de servicios"
LABEL mantainer="Luis Mata luis.antonio.mata@gmail.com"

#RUN addgroup -S appmng && adduser -S jvapps -G appmng
#USER jvapps:appmng
WORKDIR /usr/local/runme
ARG JAR_FILE=target/prx-backbone-rest.jar
COPY ${JAR_FILE} "prx-backbone-rest.jar"
COPY prx_backbone_rest.jks prx_backbone_rest.jks
COPY prx_srv_monitor.jks prx_srv_monitor.jks
COPY prx_backbone_rest_discovery.jks prx_backbone_rest_discovery.jks

EXPOSE 8084
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-Dspring.application.name=prx-backbone-rest", "-Dspring.cloud.config.label=Develop", "-Dspring.config.import=optional:configserver:http://prx.test/config-server", "-Dapi-info.version=0.0.2-2023" , "-jar", "prx-backbone-rest.jar" ]
