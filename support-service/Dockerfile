FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8119
ENTRYPOINT ["java","-jar","/app/app.jar"]
