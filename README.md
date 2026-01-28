# credit-journey-document-management
Production grade document management REST platform for banking Credit Journey Digitalization initiative using Java Spring Boot


Document Management REST API (Enterprise-Grade)                                                                                
Overview:
This project implements a production-ready PDF Document Management REST API using Spring Boot. It supports secure document upload, retrieval, listing, and GDPR-compliant deletion with enterprise-grade validations, observability, and testing standards.
The system is designed following Clean / Layered Architecture principles with strong separation of concerns, high test coverage, and clear operational readiness.
________________________________________
Key Features:

• Secure document upload & download (streaming)
• MIME type and file size validation
• Duplicate detection using SHA-256 checksum
• Soft delete and GDPR-compliant hard delete
• User isolation (users access only their own documents)
• JWT-based authentication (stateless)
• Pagination, sorting, and filtering
• Centralized logging with correlation IDs
• OpenAPI / Swagger documentation
• Enterprise testing & quality gates
________________________________________
Architecture:

High-Level Design

• Controller Layer – REST endpoints, DTOs, API versioning
• Security Layer – JWT authentication filter, authorization
• Service Layer – Business logic, validations, checksum logic
• Domain Layer – Core entities and business models
• Repository Layer – Persistence abstraction (JDBC-Template)
• Storage Layer – Local File system / object storage abstraction and metadata saving in DB
• Cross-Cutting Concerns – Logging, metrics, exception handling
This architecture ensures maintainability, scalability, and testability.
________________________________________
Tech Stack:

Category     Technology
Language     Java 21
Framework    Spring Boot
Build Tool   Gradle
Database     H2
Security     Spring Security, JWT
API Docs     Swagger / OpenAPI
Testing      JUnit 5, Mockito, MockMvc
Quality      JaCoCo, SonarLint
________________________________________

API Endpoints (api/documentmgmt):

Method     Endpoint                                  Description
POST       api/documentmgmt/documentUpload           Upload document
GET        api/documentmgmt/documents/{documentId}   Download document
GET        api/documentmgmt/documentsListing         List documents
DELETE     api/documentmgmt/documentsDelete          Soft/Hard(GDPR) delete
________________________________________

Security:

• Stateless JWT authentication
• Token validated via filter
• User ID extracted from token
• Owner-based authorization enforced at service layer.Only authenticated users can access APIs, and users can access only their own documents.
________________________________________

Validation Rules:

• Allowed MIME types: PDF (configurable)
• Maximum file size: configurable
• Duplicate detection via SHA-256 checksum
• Standardized error responses
________________________________________

Error Handling:

• Global exception handler
• Consistent error response format
• Proper HTTP status codes
________________________________________

Observability:

• Structured logging with correlation ID
• Centralized exception logging
• Metrics exposed via Actuator
________________________________________

Testing Strategy:

Unit Tests
• Service layer business logic
• Validation and checksum logic
Controller Tests
• MockMvc-based API tests
• Positive and negative scenarios
Repository Tests
Quality Gates
• JaCoCo ≥ 85% coverage
• SonarLint: zero critical issues
________________________________________

Local Setup:

Prerequisites

• Java 17+
• Gradle 8+
Run Application
./gradlew bootRun
Swagger UI
http://localhost:8080/swagger-ui.html
H2 console
http://localhost:8080/h2-console
________________________________________

Configuration:

• File storage path
• Allowed MIME types
• Max file size
• JWT secret and expiry. 
All configurations are externalized using application.yml.
________________________________________

Build & Quality:

./gradlew clean build
./gradlew test
./gradlew jacocoTestReport
________________________________________

Release Process:

• Feature branches
• Clean, meaningful commits
• Release tag created in Git
• README and Swagger updated
__________________________________________

Ownership:

This project demonstrates enterprise engineering maturity, clean architecture practices, and strong quality discipline suitable for production systems and technical leadership evaluations.
