# ContractGuard - Backend Setup Guide

## 🎯 Project Overview

ContractGuard is an API contract management and breaking change detection platform. It helps organizations:
- Manage API contracts (OpenAPI specifications)
- Detect breaking changes between versions
- Track API consumers and their dependencies
- Validate actual APIs against contracts
- Notify consumers of changes

## 📋 Prerequisites

- Java 21 or higher
- Docker and Docker Compose (for PostgreSQL and Redis)
- Maven 3.8+
- Git

## 🚀 Quick Start

### 1. Clone Repository
```bash
cd /home/lewis/Desktop/ContractGuard
```

### 2. Start Database and Cache with Docker

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** (port 5432)
  - Database: `contractguard_db`
  - User: `contractguard_user`
  - Password: `contractguard_pass`
  
- **Redis** (port 6379)
  - For caching and session management

Verify services are running:
```bash
docker-compose ps
```

### 3. Build the Project

```bash
./mvnw clean package
```

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 5. Access API Documentation

Once the application is running, access Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

Or OpenAPI JSON:
```
http://localhost:8080/v3/api-docs
```

## 📦 Project Structure

```
src/main/
├── java/ContractGuard/ContractGuard/
│   ├── config/                 # Spring configurations
│   │   ├── CacheConfig.java    # Redis caching
│   │   ├── CorsConfig.java     # CORS settings
│   │   ├── JpaAuditingConfig.java
│   │   ├── OpenApiConfig.java  # Swagger/OpenAPI config
│   │   └── SecurityConfig.java # JWT security
│   ├── controller/             # REST endpoints
│   │   ├── AuthController.java
│   │   ├── ContractController.java
│   │   └── ConsumerController.java
│   ├── dto/                    # Data transfer objects
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── ...
│   ├── entity/                 # JPA entities (database models)
│   │   ├── Organization.java
│   │   ├── User.java
│   │   ├── Contract.java
│   │   ├── Consumer.java
│   │   ├── ValidationResult.java
│   │   └── ...
│   ├── exception/              # Custom exceptions and handlers
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── BadRequestException.java
│   ├── repository/             # Spring Data JPA repositories
│   │   ├── ContractRepository.java
│   │   ├── ConsumerRepository.java
│   │   ├── ValidationResultRepository.java
│   │   └── ...
│   ├── security/               # JWT and authentication
│   │   ├── JwtProvider.java
│   │   └── JwtAuthenticationFilter.java
│   ├── service/                # Business logic
│   │   ├── AuthService.java
│   │   ├── ContractService.java
│   │   └── ConsumerService.java
│   └── ContractGuardApplication.java
└── resources/
    ├── application.yaml        # Main config
    └── db/migration/          # Flyway database migrations
        └── V1__Initial_Schema.sql
```

## 🗄️ Database Schema

The application uses PostgreSQL with the following main tables:

### Core Tables
- **organizations** - API provider organizations
- **users** - System users with role-based access
- **contracts** - API contracts (OpenAPI specs)
- **contract_versions** - Historical contract versions
- **consumers** - API consumer services/apps
- **consumer_registrations** - Consumer → Contract subscriptions

### Feature Tables
- **validation_results** - API validation test results
- **breaking_changes** - Detected breaking changes between versions
- **notifications** - User and consumer notifications
- **webhook_configs** - Webhooks for event delivery
- **api_usage_logs** - API usage analytics
- **audit_logs** - System audit trail

All tables are created automatically by **Flyway migrations** on first run.

## 🔐 Authentication

The application uses **JWT (JSON Web Tokens)** for authentication.

### Register a New User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "fullName": "John Doe",
    "organizationName": "Acme Corp",
    "organizationSlug": "acme-corp"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!"
  }'
```

This returns a JWT token in the response. Use it for subsequent requests:
```bash
Authorization: Bearer <your_jwt_token>
```

## 📚 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `GET /api/v1/auth/validate` - Validate JWT token

### Contracts
- `POST /api/v1/contracts` - Create contract
- `GET /api/v1/contracts/{contractId}` - Get contract
- `GET /api/v1/contracts` - List contracts
- `PUT /api/v1/contracts/{contractId}` - Update contract
- `DELETE /api/v1/contracts/{contractId}` - Delete contract
- `POST /api/v1/contracts/{contractId}/publish` - Publish contract
- `POST /api/v1/contracts/{contractId}/deprecate` - Deprecate contract
- `POST /api/v1/contracts/{contractId}/retire` - Retire contract

### Consumers
- `POST /api/v1/consumers` - Register consumer
- `GET /api/v1/consumers/{consumerId}` - Get consumer
- `GET /api/v1/consumers` - List consumers
- `PUT /api/v1/consumers/{consumerId}` - Update consumer
- `DELETE /api/v1/consumers/{consumerId}` - Delete consumer

See Swagger UI for complete API documentation at `/swagger-ui.html`

## 🔧 Configuration

### application.yaml
The main configuration file includes:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/contractguard_db
    username: contractguard_user
    password: contractguard_pass
  
  jpa:
    hibernate:
      ddl-auto: validate  # Validates schema, doesn't create/drop
  
  security:
    jwt:
      secret: your-super-secret-jwt-key-change-in-production
      expiration: 86400000  # 24 hours
  
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8080
```

