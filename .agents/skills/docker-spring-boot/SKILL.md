---
name: docker-spring-boot
description: Containerization guidelines for production-ready Spring Boot services, multi-stage Docker builds using Layered JARs, non-root user execution context, container-aware JVM memory settings, and graceful shutdown signal management.
---

# Spring Boot & Docker Containerization Standards

This skill governs containerization best practices for packaging, running, and managing Spring Boot microservices inside Docker containers in a secure, performant, and resilient manner.

## Guidelines and Conventions

### 1. Optimized Multi-Stage Docker Builds (Layered JARs)
To optimize build caching and minimize container image deployment sizes, do not copy or execute a monolithic fat-JAR. Instead, extract and package the JAR in layers:
- **Build Stage:** Compile the app and extract JAR layers using Spring Boot's layertools utility.
- **Run Stage:** Copy each layer individually. This ensures that when source files change, only the thin application layer is rebuilt, leaving cached dependency layers untouched.
- **Example Template:**
  ```dockerfile
  # Stage 1: Extraction
  FROM eclipse-temurin:21-jre-alpine AS builder
  WORKDIR /workspace
  ARG JAR_FILE=target/*.jar
  COPY ${JAR_FILE} app.jar
  RUN java -Djarmode=layertools -jar app.jar extract

  # Stage 2: Packaging
  FROM eclipse-temurin:21-jre-alpine
  WORKDIR /app
  
  # Configure non-root user
  RUN addgroup -S spring && adduser -S spring -G spring
  
  # Copy layers
  COPY --from=builder /workspace/dependencies/ ./
  COPY --from=builder /workspace/spring-boot-loader/ ./
  COPY --from=builder /workspace/snapshot-dependencies/ ./
  COPY --from=builder /workspace/application/ ./

  USER spring:spring
  EXPOSE 8080
  
  ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
  ```

### 2. Security & Non-Root Execution context
Running containers as root presents a significant security risk.
- **Create Dedicated User:** Always define a custom non-root system group and user (e.g., `spring` / `spring` with GID/UID 1000 or similar).
- **Context Switch:** Explicitly declare the `USER spring:spring` context before declaring ports, environment values, or the entrypoint.
- **Directory Permissions:** If the app needs write access to a folder (such as temp files or file-based DBs), create that directory and set owner permissions (`chown -R spring:spring /path`) before switching the USER context.

### 3. JVM Container Support and Memory Allocation
Without configuration, Java processes might not respect container resource limits (e.g., CPU, RAM limits defined in Kubernetes or Docker Compose), causing unexpected OOM (Out Of Memory) kills.
- **Container Awareness:** Modern JVMs (Java 17+) have `-XX:+UseContainerSupport` enabled by default. Do not disable this flag.
- **Memory Ratios:** Instead of hardcoded heap allocations (e.g. `-Xmx512m`), specify allocations as a percentage of the container's allocated RAM. For standard Spring Boot applications, use:
  `-XX:MaxRAMPercentage=75.0`
- **Garbage Collector Tuning:** For microservices with smaller resource limits (e.g., < 2GB RAM), configure the compiler and GC to prioritize startup times or footprint:
  `-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError`

### 4. Graceful Shutdown & Signal Propagation
To ensure the container can shut down cleanly without abruptly dropping active client connections or losing message states:
- **Spring Boot Config:** Enable graceful shutdown and specify the shutdown timeout threshold in `application.yml`:
  ```yaml
  server:
    shutdown: graceful
  spring:
    lifecycle:
      timeout-per-shutdown-phase: 20s
  ```
- **Signal Forwarding:** Ensure standard system shutdown signals (`SIGTERM`) reach the JVM process directly. Use the executive array format for the `ENTRYPOINT` (e.g., `ENTRYPOINT ["java", ...]`) rather than the shell string form (`ENTRYPOINT java -jar app.jar`), which starts the process inside `/bin/sh` and prevents signal forwarding.
