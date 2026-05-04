FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar payment-service-0.1.0-SNAPSHOT.jar
EXPOSE 8109
ENTRYPOINT ["java","-jar","/app/payment-service-0.1.0-SNAPSHOT.jar"]
