FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8109
ENTRYPOINT ["java","-jar","/app/app.jar"]
