# --- Build stage: compile the Spring Boot fat jar ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache dependencies first
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# Build (skip tests — they need Docker/Testcontainers, run in CI instead)
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# --- Run stage: slim JRE ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75 -jar app.jar"]
