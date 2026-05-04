FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar fraud-service-0.1.0-SNAPSHOT.jar
EXPOSE 8117
ENTRYPOINT ["java","-jar","/app/fraud-service-0.1.0-SNAPSHOT.jar"]
