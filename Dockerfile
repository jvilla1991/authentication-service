# ── Build stage ────────────────────────────────────────────────────────────────
# Downloads dependencies first (cached as long as pom.xml is unchanged),
# then compiles and packages. Tests are skipped here — run them in CI before build.

FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn/
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY src src/
RUN ./mvnw package -B -q -DskipTests

# ── Runtime stage ───────────────────────────────────────────────────────────────
# Minimal JRE image — no JDK, no build tools, no source code.

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
