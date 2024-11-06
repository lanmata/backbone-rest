# Backbone REST
REST service management

## Java arguments
`-Dspring.application.name=prx-backbone-rest -Dspring.profiles.active=qa -Dspring.config.import=optional:configserver:http://qa.prx.test/config-server/ -Dspring.cloud.config.label=Develop -Dapi.info.version=0.0.2-2023 --add-opens java.base/java.time=ALL-UNNAMED`

## Docker setup
* Build the image:\
`docker build -t lamata/backbone-rest .`

* Push the image in the Docker hub:\
`docker push lamata/backbone-rest -u [USER_REGISTRY] -p [TOKEN]`

* Pull the image from the Registry container
`docker pull lamata/backbone-rest -u [USER_REGISTRY] -p [TOKEN]`

<luis.antonio.mata@gmail.com>
