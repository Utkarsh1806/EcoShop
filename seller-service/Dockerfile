FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar seller-service-0.1.0-SNAPSHOT.jar
EXPOSE 8114
ENTRYPOINT ["java","-jar","/app/seller-service-0.1.0-SNAPSHOT.jar"]
