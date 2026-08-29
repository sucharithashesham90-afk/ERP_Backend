# ERP Platform: Enterprise Resource Planning Backend

A comprehensive, modular Java-based Enterprise Resource Planning (ERP) system designed to consolidate commercial, financial, agricultural, manufacturing, inventory, human-resource, and administrative operations in a single, scalable backend service.

## 📋 Overview

The ERP Platform is a production-ready, multi-domain backend application built with modern Java technologies. It provides a unified REST API for managing core business processes across multiple operational domains while maintaining data integrity, security, and multi-tenant isolation.

**Key Information:**
- **Technology Stack:** Java 21, Spring Boot 3.3.0, Maven
- **Architecture:** Modular Monolith (20+ domain modules)
- **API:** Versioned REST API (`/api/v1`) with OpenAPI/Swagger documentation
- **Databases:** H2 (development), PostgreSQL (production)
- **Deployment:** Docker containers with Railway support
- **Entry Point:** `com.erp.platform.ErpPlatformApplication`

## ✨ Features

### Core Business Domains
- **Authentication & Access Control** - JWT-based auth with OIDC SSO support (Google, Microsoft)
- **Customer & Vendor Management** - CRM with sales and purchase workflows
- **Inventory Management** - Warehouse operations, stock tracking, and dispatch
- **Accounting & Finance** - Ledgers, journals, vouchers, and financial reporting
- **Human Resources** - Employee management, attendance, payroll, benefits
- **Agriculture Operations** - Farmer management, crop data, production contracts
- **Manufacturing** - Production jobs, processing sequences, quality control
- **Procurement** - Supplier management, purchase orders, pricing strategies
- **Reporting & Analytics** - Configurable business intelligence and dashboards
- **AI Integration** - Intelligent assistant backed by Gemini, Claude, or OpenAI
- **Scheduling & Automation** - Background jobs, notifications, workflow orchestration

### Technical Capabilities
- **Multi-Tenancy** - Complete tenant isolation with shared infrastructure
- **Database Migrations** - Flyway-based schema versioning and evolution
- **Caching** - Caffeine-based high-performance caching layer
- **Email & SMS** - SMTP and Twilio integration for communications
- **Health Monitoring** - Spring Boot Actuator with detailed health checks
- **API Documentation** - Auto-generated OpenAPI specs and interactive Swagger UI
- **Testing** - Comprehensive test suite with Testcontainers support
- **Field IoT** - Weather and satellite data integration

## 🚀 Quick Start

### Prerequisites

- **Java Development Kit (JDK):** 21 or later
- **Apache Maven:** 3.9.0 or newer
- **PostgreSQL:** 14+ (for production profile)
- **Docker Desktop:** (for containerized deployment)

Verify your installation:
```bash
java -version
mvn -version
```

### Installation

1. **Clone the repository:**
```bash
git clone <repository-url>
cd ERP_Backend
```

2. **Build the project:**
```bash
mvn clean package
```

3. **Run with H2 (Development):**
```bash
mvn spring-boot:run
```
The application starts on `http://localhost:8080`

### Access Points

Once running, access these endpoints:

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI Spec** | http://localhost:8080/v3/api-docs |
| **Health Check** | http://localhost:8080/actuator/health |
| **H2 Console** (dev only) | http://localhost:8080/h2-console |

## 2. System Requirements

## 2. Functional Capabilities

The platform provides comprehensive coverage of enterprise business processes:

- **User & Security Management** - Authentication, authorization, role-based access control (RBAC)
- **Multi-Tenancy** - Complete data isolation per tenant with shared infrastructure
- **Master Data Management** - Organizations, locations, users, customers, vendors
- **Sales Operations** - Orders, quotations, pricing, customer management
- **Purchase Operations** - Purchase orders, supplier management, procurement workflows
- **Inventory Control** - Warehouse management, stock tracking, goods receipt/issue
- **Accounting** - General ledger, journals, vouchers, financial reconciliation
- **Human Resources** - Employee profiles, attendance, payroll, benefits management
- **Agricultural Operations** - Farmer management, crop data, production planning
- **Manufacturing** - Production jobs, BOM management, quality control
- **Reporting** - Business intelligence, analytics, custom reports
- **Workflow Automation** - Scheduled tasks, notifications, process orchestration

## 3. Architecture Overview


### 3.1 Architectural Pattern

The ERP Platform uses a **Modular Monolith** architecture:

