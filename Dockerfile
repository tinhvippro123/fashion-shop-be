# Stage 1: Build executable JAR
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Gradle wrapper and build config first to leverage Docker layer cache
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build bootJar
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon \
    && rm -f build/libs/*-plain.jar \
    && mv build/libs/*.jar app.jar

# Stage 2: Lightweight Java 21 runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8081

# Memory tuned for 512MB free containers (Render/Koyeb) and dynamic PORT binding
ENTRYPOINT ["sh", "-c", "java -Xmx360m -Xms128m -XX:+UseSerialGC -Dserver.port=${PORT:-8081} -jar app.jar"]
