# ERP Platform: A Modular Enterprise Resource Planning Backend



**Academic Project Documentation and Technical Report**

| Item | Details |
| --- | --- |
| Project title | ERP Platform: A Modular Enterprise Resource Planning Backend |
| Project type | Software engineering / applied computing project |
| Implementation | Java 21, Spring Boot 3.3.0, Maven |
| Architecture | Package-organized modular monolith |
| Primary interface | Versioned REST API (`/api/v1`) |
| Data platforms | H2 for development and tests; PostgreSQL for deployment |
| Deployment | Docker container with Railway support |
| Application entry point | `com.erp.platform.ErpPlatformApplication` |

> **Academic note:** This document is a code-grounded technical report. Replace the identification fields required by the institution, and add measured experimental results where indicated before formal submission.

## Abstract

The ERP Platform is a multi-domain enterprise resource planning backend designed to consolidate commercial, financial, agricultural, manufacturing, inventory, human-resource, and administrative operations in a single service. The system uses a modular-monolith architecture: domain capabilities are separated into package-level modules while remaining in one deployable Spring Boot application. A versioned REST API provides client access, while Spring Data JPA, Hibernate, PostgreSQL, Flyway, tenant context, JWT security, and optional OIDC authentication support the application infrastructure. The implementation also provides integration points for AI assistants, email, SMS/WhatsApp, weather and satellite data, scheduled jobs, and operational health monitoring. This report describes the problem context, objectives, architecture, implementation methodology, deployment model, verification procedure, limitations, and future research directions.

**Keywords:** enterprise resource planning, modular monolith, Spring Boot, REST API, multi-tenancy, JWT, PostgreSQL, software architecture

## 1. Introduction

Organizations frequently operate sales, purchasing, finance, inventory, production, agriculture, and workforce processes in disconnected systems. Such fragmentation can cause duplicate data, inconsistent records, limited traceability, and costly integration work. The ERP Platform addresses this class of problem by providing a unified backend with domain-oriented modules and shared cross-cutting services.

The repository contains the backend implementation, database migrations, configuration profiles, automated tests, deployment artifacts, and architecture documentation. The service is designed as an extensible foundation rather than as a collection of independent microservices.

### 1.1 Problem Statement

The central problem is the absence of a common, tenant-aware backend capable of coordinating heterogeneous enterprise workflows while preserving domain separation, access control, data persistence, and deployment simplicity.

### 1.2 Objectives

1. Provide a unified REST API for core ERP business processes.
2. Organize business capabilities into maintainable domain modules.
3. Support tenant-aware persistence and authenticated access.
4. Provide reliable schema evolution through database migrations.
5. Enable local development with a low-friction H2 profile and deployment with PostgreSQL.
6. Expose integration points for external communication, AI, field data, and identity providers.
7. Supply a repeatable build, test, containerization, and health-check process.

### 1.3 Scope

The current implementation covers authentication, administration, master data, organization, CRM, sales, purchase, supplier management, pricing, promotions, dispatch, accounting, expenses, agriculture, production, processing, inventory, quality, HR, payroll, reports, workflow, scheduling, AI assistance, and field IoT. The frontend client is outside the scope of this repository.

## 2. System Requirements

### 2.1 Functional Requirements

- Authenticate users through local credentials and optionally configured OIDC providers.
- Enforce authenticated access, roles, privileges, and tenant context on protected operations.
- Manage customers, vendors, growers, organizers, employees, products, locations, and organizational data.
- Record and coordinate sales, purchasing, stock, production, processing, dispatch, accounting, payroll, and quality workflows.
- Provide reporting, scheduled processing, party-ledger support, and operational health information.
- Integrate with configurable AI, SMTP, Twilio, weather, satellite, and field-data providers.

### 2.2 Non-Functional Requirements

- Maintain a clear separation between controllers, services, repositories, and entities.
- Support schema persistence through PostgreSQL and isolated development through H2.
- Provide API documentation through OpenAPI and Swagger UI.
- Package the application as an executable JAR and a multi-stage Docker image.
- Keep deployment configuration externalized through environment variables.
- Provide automated tests using Spring Boot Test, Spring Security Test, and Testcontainers support.

## 3. Methodology