```
┌─────────────────────────────────────────────┐
│         Spring Boot Application             │
├─────────────────────────────────────────────┤
│  Security Layer (JWT, OIDC, Authorization)  │
├─────────────────────────────────────────────┤
│  Domain Modules (20+ business modules)      │
│  ├─ Admin          ├─ CRM             ├─ HR       │
│  ├─ Accounting     ├─ Sales           ├─ Payroll  │
│  ├─ Inventory      ├─ Purchase        ├─ Reports  │
│  ├─ Manufacturing  ├─ Agriculture     ├─ Workflow │
│  └─ ... and more                               │
├─────────────────────────────────────────────┤
│  Shared Services (Email, SMS, AI, Cache)     │
├─────────────────────────────────────────────┤
│  Data Layer (Spring Data JPA, Hibernate)     │
├─────────────────────────────────────────────┤
│  Databases (H2 or PostgreSQL)                │
└─────────────────────────────────────────────┘
```

**Benefits of this approach:**
- Single deployment unit simplifies DevOps
- Cross-module transactions maintain data consistency
- Clear module boundaries enable feature independence
- Future path to microservices if needed

### 3.2 Request Flow

A typical authenticated API request follows this flow:

```
Client Request
    ↓
SecurityFilterChain → JWT Validation
    ↓
REST Controller → Input Validation
    ↓
Domain Service → Business Logic
    ↓
Tenant Context Resolution
    ↓
Spring Data Repository / JPA
    ↓
H2 (dev) or PostgreSQL (prod)
    ↓
ApiResponse → JSON Response
```

### 3.3 Domain Modules

| Module | Purpose |
|--------|---------|
| **admin** | User, role, privilege, and system administration |
| **auth** | Authentication, login, JWT, OIDC SSO |
| **organization** | Company, department, location management |
| **master** | Master data entities and configurations |
| **crm** | Customer relationship management, leads, accounts |
| **sales** | Sales orders, quotations, customer invoices |
| **purchase** | Purchase orders, vendor management, receipts |
| **supplier** | Supplier profiles, performance tracking |
| **pricing** | Price lists, discounts, promotions |
| **dispatch** | Shipment tracking, logistics, delivery |
| **inventory** | Warehouse, stock operations, transfers |
| **accounting** | Ledgers, journals, vouchers, reconciliation |
| **expense** | Expense tracking and reimbursement |
| **hr** | Employee management, org structure, attendance |
| **payroll** | Salary processing, benefits, deductions |
| **agri** | Agriculture operations, farmer management |
| **production** | Production jobs, scheduling, BOMs |
| **processing** | Material processing sequences |
| **manufacturing** | Manufacturing workflows and tracking |
| **quality** | Quality control, inspections, compliance |
| **intake** | Goods receipt and intake workflows |
| **reports** | Business intelligence and analytics |
| **scheduler** | Background jobs and scheduled tasks |
| **workflow** | Workflow orchestration and approvals |
| **ai** | AI assistant integration and management |
| **fieldiot** | Field data, weather, satellite integration |

### 3.4 Security Architecture

- **Authentication:** JWT (JSON Web Tokens) with configurable expiration
- **Authorization:** Role-based access control (RBAC) with fine-grained privileges
- **Multi-Tenancy:** Automatic tenant context resolution per request
- **SSO Support:** OIDC integration with Google Cloud Identity and Microsoft Entra ID
- **Data Isolation:** Complete tenant data segregation at database level

## 4. Technology Stack

### 4.1 Core Technologies

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.3.0 |
| **Build Tool** | Apache Maven | 3.9+ |
| **Runtime** | Spring Framework | 6.0+ |

### 4.2 Persistence & Databases

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **ORM** | Hibernate + JPA | Object-relational mapping |
| **Query Builder** | Spring Data JPA | Repository pattern implementation |
| **Dev Database** | H2 | In-memory relational DB (development/testing) |
| **Prod Database** | PostgreSQL | 14+ production database |
| **Migrations** | Flyway | Database schema versioning |

### 4.3 Security & Authentication

| Component | Technology |
|-----------|-----------|
| Authentication | JWT (JJWT 0.12.3) |
| Authorization | Spring Security |
| SSO/OIDC | Spring Security OAuth2 |
| Password Encoding | BCrypt |

### 4.4 API & Documentation

