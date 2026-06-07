---
name: java-fintech-standards
description: Software engineering principles, best practices, and conventions for banking-grade Java development, transaction integrity, validation rules, strict audit logging, database connection tuning, and JVM resource optimization.
---

# Java Fintech & Banking Standards

This skill governs best practices for Java and Spring Boot development inside financial service components, with a strict emphasis on transaction integrity, precision, auditability, and database/JVM resource optimization.

## Guidelines and Conventions

### 1. Financial Precision and Calculations
Floating-point arithmetic is non-deterministic and must **never** be used for currency or account balances.
- **Data Type:** Always use `java.math.BigDecimal` for financial values, interest rates, and fees.
- **Scale and Rounding:** Define explicit scale (e.g., standard `2` decimal places, or `4` to `8` for exchange rates/crypto-fiat conversions) and specify a rounding mode. Use `RoundingMode.HALF_EVEN` (Banker's rounding) as the default for financial operations.
- **Comparison:** Do not use `.equals()` to compare `BigDecimal` values as it compares scale too (e.g., `2.0` vs `2.00` is false). Use `compareTo() == 0` instead.
- **Database Mapping:** Map fields in JPA/Hibernate with `@Column(precision = 18, scale = 4)` or equivalent depending on domain rules.

### 2. Transaction Management & Isolation
To prevent race conditions, double-spend vulnerabilities, and data inconsistency under high concurrency:
- **Declarative Transactions:** Use `@Transactional(rollbackFor = Exception.class)`. Ensure it is placed on public methods where state transitions occur.
- **Isolation Level:** Default to `Isolation.READ_COMMITTED` for standard operations. For ledger balances, interest postings, or ledger-altering operations, enforce pessimistic locking or utilize version-based optimistic locking (`@Version` on entities) to block concurrent writes.
- **Scope Limitation:** Keep transactional methods small. Avoid performing slow external HTTP/RPC requests (e.g., calling Keycloak or Web3 endpoints) inside a `@Transactional` block to prevent holding database connections open.

### 3. Banking-Grade Audit Logging
Every monetary transaction, authentication state change, or high-privilege configuration modification must produce a permanent audit record.
- **Security Logs:** Standardize log formats containing `traceId`, `spanId`, `userId`, `clientId`, and `clientIp` (retrieved from `MDC` headers set by filters).
- **Redaction of Sensitive PII/Secrets:** Under no circumstances should logs contain raw passwords, OAuth2 client secrets, CVVs, card PINs, or private keys. Utilize logback or log4j2 masking filters to scrub sensitive strings.
- **System Traceability:** Implement Spring's `MDC` (Mapped Diagnostic Context) to ensure that a trace ID from the API Gateway is propagated through all logging calls and async threads.

### 4. Data Validation and Defensive Programming
- **Validation Constraints:** Validate all incoming REST payloads at the controller boundary using JSR-380 validation annotations (`@NotNull`, `@Size`, `@DecimalMin`, `@Pattern`, `@Valid`).
- **Domain Validation:** Entities must self-validate prior to state transitions (e.g., ensuring an account balance does not drop below zero, or verifying that a remittance transfer is in a valid transition status). Do not depend solely on controller-level validations.
- **Immutability:** Make Value Objects immutable using Java `record` classes (e.g., `Amount`, `Currency`).

### 5. Performance and Database Resource Tuning
- **HikariCP Configuration:** Adjust the database connection pool sizing (`spring.datasource.hikari.maximum-pool-size`) based on runtime container constraints. Enable prep-statement caching.
- **Batch Processing:** When saving multiple ledger items or updates, configure Hibernate to batch inserts:
  `spring.jpa.properties.hibernate.jdbc.batch_size=50`
  `spring.jpa.properties.hibernate.order_inserts=true`
  `spring.jpa.properties.hibernate.order_updates=true`
- **Avoid N+1 Query Problems:** Use JPA entity graphs, `@Query("SELECT x FROM Entity x JOIN FETCH x.details")`, or native SQL queries instead of loading lazy collections sequentially.
- **JVM Garbage Collection:** For fintech applications targeting low-latency response times, configure the JVM to use the G1 Garbage Collector (`-XX:+UseG1GC`) or Z Garbage Collector (`-XX:+UseZGC`) depending on heap capacity.
