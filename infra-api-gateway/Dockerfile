FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar infra-api-gateway-0.1.0-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/infra-api-gateway-0.1.0-SNAPSHOT.jar"]
