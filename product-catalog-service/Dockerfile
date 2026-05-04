FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar product-catalog-service-0.1.0-SNAPSHOT.jar
EXPOSE 8102
ENTRYPOINT ["java","-jar","/app/product-catalog-service-0.1.0-SNAPSHOT.jar"]
