---
name: spring-microservices-kafka
description: Architectural design patterns for distributed Spring Boot microservices, Kafka event-driven integration, clean architecture boundaries, robust REST APIs with RFC 7807, and transactional safety patterns.
---

# Spring Microservices and Kafka Standards

This skill governs architectural and messaging conventions for building distributed Spring Boot microservices that integrate with Apache Kafka, ensuring message durability, strict schema validations, API standards, and clean boundaries.

## Architecture and Design Rules

### 1. Clean Architecture Boundary Enforcement
The codebase is structured around strict Clean Architecture / Hexagonal boundaries:
- **Domain Layer (`domain`):** Contains business entities, domain services, value objects, exceptions, and ports (interfaces for outgoing adapters). Agnostic of Spring Boot framework, Kafka, JPA/PostgreSQL, or Keycloak details.
- **Application Layer (`application`):** Orchestrates use cases (Application Services), maps request/response data through DTOs, and executes ports. Must not contain database transaction details or framework-specific controllers.
- **Infrastructure Layer (`infrastructure`):** Implements all incoming adapters (e.g., REST controllers, Kafka consumers) and outgoing adapters (e.g., JPA repositories, Keycloak client integrations, Kafka message producers).

*Constraint:* Infrastructure details must not leak into the Domain or Application layers. Always use interface abstractions (ports) for external calls. Map models between layers (e.g., using explicit Mappers) to prevent DB entities or REST DTOs from polluting the domain.

### 2. REST API Design & Error Representation
- **Conventions:** Follow RESTful principles. Group endpoints logically, use plural nouns (e.g., `/api/v1/remittances`), and return proper HTTP status codes:
  - `200 OK` / `201 Created` / `202 Accepted` for success.
  - `400 Bad Request` for validation failures.
  - `401 Unauthorized` / `403 Forbidden` for auth and role failures.
  - `404 Not Found` for missing resources.
  - `409 Conflict` for state conflicts (e.g., duplicate operations).
  - `422 Unprocessable Entity` for business rules violations.
- **Error Handling (RFC 7807):** Represent REST API errors uniformly using the RFC 7807 Problem Details specification. Use `@RestControllerAdvice` and `@ExceptionHandler` to catch exceptions globally and map them to standard problem details JSON objects (e.g., using `ProblemDetail` introduced in Spring Boot 3 / Java 17+).
- **Documentation:** Document all public endpoints using Springdoc OpenAPI 3. Include descriptive summaries, parameter details, and response schemas.

### 3. Kafka Messaging: Reliability & Resilience
For distributed communications and remittance updates:
- **Idempotent Producer:** Ensure Kafka producers are configured with `enable.idempotence=true` and `acks=all` to protect against message duplication or data loss.
- **Deterministic Partition Routing Key:** Every published event must specify a deterministic, non-null message key (e.g., the remittance ID, user ID, or transaction hash) to ensure messages associated with the same entity are routed to the same partition and processed in order.
- **Error Handling and Dead Letter Topics (DLT):**
  - Wrap consumers in a `DefaultErrorHandler` configured with exponential backoff (e.g., `BackOff` settings) and a max retry attempt limit (typically `3` to `5`).
  - Configure `DeadLetterPublishingRecoverer` to automatically forward poison-pill messages (unparseable or consistently failing payloads) to a `.DLT` topic rather than blocking partition execution.
- **Idempotent Consumers:** Because Kafka guarantees at least-once delivery, all message listeners must be idempotent. Implement a mechanism to check if an event ID or message key has already been processed (e.g., using a PostgreSQL transaction log or database unique constraints) before executing state changes.
- **Transactional Outbox Pattern:** When database updates and message publishing are part of the same business event, do not publish to Kafka directly inside the transaction. Write the event payload to an `outbox` table in PostgreSQL within the local database transaction, then publish to Kafka asynchronously using an outbox publisher process (or Debezium/CDC).
