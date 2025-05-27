FROM openjdk:17-jdk-slim
WORKDIR /app
ARG JAR_FILE=target/file-analysis-service-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"] 