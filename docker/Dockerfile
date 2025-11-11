FROM eclipse-temurin:21-jdk

RUN mkdir -p /mdt/mdt-client/config

COPY mdt-client-all.jar /mdt/mdt-client/mdt-client-all.jar
COPY logback.xml /mdt/mdt-client/config/logback.xml
COPY mdt_client_config.yaml /mdt/mdt-client/config/mdt_client_config.yaml

WORKDIR /mdt/mdt-client/config

CMD ["java", "-cp", "../mdt-client-all.jar"]
