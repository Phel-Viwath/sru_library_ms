FROM gradle:7.6.0-jdk17 AS build
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME

# Copy only wrapper and config for caching
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts

RUN gradle build --no-daemon || true

# Now copy the rest of the source
COPY . .

RUN gradle bootJar --no-daemon

FROM openjdk:17-jdk-slim
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME

COPY --from=build $APP_HOME/build/libs/*.jar app.jar

EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
