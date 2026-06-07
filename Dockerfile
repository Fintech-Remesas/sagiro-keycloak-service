FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root group and user
RUN groupadd -r -g 1001 appgroup && \
    useradd -r -u 1001 -g appgroup appuser

COPY --from=build /workspace/target/iam-service-0.0.1-SNAPSHOT.jar app.jar

# Change ownership of the runtime directory
RUN chown -R appuser:appgroup /app

# Switch to the non-root user
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

