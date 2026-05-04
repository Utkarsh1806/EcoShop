FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar search-service-0.1.0-SNAPSHOT.jar
EXPOSE 8103
ENTRYPOINT ["java","-jar","/app/search-service-0.1.0-SNAPSHOT.jar"]
