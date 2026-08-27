# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache Maven dependencies before copying source (speeds up rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/erp-platform-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Dspring.profiles.active=railway", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
