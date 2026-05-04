FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar cart-service-0.1.0-SNAPSHOT.jar
EXPOSE 8106
ENTRYPOINT ["java","-jar","/app/cart-service-0.1.0-SNAPSHOT.jar"]