| Component | Technology | Version |
|-----------|-----------|---------|
| **REST Framework** | Spring Web MVC | 6.0+ |
| **API Docs** | SpringDoc OpenAPI | 2.5.0 |
| **Swagger UI** | Swagger | Latest |
| **Validation** | Spring Validation + Hibernate Validator | - |

### 4.5 Supporting Libraries

| Library | Purpose | Version |
|---------|---------|---------|
| Lombok | Code generation (getters, setters) | 1.18.32 |
| MapStruct | DTO mapping | 1.5.5 |
| Caffeine | Caching | Latest |
| Spring Mail | Email integration | 3.3.0 |

### 4.6 Testing

| Framework | Purpose |
|-----------|---------|
| Spring Boot Test | Integration testing |
| Spring Security Test | Security testing |
| JUnit 5 (Jupiter) | Unit testing |
| Testcontainers | Containerized PostgreSQL testing |
| MockMvc | HTTP request testing |

### 4.7 Deployment

| Component | Technology |
|-----------|-----------|
| **Containerization** | Docker (multi-stage builds) |
| **Base Image** | Eclipse Temurin 21 JRE |
| **App Server** | Apache Tomcat (embedded) |
| **Cloud** | Railway (primary deployment platform) |

## 5. Implementation Details

### 5.1 Project Structure

```
src/main/
├── java/com/erp/platform/
│   ├── ErpPlatformApplication.java      # Application entry point
│   ├── common/                          # Shared utilities and DTOs
│   │   ├── audit/                       # Audit trail
│   │   ├── dto/                         # Data transfer objects
│   │   ├── email/                       # Email service
│   │   ├── exception/                   # Exception handling
│   │   ├── sms/                         # SMS/WhatsApp service
│   │   ├── tenant/                      # Multi-tenancy context
│   │   └── util/                        # Utility functions
│   ├── config/                          # Spring configurations
│   ├── security/                        # JWT, OIDC, authorization
│   └── modules/                         # 20+ domain modules
│       ├── admin/                       # Admin operations
│       ├── auth/                        # Authentication
│       ├── crm/                         # Customer management
│       ├── sales/                       # Sales operations
│       ├── inventory/                   # Inventory management
│       └── ... (other domains)
├── resources/
│   ├── application.properties           # Default (H2) configuration
│   ├── application-postgres.properties  # PostgreSQL profile
│   ├── application-railway.properties   # Railway deployment profile
│   └── db/migration/                    # Flyway SQL migrations
└── test/
    └── java/com/erp/platform/           # Integration and unit tests
```

### 5.2 Layered Architecture per Module

Each domain module typically follows this structure:

```
modules/{domain}/
├── controller/          # REST endpoints (@RestController)
├── service/             # Business logic (@Service)
├── repository/          # Data access (Spring Data JPA)
├── dto/                 # Data transfer objects
├── entity/              # JPA entities
├── exception/           # Domain-specific exceptions
├── enums/               # Enumerations
└── mapper/              # DTO ↔ Entity mapping (MapStruct)
```

### 5.3 Database Schema Management

- **Development (H2):** Automatic schema creation/drop on startup (`create-drop`)
- **Production (PostgreSQL):** Controlled migrations via Flyway
- **Migration Files:** Located in `src/main/resources/db/migration/`
- **Version Naming:** `V{sequence}__description.sql` (Flyway convention)

### 5.4 API Design

- **Base URL:** `http://localhost:8080/api/v1`
- **Authentication:** JWT bearer token in `Authorization` header
- **Request/Response:** JSON format
- **Pagination:** Supported via `page` and `size` query parameters
- **Sorting:** Supported via `sort` query parameter
- **Error Handling:** Standardized `ApiResponse` wrapper with error codes

## 6. Configuration & Environment

### 6.1 Development Configuration (H2 Profile)

**Default Profile:** Automatic on startup using H2 in-memory database

**Properties:**
```properties
spring.datasource.url=jdbc:h2:mem:erpdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

**Access H2 Console:**
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:erpdb`
- User: `sa`
- Password: (empty)

### 6.2 PostgreSQL Configuration (Local)

Start with PostgreSQL profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

**Default Connection:**
- Host: `localhost:5432`
- Database: `erp_platform`
- User: `erp_user`
- Password: `erp_password`

**Prerequisites:**
```bash
# Create database and user
createdb erp_platform
psql -U postgres -d erp_platform -c "CREATE USER erp_user WITH ENCRYPTED PASSWORD 'erp_password';"
psql -U postgres -d erp_platform -c "GRANT ALL PRIVILEGES ON DATABASE erp_platform TO erp_user;"
```