### Environment Profiles
- **dev** - Development with SQL logging enabled
- **prod** - Production with optimized settings

Switch profiles:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

## 📝 Development Workflow

### 1. Make Code Changes
Create new features in appropriate packages (controller, service, repository, etc.)

### 2. Build Locally
```bash
./mvnw clean compile
```

### 3. Run Tests
```bash
./mvnw test
```

### 4. Start Application
```bash
./mvnw spring-boot:run
```

### 5. Test with Swagger UI
Navigate to `http://localhost:8080/swagger-ui.html` and test endpoints interactively

## 🐛 Troubleshooting

### Database Connection Failed
- Ensure Docker containers are running: `docker-compose ps`
- Verify PostgreSQL is accessible: `docker-compose logs postgres`
- Reset database: `docker-compose down -v && docker-compose up -d`

### Port Already in Use
- Change port in `application.yaml` under `server.port`
- Or kill process using port: `lsof -ti:8080 | xargs kill -9`

### JWT Token Invalid
- Ensure `Authorization: Bearer <token>` header format is correct
- Check token hasn't expired (24 hour default)
- Verify JWT secret in `application.yaml` hasn't changed

### Cache Issues
- Clear Redis cache: `docker exec contractguard-redis redis-cli FLUSHALL`
- Restart Redis: `docker-compose restart redis`

## 📦 Maven Commands

| Command | Purpose |
|---------|---------|
| `./mvnw clean` | Remove build artifacts |
| `./mvnw compile` | Compile source code |
| `./mvnw test` | Run unit tests |
| `./mvnw package` | Package as JAR |
| `./mvnw spring-boot:run` | Run application |
| `./mvnw install` | Install to local repository |

## 🚀 Deployment

### Docker Build
```bash
docker build -t contractguard:latest .
docker run -p 8080:8080 --env-file .env contractguard:latest
```

### Environment Variables for Production
```env
DB_URL=jdbc:postgresql://prod-db:5432/contractguard_db
DB_USER=contractguard_user
DB_PASSWORD=secure_password_here
REDIS_HOST=prod-redis
REDIS_PORT=6379
JWT_SECRET=very_long_secure_secret_key_here_minimum_256_bits
JWT_EXPIRATION=86400000
SPRING_PROFILES_ACTIVE=prod
```

## 📋 Implementation Phases

This project is being built in phases:

### ✅ Phase 1: Maven Dependencies & Configuration
- Added all required dependencies
- Configured Spring Boot, Security, JWT, Redis, Flyway

### ✅ Phase 2: Database Setup & Flyway Migrations
- Created Flyway migration with complete schema
- Set up PostgreSQL with Docker Compose
- Configured Hibernate validation mode

### ✅ Phase 3: Java Project Structure & Core Entities
- Created entity classes for all database tables
- Configured JPA mappings with relationships
- Set up Lombok for boilerplate reduction

### ✅ Phase 4: Repository Layer
- Implemented Spring Data JPA repositories
- Created custom query methods for business logic
- Added pagination and search support

### ✅ Phase 5: Core Service Layer Foundation
- Implemented ContractService with CRUD operations
- Implemented AuthService with registration and login
- Implemented ConsumerService for consumer management
- Added caching decorators using Redis

### ✅ Phase 6: API Security & Configuration
- Created SecurityConfig with JWT authentication
- Implemented JwtProvider for token generation/validation
- Added CORS configuration
- Created GlobalExceptionHandler for error handling

### ✅ Phase 7: Core API Controllers & CRUD Endpoints
- Implemented AuthController (register, login)
- Implemented ContractController (CRUD + publish/deprecate)
- Implemented ConsumerController (CRUD + search)

### 🔄 Phase 8-12: Advanced Features (Coming Next)
- Breaking Change Detection Service
- Validation Engine & API Comparison
- Notification Service & Webhooks
- Analytics & Reporting
- Integration Testing & Documentation

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit pull request

## 📄 License

MIT License - See LICENSE file for details

## ✉️ Support

For issues and questions:
- GitHub Issues: https://github.com/your-org/contractguard/issues
- Email: support@contractguard.dev

---

**Last Updated:** December 6, 2025
**Current Version:** 0.0.1-SNAPSHOT

