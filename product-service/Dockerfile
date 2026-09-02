FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home appuser \
    && chown -R appuser:appuser /app
COPY --from=build /workspace/target/*.jar app.jar
USER appuser
EXPOSE 9092
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=5 \
    CMD curl --fail http://localhost:9092/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
