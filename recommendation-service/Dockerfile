FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar recommended-service-0.1.0-SNAPSHOT.jar
EXPOSE 8104
ENTRYPOINT ["java","-jar","/app/recommended-service-0.1.0-SNAPSHOT.jar"]
