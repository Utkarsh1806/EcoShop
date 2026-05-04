FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar shipping-service-0.1.0-SNAPSHOT.jar
EXPOSE 8110
ENTRYPOINT ["java","-jar","/app/shipping-service-0.1.0-SNAPSHOT.jar"]
