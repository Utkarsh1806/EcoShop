FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar admin-service-0.1.0-SNAPSHOT.jar
EXPOSE 8115
ENTRYPOINT ["java","-jar","/app/admin-service-0.1.0-SNAPSHOT.jar"]
