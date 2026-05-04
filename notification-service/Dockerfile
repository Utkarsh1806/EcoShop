FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar notification-service-0.1.0-SNAPSHOT.jar
EXPOSE 8113
ENTRYPOINT ["java","-jar","/app/notification-service-0.1.0-SNAPSHOT.jar"]
