# ContractGuard 🛡️

ContractGuard is an enterprise-grade **API Contract Testing Platform** built to ensure seamless integration between service providers and consumers. It automates the validation of API specifications, catching breaking changes before they reach production.

---

## 📖 Table of Contents
- [Project Overview](#-project-overview)
- [Architecture & Design](#-architecture--design)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Deployment](#-deployment)

---

## 🌟 Project Overview
In a microservices ecosystem, service dependencies are the most common point of failure. **ContractGuard** acts as a centralized gatekeeper. It allows developers to:
1. Define strict API contracts.
2. Validate incoming and outgoing payloads against these contracts in real-time.
3. Generate comprehensive compliance reports for CI/CD pipelines.

---

## 🏗 Architecture & Design
The platform is built following **Clean Architecture** principles and a **Microservices-oriented** design:

- **Core Module:** Contains the domain entities and business logic for contract validation.
- **API Layer:** RESTful endpoints for contract management and test execution.
- **Persistence Layer:** PostgreSQL-driven storage with optimized PL/pgSQL procedures for high-performance data handling.
- **Security:** Integrated JWT-based authentication and Role-Based Access Control (RBAC).

---

## ✨ Key Features
- **Contract Definition:** Support for OpenAPI/Swagger and custom JSON schemas.
- **Mock Service Generation:** Automatically spin up mock providers based on existing contracts.
- **Version Control:** Track changes to contracts over time and manage breaking versions.
- **Automated Scanning:** Integrated security scanning for common API vulnerabilities.
- **Real-time Notifications:** Webhook support for alerting teams when a contract is violated.

---

## 🛠 Tech Stack
- **Backend:** Java 17, Spring Boot 3.4
- **Database:** PostgreSQL (Advanced stored procedures)
- **Security:** Spring Security & JWT
- **DevOps:** Docker (Multi-stage builds), Docker Compose, GitHub Actions
- **Cloud:** Optimized for Microsoft Azure deployment
- **Build System:** Maven (Wrapper included)

---

## 🚀 Getting Started

### Prerequisites
- **JDK 25**
- **Maven 3.9+**
- **Docker & Docker Compose**

### Installation & Local Setup

1. **Clone the repository**
   ```bash
   git clone [https://github.com/Lewi217/ContractGuard.git](https://github.com/Lewi217/ContractGuard.git)
   cd ContractGuard
```
Configure Environment Variables Create a .env file based on .env.example:

Bash

cp .env.example .env
# Update variables for Database and JWT secret
Database Setup Ensure PostgreSQL is running, then run the initialization scripts located in src/main/resources/db/.

Run the Application

Bash

./mvnw spring-boot:run
The API will be available at http://localhost:8080

Using Docker
To spin up the entire stack (App + DB):

Bash

docker-compose up --build
