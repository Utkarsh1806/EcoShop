FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar infra-eureka-server-0.1.0-SNAPSHOT.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/infra-eureka-server-0.1.0-SNAPSHOT.jar"]
