FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar infra-config-server-0.1.0-SNAPSHOT.jar
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app/infra-config-server-0.1.0-SNAPSHOT.jar"]
