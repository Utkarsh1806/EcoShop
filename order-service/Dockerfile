FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar order-service-0.1.0-SNAPSHOT.jar
EXPOSE 8108
ENTRYPOINT ["java","-jar","/app/order-service-0.1.0-SNAPSHOT.jar"]
