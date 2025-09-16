# Use the official Gradle image to build the project
FROM gradle:8.10-jdk17 AS build

# Set environment variables
ENV APP_HOME=/usr/app \
    GRADLE_USER_HOME=/usr/app/gradle

# Set the working directory using APP_HOME
WORKDIR $APP_HOME

# Copy only Gradle wrapper and project configuration files first
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts

# Cache dependencies by resolving them first
RUN gradle dependencies --no-daemon || true

# Copy the rest of the project files
COPY . .

# Build the application using Gradle
RUN gradle bootJar --no-daemon
RUN curl -I https://repo.maven.apache.org/maven2/

# Use a lightweight JDK image for runtime
FROM openjdk:17-jdk-slim

# Use the same APP_HOME for consistency
ENV APP_HOME=/usr/app

# Set the working directory for the runtime container
WORKDIR $APP_HOME

# Copy the built JAR file from the build stage
COPY --from=build $APP_HOME/build/libs/*.jar app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 8090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
