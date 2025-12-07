# ContractGuard - Backend Architecture & API Documentation

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Deep Dive](#architecture-deep-dive)
3. [Database Schema](#database-schema)
4. [API Endpoints Reference](#api-endpoints-reference)
5. [Business Logic Flow](#business-logic-flow)
6. [Integration Points](#integration-points)
7. [Security Architecture](#security-architecture)
8. [End-to-End Scenarios](#end-to-end-scenarios)

---

## System Overview

### What Problem Are We Solving?

**Primary Problem:** API breaking changes cause production incidents

**Real-World Scenario:**

```
Day 1: Backend team changes User API response format
       Old: { "userId": "123", "name": "John" }
       New: { "id": "123", "fullName": "John" }

Day 2: Frontend crashes - Expected "userId", got "id"
       Mobile app breaks - Expected "name", got "fullName"
       3 microservices fail silently

Result: 4 hours downtime, angry customers, emergency hotfix
```

**Our Solution:** ContractGuard detects this BEFORE deployment

### Core Capabilities

- **Contract Management** - Store and version API contracts (OpenAPI specs)
- **Validation Engine** - Compare actual API vs contract
- **Breaking Change Detection** - Identify what will break
- **Consumer Tracking** - Know who depends on your APIs
- **Impact Analysis** - Visualize what breaks if you deploy
- **CI/CD Integration** - Block bad deployments automatically

---

## Architecture Deep Dive

### High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT APPLICATIONS                      │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │  Next.js    │  │   Mobile     │  │   CI/CD Pipeline │   │
│  │  Frontend   │  │     App      │  │  (GitHub/Azure)  │   │
│  └──────┬──────┘  └──────┬───────┘  └────────┬─────────┘   │
└─────────┼────────────────┼───────────────────┼──────────────┘
          │                │                   │
          │    HTTPS/REST  │                   │ Webhooks
          │                │                   │
┌─────────┴────────────────┴───────────────────┴──────────────┐
│                    API GATEWAY LAYER                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Spring Boot REST Controllers                 │   │
│  │  • Authentication Filter (JWT)                       │   │
│  │  • Request Validation                                │   │
│  │  • Rate Limiting                                     │   │
│  │  • CORS Configuration                                │   │
│  └────────────────────┬─────────────────────────────────┘   │
└───────────────────────┼──────────────────────────────────────┘
                        │
┌───────────────────────┴──────────────────────────────────────┐
│                    SERVICE LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Contract    │  │  Validation  │  │    Consumer      │  │
│  │  Service     │  │  Service     │  │    Service       │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                 │                    │             │
│  ┌──────┴───────┐  ┌──────┴───────┐  ┌────────┴─────────┐  │
│  │ Change       │  │ Notification │  │   Analytics      │  │
│  │ Detection    │  │ Service      │  │   Service        │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└───────────────────────┬──────────────────────────────────────┘
                        │
┌───────────────────────┴──────────────────────────────────────┐
│                  REPOSITORY LAYER (Data Access)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Contract    │  │   Consumer   │  │   Validation     │  │
│  │  Repository  │  │   Repository │  │   Repository     │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
└─────────┼──────────────────┼───────────────────┼─────────────┘
          │                  │                   │
┌─────────┴──────────────────┴───────────────────┴─────────────┐
│                    DATA LAYER                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  PostgreSQL  │  │    Redis     │  │   Azure Blob     │  │
│  │  (Primary)   │  │   (Cache)    │  │    Storage       │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└───────────────────────────────────────────────────────────────┘
```

### Component Breakdown

#### 1. API Gateway Layer

**Purpose:** Entry point for all requests, handles cross-cutting concerns

**Components:**

```
SecurityConfig.java
├── JWT Authentication Filter
│   └── Validates Bearer tokens
│   └── Extracts user context
│   └── Sets SecurityContext
│
├── CORS Configuration
│   └── Allow origins: frontend domain
│   └── Allow methods: GET, POST, PUT, DELETE
│   └── Allow headers: Authorization, Content-Type
│
└── Rate Limiting (Redis-based)
    └── 100 requests/minute per user
    └── 1000 requests/minute per organization
```

**Request Flow:**

1. Request arrives → JWT Filter
2. Token validated → User loaded
3. Authorization checked → Role-based access
4. Rate limit checked → Redis counter
5. Request forwarded → Controller

#### 2. Service Layer

##### A. ContractService

**Responsibilities:**
- Create, read, update, delete contracts
- Version management
- Parse and validate OpenAPI specs
- Store contracts in database and blob storage

**Key Methods:**

ContractService {
    createContract(CreateContractRequest) → ContractResponse
    getContract(UUID contractId) → ContractDetailResponse
    updateContract(UUID id, UpdateRequest) → ContractResponse
    listContracts(filters, pagination) → PagedContractResponse
    deleteContract(UUID contractId) → void
    getContractVersions(UUID contractId) → List<ContractVersion>
    compareVersions(UUID id, v1, v2) → VersionComparisonResponse
    publishContract(UUID contractId) → ContractResponse
    deprecateContract(UUID id, deprecationDate) → ContractResponse
}

**Internal Logic:**

```text
createContract():
  1. Parse OpenAPI YAML/JSON
  2. Validate schema structure
  3. Extract metadata (endpoints, models)
  4. Create Contract entity
  5. Save to PostgreSQL
  6. Upload full spec to Blob Storage
  7. Cache in Redis
  8. Return response
```

##### B. ValidationService

**Responsibilities:**
- Validate actual API responses against contracts
- Run contract compliance checks
- Generate validation reports
- Track validation history

**Key Methods:**

```text
ValidationService {
    validateEndpoint(ValidateEndpointRequest) → ValidationResult
    validateFullApi(UUID contractId, apiBaseUrl) → FullValidationReport
    cicdValidation(CICDRequest) → CICDValidationResponse
    getValidationHistory(UUID contractId) → List<ValidationResult>
    rerunValidation(UUID validationId) → ValidationResult
}
```

**Validation Algorithm:**

```text
validateEndpoint():
  1. Fetch contract from cache/database
  2. Extract endpoint schema (path, method, params)
  3. Make actual HTTP request to API
  4. Compare response schema vs contract:
     a. Check status code
     b. Validate response headers
     c. Validate response body structure
     d. Check data types
     e. Verify required fields
     f. Check enum values
  5. Generate detailed diff
  6. Save result to database
  7. Send notifications if failed
  8. Return ValidationResult
```

##### C. BreakingChangeDetector

**Responsibilities:**
- Compare two contract versions
- Identify breaking vs non-breaking changes
- Calculate impact score
- Generate migration recommendations

**Key Methods:**

```text
BreakingChangeDetector {
    detectChanges(oldSpec, newSpec) → BreakingChangeReport
    analyzeImpact(changes, consumers) → ImpactAnalysisReport
    generateMigrationGuide(changes) → MigrationGuide
    isBreaking(change) → boolean
}
```

**Breaking Change Rules:**

BREAKING CHANGES:
1. Endpoint removed
2. HTTP method changed
3. Required parameter added
4. Parameter removed
5. Response field removed
6. Field type changed (string → number)
7. Field made required (was optional)
8. Enum values reduced
9. URL path changed
10. Status code changed

NON-BREAKING CHANGES:
1. New optional parameter added
2. New response field added
3. New endpoint added
4. Field made optional (was required)
5. Documentation updated
6. Example values changed

**Detection Algorithm:**

```text
detectChanges(oldSpec, newSpec):
  changes = []
  // Check endpoints
  for each endpoint in oldSpec:
    if endpoint not in newSpec:
      changes.add(BREAKING: "Endpoint removed")
    else:
      if method changed:
        changes.add(BREAKING: "Method changed")
      if parameters changed:
        changes.add(analyzeParameterChanges())
      if response schema changed:
        changes.add(analyzeResponseChanges())
  // Check new endpoints (non-breaking)
  for each endpoint in newSpec:
    if endpoint not in oldSpec:
      changes.add(NON_BREAKING: "New endpoint")
  return BreakingChangeReport(changes)
```

##### D. ConsumerService

**Responsibilities:**
- Register consumers (apps/services using APIs)
- Track which consumers use which contracts
- Notify consumers of changes
- Manage consumer metadata

**Key Methods:**

ConsumerService {
    registerConsumer(RegisterConsumerRequest) → ConsumerResponse
    subscribeToContract(consumerId, contractId, version) → Subscription
    getContractConsumers(UUID contractId) → List<Consumer>
    getConsumerContracts(UUID consumerId) → List<Contract>
    notifyConsumers(contractId, changes) → NotificationResult
    trackUsage(consumerId, contractId, endpoint) → void
}

##### E. NotificationService

**Responsibilities:**
- Send notifications to users and consumers
- Support multiple channels (email, Slack, webhooks)
- Queue and batch notifications
- Track notification delivery

**Key Methods:**

NotificationService {
    sendBreakingChangeAlert(contractId, changes, consumers) → void
    sendValidationFailure(validationResult) → void
    sendDeprecationWarning(contractId, deprecationDate) → void
    sendEmail(to, subject, body) → void
    sendSlackMessage(channel, message) → void
    triggerWebhook(url, payload) → void
}

##### F. AnalyticsService

**Responsibilities:**
- Track API usage metrics
- Generate reports
- Calculate trends
- Provide insights

**Key Methods:**

AnalyticsService {
    getContractStats(UUID contractId) → ContractStats
    getValidationMetrics(timeRange) → ValidationMetrics
    getConsumerActivity(UUID consumerId) → ActivityReport
    getBreakingChangeTrends(timeRange) → TrendReport
    calculateHealthScore(UUID contractId) → HealthScore
}

#### 3. Repository Layer

**Purpose:** Abstract database operations, provide clean data access API

ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findByStatus(ContractStatus status)
    List<Contract> findByOrganizationId(UUID orgId)
    Optional<Contract> findByNameAndVersion(String name, String version)
    @Query("SELECT c FROM Contract c WHERE c.name LIKE %:searchTerm%")
    List<Contract> searchByName(@Param("searchTerm") String term)
}

ConsumerRepository extends JpaRepository<Consumer, UUID> {
    List<Consumer> findByOrganizationId(UUID orgId)
    Optional<Consumer> findByApiKey(String apiKey)
}

ValidationResultRepository extends JpaRepository<ValidationResult, UUID> {
    List<ValidationResult> findByContractIdOrderByCreatedAtDesc(UUID contractId)
    List<ValidationResult> findByStatus(ValidationStatus status)
    @Query("SELECT v FROM ValidationResult v WHERE v.contractId = :contractId AND v.createdAt >= :since")
    List<ValidationResult> findRecentValidations(UUID contractId, LocalDateTime since)
}

#### 4. Data Layer

##### PostgreSQL Schema

```
┌─────────────────────┐
│   organizations     │
├─────────────────────┤
│ id (PK)             │
│ name                │
│ slug (unique)       │
│ created_at          │
└──────────┬──────────┘
           │
           │ 1:N
           │
┌──────────┴──────────┐
│     contracts       │
├─────────────────────┤
│ id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
│ organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
│ name VARCHAR(255) NOT NULL,
│ description TEXT,
│ version VARCHAR(50) NOT NULL,
│ status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
│ base_path VARCHAR(255) NOT NULL,
│ openapi_spec JSONB NOT NULL,
│ blob_storage_url VARCHAR(500),
│ tags VARCHAR(255)[],
│ created_by UUID REFERENCES users(id),
│ created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
│ updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
│ deprecated_at TIMESTAMP,
│ retired_at TIMESTAMP,
│ UNIQUE(organization_id, name, version)
);
```

(Full SQL schema continues in the document with contract_versions, consumers, consumer_registrations, validation_results, breaking_changes, notifications, api_usage_logs, webhook_configs, audit_logs, triggers, and indexes.)

---

## API Endpoints Reference

### Base URL

- **Production:** `https://api.contractguard.dev`
- **Development:** `http://localhost:8080`

### Authentication

All endpoints (except auth endpoints) require JWT token:

```
Authorization: Bearer <jwt_token>
```

---

### 1. Authentication Endpoints

#### POST /api/v1/auth/register

Register new user and organization

**Request:**

```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "organizationName": "Acme Corp",
  "organizationSlug": "acme-corp"
}
```

**Response (201 Created):**

```json
{
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "ADMIN"
  },
  "organization": {
    "id": "uuid",
    "name": "Acme Corp",
    "slug": "acme-corp",
    "plan": "FREE"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### POST /api/v1/auth/login

Authenticate user and return JWT token

**Request:**

```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "ADMIN"
  }
}
```

---

### 2. Contract Management Endpoints

#### POST /api/v1/contracts

Create a new API contract

**Request:**

```json
{
  "name": "User API",
  "version": "1.0.0",
  "organizationId": "org-uuid",
  "basePath": "/api/v1/users",
  "openapiSpec": { ... }  // OpenAPI 3.0 spec object
}
```

**Response (201 Created):**

```json
{
  "id": "contract-uuid",
  "name": "User API",
  "version": "1.0.0",
  "status": "DRAFT",
  "organizationId": "org-uuid",
  "basePath": "/api/v1/users",
  "openapiSpec": { ... },
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

#### GET /api/v1/contracts/{contractId}

Get contract details by ID

**Response (200 OK):**

```json
{
  "id": "contract-uuid",
  "name": "User API",
  "version": "1.0.0",
  "status": "DRAFT",
  "organizationId": "org-uuid",
  "basePath": "/api/v1/users",
  "openapiSpec": { ... },
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

#### PUT /api/v1/contracts/{contractId}

Update an existing contract

**Request:**

```json
{
  "name": "User API",
  "version": "1.0.1",
  "status": "ACTIVE",
  "openapiSpec": { ... }  // Updated OpenAPI 3.0 spec object
}
```

**Response (200 OK):**

```json
{
  "id": "contract-uuid",
  "name": "User API",
  "version": "1.0.1",
  "status": "ACTIVE",
  "organizationId": "org-uuid",
  "basePath": "/api/v1/users",
  "openapiSpec": { ... },
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-02T12:00:00Z"
}
```

#### DELETE /api/v1/contracts/{contractId}

Delete a contract

**Response (204 No Content):**

```
(no content)
```

---

### 3. Validation Endpoints

#### POST /api/v1/validate

Validate API responses against contract

**Request:**

```json
{
  "contractId": "contract-uuid",
  "apiBaseUrl": "https://api.example.com",
  "endpoints": [
    {
      "path": "/users/123",
      "method": "GET",
      "params": { "include": "profile" }
    }
  ]
}
```

**Response (200 OK):**

```json
{
  "contractId": "contract-uuid",
  "results": [
    {
      "endpoint": "/users/123",
      "method": "GET",
      "status": "PASS",
      "violations": []
    }
  ]
}
```

#### GET /api/v1/validation/results/{resultId}

Get validation result details

**Response (200 OK):**

```json
{
  "id": "result-uuid",
  "contractId": "contract-uuid",
  "endpoint": "/users/123",
  "method": "GET",
  "status": "FAIL",
  "violations": [
    {
      "field": "id",
      "expected": "string",
      "actual": "number"
    }
  ],
  "createdAt": "2024-01-02T12:00:00Z"
}
```

---

### 4. Breaking Change Detection Endpoints

#### POST /api/v1/breaking-changes/detect

Detect breaking changes between contract versions

**Request:**

```json
{
  "contractId": "contract-uuid",
  "oldVersion": "1.0.0",
  "newVersion": "1.0.1"
}
```

**Response (200 OK):**

```json
{
  "contractId": "contract-uuid",
  "changes": [
    {
      "type": "BREAKING",
      "description": "Response field 'age' removed"
    }
  ]
}
```

---

### 5. Consumer Registration Endpoints

#### POST /api/v1/consumers

Register a new consumer (e.g., frontend app)

**Request:**

```json
{
  "name": "Mobile App",
  "organizationId": "org-uuid",
  "apiKey": "abc123xyz",
  "contactEmail": "dev@example.com"
}
```

**Response (201 Created):**

```json
{
  "id": "consumer-uuid",
  "name": "Mobile App",
  "organizationId": "org-uuid",
  "apiKey": "abc123xyz",
  "contactEmail": "dev@example.com",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

#### GET /api/v1/consumers/{consumerId}

Get consumer details by ID

**Response (200 OK):**

```json
{
  "id": "consumer-uuid",
  "name": "Mobile App",
  "organizationId": "org-uuid",
  "apiKey": "abc123xyz",
  "contactEmail": "dev@example.com",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

---

### 6. Notification Endpoints

#### POST /api/v1/notifications

Send a notification (e.g., breaking change alert)

**Request:**

```json
{
  "contractId": "contract-uuid",
  "changes": [ "Response field 'age' removed" ],
  "consumers": [ "consumer-uuid" ]
}
```

**Response (202 Accepted):**

```
(accepted for processing)
```

---

### 7. Analytics Endpoints

#### GET /api/v1/analytics/contract/{contractId}

Get contract usage statistics

**Response (200 OK):**

```json
{
  "contractId": "contract-uuid",
  "totalRequests": 1000,
  "successRate": 98.5,
  "averageResponseTime": 250
}
```

---

## Business Logic Flow

### Contract Creation Flow

1. **User Action:** User uploads OpenAPI spec
2. **API Call:** `POST /api/v1/contracts`
3. **ContractService:**
   - Parse and validate spec
   - Generate contract ID
   - Save contract to database
   - Upload spec to blob storage
   - Cache contract in Redis
4. **Response:** Return contract details

### Validation Flow

1. **Trigger:** CI/CD pipeline detects contract change
2. **API Call:** `POST /api/v1/validate`
3. **ValidationService:**
   - Fetch contract and spec
   - Compare live API response with contract
   - Generate validation report
4. **Response:** Return validation results

### Breaking Change Detection Flow

1. **Trigger:** Contract version update
2. **API Call:** `POST /api/v1/breaking-changes/detect`
3. **BreakingChangeDetector:**
   - Compare old and new contract versions
   - Identify breaking changes
   - Generate migration guide
4. **Response:** Return breaking change report

### Consumer Notification Flow

1. **Trigger:** Breaking change detected
2. **API Call:** `POST /api/v1/notifications`
3. **NotificationService:**
   - Fetch affected consumers
   - Send notification of breaking change
4. **Response:** Return notification status

---

## Integration Points

- **CI/CD Integration:** Webhook to trigger validation on contract change
- **Slack Integration:** Notify #api-updates channel on breaking changes
- **Email Integration:** Alert users on contract deprecation

---

## Security Architecture

- **Authentication:** JWT tokens for user and service authentication
- **Authorization:** Role-based access control (RBAC)
- **Data Protection:** Encrypt sensitive data in transit and at rest
- **API Security:** Rate limiting, input validation, and logging

---

## End-to-End Scenarios

### Scenario 1: Contract Creation and Validation

1. User registers and logs in
2. User creates a new contract by uploading an OpenAPI spec
3. System validates and saves the contract
4. User triggers a validation for the live API
5. System compares the API response with the contract
6. Validation report is generated and sent to the user

### Scenario 2: Breaking Change Detection and Consumer Notification

1. Backend team updates an API contract (e.g., removes a response field)
2. System detects a breaking change in the contract version
3. Affected consumers are notified via email and Slack
4. Consumers update their integrations based on the migration guide

---

*Last Updated: December 2025*
