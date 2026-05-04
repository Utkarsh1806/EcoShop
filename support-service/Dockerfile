FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar support-service-0.1.0-SNAPSHOT.jar
EXPOSE 8119
ENTRYPOINT ["java","-jar","/app/support-service-0.1.0-SNAPSHOT.jar"]
