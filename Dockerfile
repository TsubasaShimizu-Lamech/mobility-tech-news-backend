# ── ビルドステージ ──
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts .
RUN ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew bootJar --no-daemon

# ── 実行ステージ ──
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*[^plain].jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
