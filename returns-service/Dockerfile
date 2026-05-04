FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar returns-service-0.1.0-SNAPSHOT.jar
EXPOSE 8118
ENTRYPOINT ["java","-jar","/app/returns-service-0.1.0-SNAPSHOT.jar"]
