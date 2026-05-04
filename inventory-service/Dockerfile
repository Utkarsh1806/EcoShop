FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar inventory-service-0.1.0-SNAPSHOT.jar
EXPOSE 8105
ENTRYPOINT ["java","-jar","/app/inventory-service-0.1.0-SNAPSHOT.jar"]
