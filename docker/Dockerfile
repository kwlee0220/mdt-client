FROM eclipse-temurin:21-jdk

RUN mkdir -p /mdt-client
COPY mdt-client-all.jar /mdt-client/mdt-client-all.jar

WORKDIR /mdt-client
VOLUME /mdt-client

#ENTRYPOINT ["java", "-cp", "/mdt-client/mdt-client-all.jar"]