### 6.3 Environment Variables

| Variable | Purpose | Example |
|----------|---------|---------|
| `JWT_SECRET` | JWT signing key (production required) | `your-256-bit-secret-key` |
| `JWT_EXPIRATION` | Token expiration in ms | `86400000` |
| `DB_HOST` | Database hostname | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name | `erp_platform` |
| `DB_USER` | Database user | `erp_user` |
| `DB_PASS` | Database password | `secure-password` |
| `AI_PROVIDER` | LLM provider | `gemini`, `claude`, `openai-compatible` |
| `GEMINI_API_KEY` | Google AI Studio API key | - |
| `ANTHROPIC_API_KEY` | Anthropic Claude API key | - |
| `MAIL_HOST` | SMTP server | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | SMTP username | - |
| `MAIL_PASSWORD` | SMTP password | - |
| `MAIL_ENABLED` | Enable email sending | `true`, `false` |
| `SMS_PROVIDER` | SMS provider | `NONE`, `TWILIO` |
| `TWILIO_ACCOUNT_SID` | Twilio account ID | - |
| `TWILIO_AUTH_TOKEN` | Twilio auth token | - |
| `SSO_ENABLED` | Enable OIDC SSO | `true`, `false` |
| `SSO_GOOGLE_CLIENT_ID` | Google OAuth client ID | - |
| `SSO_MS_CLIENT_ID` | Microsoft OAuth client ID | - |

## 7. Building & Testing

### 7.1 Build Commands

```bash
# Build project (skipping tests)
mvn clean package

# Build with tests
mvn clean package -DskipTests=false

# Run tests only
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest
```

### 7.2 Running Locally

```bash
# Development (H2)
mvn spring-boot:run

# With PostgreSQL
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# Packaged JAR
java -jar target/erp-platform-1.0.0.jar

# With custom profile
java -Dspring.profiles.active=postgres -jar target/erp-platform-1.0.0.jar
```

### 7.3 Test Coverage

The project includes:
- **Unit Tests:** Service and utility layer tests
- **Integration Tests:** Database and transaction tests
- **Security Tests:** Authentication and authorization tests
- **API Tests:** REST endpoint validation

Run all tests:
```bash
mvn test
```

## 8. Docker & Deployment

### 8.1 Building Docker Image

**Multi-stage build process:**

```bash
# Build image
docker build -t erp-platform:latest .

# Run container
docker run --rm -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=erp_platform \
  -e DB_USER=erp_user \
  -e DB_PASS=secure-password \
  -e JWT_SECRET=your-256-bit-secret \
  erp-platform:latest
```

**Image Specifications:**
- **Build Stage:** Maven 3.9 + Eclipse Temurin JDK 21
- **Runtime Stage:** Eclipse Temurin JRE 21 (minimal)
- **Base OS:** Alpine Linux
- **Size:** ~400MB (minimal runtime image)
- **Health Check:** `/actuator/health`
- **Port:** 8080 (configurable via `SERVER_PORT`)

### 8.2 Railway Deployment

The `railway.toml` configuration enables deployment on Railway platform:

```bash
# Set environment variables in Railway console:
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS, JWT_SECRET

# Push to Railway
git push railway main
```

**Railway-specific Configuration:**
- Profile: `railway` (PostgreSQL + Flyway migrations)
- Health Check: `/actuator/health`
- Port: Auto-configured via `$PORT` environment variable

### 8.3 Docker Compose (Local Testing)

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: erp_platform
      POSTGRES_USER: erp_user
      POSTGRES_PASSWORD: erp_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  erp-app:
    build: .
    environment:
      - SPRING_PROFILES_ACTIVE=railway
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=erp_platform
      - DB_USER=erp_user
      - DB_PASS=erp_password
      - JWT_SECRET=your-256-bit-secret
    ports:
      - "8080:8080"
    depends_on:
      - postgres

volumes:
  postgres_data:
```

Start with:
```bash
docker-compose up -d
```

## 9. API Documentation

### 9.1 Authentication

**Login Endpoint:**
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password"
}

Response:
{
  "token": "eyJhbGc...",
  "expiresIn": 86400000,
  "user": { ... }
}
```

**Using Token:**
```
Authorization: Bearer eyJhbGc...
```