The implementation follows a domain-oriented software engineering approach. Requirements are represented by domain modules, HTTP contracts are implemented in controllers, business rules are coordinated by services, and persistence is handled by Spring Data repositories and JPA entities. Shared concerns such as authentication, tenant resolution, auditing, error handling, validation, scheduling, and integrations are implemented in common, configuration, and security packages.

The repository provides code-level evidence for the architecture. Runtime performance, availability, usability, and security assurance must be evaluated through controlled experiments and documented separately; they are not inferred solely from the source code.

## 4. System Architecture

### 4.1 Architectural Style

The application is a **modular monolith**. All modules run within one Spring Boot process and are packaged into one executable JAR, while package boundaries group related business responsibilities. This design simplifies deployment and cross-domain transactions and retains a possible future path toward service extraction.

### 4.2 Layered Request Flow

An authenticated request generally follows this sequence:

```text
Client
	-> SecurityFilterChain and JWT filter
	-> REST controller and request validation
	-> Domain service and tenant context
	-> Spring Data repository / JPA transaction
	-> H2 or PostgreSQL
	-> ApiResponse and serialized response
```

The login flow is exposed through `/api/v1/auth/login`. A successful login returns a JWT that is used for protected API calls. A representative customer operation is documented in [`flow-diagram.html`](flow-diagram.html).

### 4.3 Domain Modules

| Module group | Representative responsibilities |
| --- | --- |
| Core and administration | authentication, users, roles, privileges, locations, organization, workflow |
| Commercial | CRM, customers, vendors, sales, purchase, supplier, pricing, promotions, dispatch, expenses |
| Finance and people | accounting, journals, vouchers, ledgers, assets, HR, attendance, payroll, benefits, shareholders |
| Agriculture and operations | farmers, organizers, crops, plots, lots, production plans, contracts, payments, intake, logistics |
| Manufacturing and quality | production jobs, processing sequences, bills of materials, material issues, quality records |
| Inventory and insights | warehouses, stock operations, reports, scheduling, AI assistant, field IoT |

### 4.4 Security and Multi-Tenancy

Spring Security protects application routes using JWT-based stateless authentication. OIDC configuration supports optional Google and Microsoft providers. Domain operations use a shared tenant context so records can be associated with the active tenant. Production deployments must use a unique secret supplied through `JWT_SECRET`; the development property is not suitable for production.

## 5. Implementation Details

### 5.1 Technology Selection

Java 21 and Spring Boot 3.3.0 provide the runtime and application framework. Spring Web and Validation implement HTTP contracts, Spring Data JPA and Hibernate provide persistence, Spring Security provides authentication and authorization, Flyway manages PostgreSQL migrations, SpringDoc generates API documentation, and Caffeine supplies caching support.

### 5.2 Persistence and Data Management

The default development profile uses an in-memory H2 database with `create-drop` schema behavior. The PostgreSQL profile is available for local database integration. The Railway profile uses PostgreSQL, enables Flyway migrations from `src/main/resources/db/migration`, disables the H2 console, and reads credentials from environment variables. Startup seeders provide development and master data defaults.

### 5.3 External Integrations

The AI assistant supports Gemini, Claude, and OpenAI-compatible providers. Email uses SMTP configuration; SMS and WhatsApp can use Twilio; field IoT can simulate readings or use configured weather and satellite providers. These integrations are optional and should be enabled through environment configuration rather than hard-coded credentials.

## 6. Installation and Execution

### 6.1 Prerequisites

- JDK 21
- Maven 3.9 or newer
- PostgreSQL 14 or newer for the PostgreSQL profile
- Docker Desktop for container execution

```bash
java -version
mvn -version
```

### 6.2 Local Development

The default H2 profile can be started with:

```bash
mvn spring-boot:run
```

The service is available at `http://localhost:8080`. Swagger UI is available at `/swagger-ui.html`, OpenAPI at `/v3/api-docs`, health monitoring at `/actuator/health`, and the H2 console at `/h2-console`. H2 console credentials are JDBC URL `jdbc:h2:mem:erpdb`, user `sa`, and an empty password.

### 6.3 Build and Verification Commands

```bash
mvn test
mvn clean package
java -jar target/erp-platform-1.0.0.jar
```

The test configuration includes Spring Boot Test, Spring Security Test, and Testcontainers PostgreSQL dependencies. Actual test results should be recorded in the project submission using the execution date and environment.

### 6.4 PostgreSQL Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

