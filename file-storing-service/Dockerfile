FROM openjdk:17-jdk-slim
WORKDIR /app
ARG JAR_FILE=target/file-storing-service-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 