# Use the official Gradle image to build the project
FROM gradle:7.6.0-jdk17 AS build

# Set environment variables
ENV APP_HOME=/usr/app \
    GRADLE_USER_HOME=/usr/app/gradle

# Set the working directory using APP_HOME
WORKDIR $APP_HOME

# Copy the project files into the container
COPY . $APP_HOME

# Build the application using Gradle
RUN gradle bootJar

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
