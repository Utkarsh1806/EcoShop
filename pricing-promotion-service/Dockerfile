FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar pricing-promotion-service-0.1.0-SNAPSHOT.jar
EXPOSE 8111
ENTRYPOINT ["java","-jar","/app/pricing-promotion-service-0.1.0-SNAPSHOT.jar"]
