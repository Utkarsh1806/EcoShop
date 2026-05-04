FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar user-service-0.1.0-SNAPSHOT.jar
EXPOSE 8101
ENTRYPOINT ["java","-jar","/app/user-service-0.1.0-SNAPSHOT.jar"]