The checked-in local profile targets database `erp_platform` on `localhost:5432` with user `erp_user`. Replace sample credentials before use outside local development. Avoid enabling competing schema-management strategies without reviewing the deployment migration policy.

## 7. Deployment

The Dockerfile uses Maven with Eclipse Temurin 21 for the build stage and a smaller Temurin 21 JRE image for runtime.

```bash
docker build -t erp-platform .
docker run --rm -p 8080:8080 erp-platform
```

The container activates the `railway` Spring profile. Railway uses `/actuator/health` as the health-check path and supplies the database connection through:

```text
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS, JWT_SECRET
```

Optional AI, email, SMS/WhatsApp, SSO, and field IoT variables are documented in `src/main/resources/application*.properties`. Secrets must be stored in the deployment environment, never in source control.

## 8. Configuration Reference

| Concern | Main variables |
| --- | --- |
| Security | `JWT_SECRET`, `JWT_EXPIRATION` |
| AI | `AI_PROVIDER`, `GEMINI_API_KEY`, `ANTHROPIC_API_KEY`, `OPENAI_COMPAT_API_KEY` |
| Email | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_ENABLED` |
| SMS/WhatsApp | `SMS_PROVIDER`, `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_SMS_FROM`, `TWILIO_WHATSAPP_FROM` |
| OIDC SSO | `SSO_ENABLED`, provider client IDs, issuer URLs, and JWKS URLs |
| Field IoT | `FIELDIOT_MODE`, provider URLs and keys, `FIELDIOT_SYNC_ENABLED`, `FIELDIOT_SYNC_CRON` |

## 9. Evaluation Protocol

For an academic evaluation, the following evidence should be collected and attached to the final report:

1. `mvn test` output, including test count, failures, and execution date.
2. Startup evidence from the H2 and PostgreSQL profiles.
3. OpenAPI screenshots or exported specifications for representative API workflows.
4. Authentication tests covering valid tokens, invalid tokens, and unauthenticated access.
5. Tenant-isolation tests demonstrating that one tenant cannot access another tenant's records.
6. Database migration evidence showing a clean migration on a disposable PostgreSQL instance.
7. Performance measurements such as response latency, throughput, and resource utilization under a documented workload.
8. Container health-check and redeployment evidence.

This section intentionally defines measurements rather than reporting unverified values.

## 10. Limitations

- The repository contains a backend service; a separate frontend or end-user interface is not included.
- H2 is ephemeral and should not be used to infer production persistence behavior.
- Background scheduled work runs inside the application instance rather than in a separately scalable worker.
- External integrations depend on provider availability, credentials, quotas, and network access.
- Source code alone cannot establish production performance, operational availability, or complete security assurance.
- The PostgreSQL profile and Railway profile use different schema-management settings; deployment owners must follow one controlled migration policy.

## 11. Future Work

Potential research and engineering extensions include service-boundary evaluation, asynchronous job execution, stronger automated tenant-isolation testing, performance benchmarking, audit-log analysis, observability improvements, formal threat modeling, and empirical comparison with a microservice deployment.

## 12. Conclusion

The ERP Platform demonstrates a practical modular-monolith approach to integrating heterogeneous enterprise processes in a single Java/Spring backend. Its package-level domain organization, tenant-aware security model, relational persistence, migration support, API documentation, and container deployment provide a coherent foundation for further research and production hardening. The next stage for an academic submission is to supplement this code-grounded description with reproducible experiments, measured results, and a discussion of the chosen architecture against clearly defined alternatives.

## Appendix A: Repository Structure

```text
src/main/java/com/erp/platform/
├── common/       shared DTOs, exceptions, auditing, and tenant context
├── config/       application configuration and startup seeders
├── security/     JWT, OIDC, authorization, and security filters
└── modules/      domain modules with controller/service/repository/entity layers

src/main/resources/
├── application*.properties
└── db/migration/ Flyway migrations
```

## Appendix B: Supporting Artifacts

- [`architecture.html`](architecture.html): code-grounded architecture diagram and notes
- [`flow-diagram.html`](flow-diagram.html): authentication and representative request-flow diagrams
- [`pom.xml`](pom.xml): build, dependency, and test configuration
- [`Dockerfile`](Dockerfile): multi-stage container build
- [`railway.toml`](railway.toml): Railway health-check and restart configuration
