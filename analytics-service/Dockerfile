FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar analytics-service-0.1.0-SNAPSHOT.jar
EXPOSE 8116
ENTRYPOINT ["java","-jar","/app/analytics-service-0.1.0-SNAPSHOT.jar"]
