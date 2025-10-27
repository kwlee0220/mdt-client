FROM eclipse-temurin:21-jdk

RUN mkdir -p /mdt/mdt-client/config
COPY mdt-client-all.jar /mdt/mdt-client/mdt-client-all.jar

WORKDIR /mdt/mdt-client/config
VOLUME /mdt/mdt-client/config

# CMD ["java", "-Dlogging.config=config/logback.xml", "-jar", "mdt-client-all.jar", "--client_conf=config/mdt_client_config.yaml"]
#ENTRYPOINT ["java", "-cp", "/mdt-client/mdt-client-all.jar"]
