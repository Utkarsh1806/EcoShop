FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar checkout-service-0.1.0-SNAPSHOT.jar
EXPOSE 8107
ENTRYPOINT ["java","-jar","/app/checkout-service-0.1.0-SNAPSHOT.jar"]