### 9.2 Interactive API Documentation

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Health:** `http://localhost:8080/actuator/health`
- **Info:** `http://localhost:8080/actuator/info`

### 9.3 Common API Patterns

All domain endpoints follow RESTful conventions:

```
GET    /api/v1/{resource}           # List all
GET    /api/v1/{resource}/{id}      # Get by ID
POST   /api/v1/{resource}           # Create
PUT    /api/v1/{resource}/{id}      # Update
DELETE /api/v1/{resource}/{id}      # Delete
```

## 10. Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| **Port 8080 already in use** | Change port: `java -Dserver.port=9090 -jar ...` |
| **PostgreSQL connection error** | Verify credentials and DB exists: `psql -h localhost -U erp_user -d erp_platform` |
| **JWT token invalid** | Ensure `JWT_SECRET` is set and consistent across instances |
| **H2 console not accessible** | Verify `spring.h2.console.enabled=true` in application.properties |
| **Flyway migration failure** | Check SQL syntax in `src/main/resources/db/migration/` |
| **Test failures with PostgreSQL** | Ensure Testcontainers Docker is available |

### Debug Logging

Enable debug logs in `application.properties`:

```properties
logging.level.com.erp=DEBUG
logging.level.org.springframework=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## 11. Limitations

- **No Frontend:** Backend service only; frontend/UI must be developed separately
- **Single Instance Processing:** Background jobs run within the application instance (not distributed)
- **H2 Limitation:** In-memory database is ephemeral; not suitable for persistent data in production
- **External Dependencies:** AI, email, SMS features depend on third-party provider availability
- **Horizontal Scaling:** Stateless design allows scaling, but shared scheduler jobs need coordination
- **Authentication:** Currently supports local credentials and OIDC; no LDAP/Active Directory

## 12. Future Enhancements

Planned improvements and potential extensions:

- **Microservices Migration:** Extract high-volume domains as independent services
- **Async Processing:** Implement message queues (Kafka, RabbitMQ) for async workflows
- **Advanced Analytics:** Business intelligence dashboard and reporting engine
- **Audit & Compliance:** Enhanced audit logging and compliance reporting
- **Observability:** Distributed tracing (Jaeger), metrics (Prometheus), logging (ELK)
- **Performance Optimization:** Query optimization, caching strategies, database indexing
- **GraphQL API:** Add GraphQL endpoint alongside REST API
- **Mobile Support:** Native mobile app SDKs and APIs
- **Internationalization:** Multi-language and multi-currency support
- **Advanced Workflows:** BPM engine integration for complex workflows

## 13. Contributing

Guidelines for contributing to this project:

1. **Code Standards:**
   - Follow existing code structure and naming conventions
   - Use meaningful commit messages
   - Write tests for new functionality
   - Keep methods small and focused

2. **Pull Request Process:**
   - Create feature branch from `develop`
   - Make focused, atomic commits
   - Add tests covering new code
   - Update documentation as needed
   - Request review from team members

3. **Testing Requirements:**
   - All tests must pass: `mvn test`
   - Maintain or improve code coverage
   - Test both success and failure scenarios
   - Include integration tests for new features

## 14. Project Structure Reference

### Source Organization

```
ERP_Backend/
├── src/
│   ├── main/
│   │   ├── java/com/erp/platform/
│   │   │   ├── common/              # Shared code
│   │   │   ├── config/              # Spring configs
│   │   │   ├── security/            # Auth & security
│   │   │   └── modules/             # Domain modules
│   │   └── resources/
│   │       ├── application*.properties
│   │       └── db/migration/        # Flyway scripts
│   └── test/
│       └── java/com/erp/platform/   # Test classes
├── pom.xml                          # Maven config
├── Dockerfile                       # Container build
├── railway.toml                     # Railway config
├── README.md                        # This file
└── LICENSE                          # License info
```

## 15. Support & Documentation

### Additional Resources

- **Architecture Diagram:** See `architecture.html` for system architecture
- **Flow Diagrams:** See `flow-diagram.html` for request flow documentation
- **API Spec:** Generated at `/v3/api-docs` (OpenAPI format)
- **Swagger UI:** Interactive API explorer at `/swagger-ui.html`

### Getting Help

- Check existing issues in the repository
- Review API documentation in Swagger UI
- Enable debug logging for troubleshooting
- Check database migrations in `src/main/resources/db/migration/`

## 16. License

This project is provided under the terms specified in the LICENSE file.
