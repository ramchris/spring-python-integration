# Spring Boot app. Build context is the repo ROOT (needs settings.gradle + gradlew):
#   docker build -f spring-app/Dockerfile .

# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy Gradle wrapper + build scripts first so dependency downloads are cached.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY spring-app/build.gradle spring-app/
COPY python-model/build.gradle python-model/
RUN chmod +x gradlew && ./gradlew --no-daemon :spring-app:dependencies > /dev/null

COPY spring-app/src spring-app/src
RUN ./gradlew --no-daemon :spring-app:bootJar

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --create-home --uid 10001 appuser
COPY --from=build /workspace/spring-app/build/libs/app.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
