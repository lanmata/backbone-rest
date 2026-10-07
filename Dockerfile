# syntax=docker/dockerfile:1
FROM amazoncorretto:25-alpine3.24-jdk
LABEL version="0.0.3"
LABEL description="PRX Backbone REST"
LABEL maintainer="Luis Mata luis.antonio.mata@gmail.com"

ARG TARGET_FILE=target/
ARG JAR_FILE=backbone-rest.jar
WORKDIR /usr/local/runme
# backbone.jks / umdc-truststore.jks are no longer COPYed separately here — pom.xml's
# <resources> entry for certs/backbone/ already bakes them onto the classpath at `mvn package`
# time, so they travel inside this jar (same fix as the certs-in-git problem: the files exist
# only on disk under certs/backbone/, gitignored, never in a Dockerfile COPY from a path that
# could be missing on a fresh checkout).
COPY ${TARGET_FILE}${JAR_FILE} ${JAR_FILE}
COPY docker-entrypoint.sh docker-entrypoint.sh

# certs/backbone/ is entirely gitignored (*.crt, see .gitignore's "Secrets / certs" section),
# so a plain `COPY certs/backbone/ certs/backbone/` from the build context copies nothing when
# building from a fresh git checkout — same trap mercury's own prx-internal-ca.crt hit (MER-5)
# and same fix as Dockerfile.native: passed in as a BuildKit secret instead of COPYed, so it
# never depends on the gitignored file being present in the build context.
# `docker build` needs: --secret id=prx_internal_ca,src=certs/backbone/prx-internal-ca.crt
RUN --mount=type=secret,id=prx_internal_ca,target=/run/secrets/prx-internal-ca.crt \
    mkdir -p certs/backbone && \
    cp /run/secrets/prx-internal-ca.crt certs/backbone/prx-internal-ca.crt

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
