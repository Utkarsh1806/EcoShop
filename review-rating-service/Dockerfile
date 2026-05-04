FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar review-rating-service-0.1.0-SNAPSHOT.jar
EXPOSE 8112
ENTRYPOINT ["java","-jar","/app/review-rating-service-0.1.0-SNAPSHOT.jar"]
