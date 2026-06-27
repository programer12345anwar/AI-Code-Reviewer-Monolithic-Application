# Phase 1 – Project Analysis Report

## 1. Folder structure
- Root contains the frontend, backend, and project documentation.
- Backend is a standard Spring Boot Maven project with controllers, services, repositories, models, and configuration.
- Frontend is a static web application using plain HTML, CSS, and JavaScript.

## 2. Architecture
- The application follows a simple layered monolith pattern:
  - Controller layer for HTTP endpoints
  - Service layer for business logic
  - Repository layer for persistence
  - Model layer for JPA entities
- The current design is functional but tightly coupled and not yet production-ready.

## 3. Layer separation
- Separation exists at a basic level, but request and response models are directly coupled to persistence entities.
- Business logic is mixed with controller flow and persistence concerns.
- There is no explicit domain service boundary, DTO layer, or shared contract layer.

## 4. Design patterns used
- Basic Spring dependency injection
- Repository pattern through Spring Data JPA
- MVC pattern for the REST API
- Simple service orchestration for code submission and version analysis

## 5. Security implementation
- No authentication or authorization is implemented.
- CORS is permissive and allows all origins.
- API keys are stored directly in configuration and are not environment-driven.
- No CSRF protection, validation, or rate limiting is present.

## 6. Authentication flow
- No authentication flow exists.
- The frontend uses a client-generated user ID stored in local storage.
- The backend accepts submissions based on that user ID without any identity verification.

## 7. API structure
- Current endpoints are:
  - GET /api/code/hello
  - POST /api/code/upload
  - POST /api/code/analyze/{submissionId}
  - GET /api/code/version/{submissionId}
  - POST /api/code/version/{submissionId}/new
  - GET /api/code/submissions/{userId}
  - POST /api/code/compare
- The API is not versioned and does not expose consistent response objects.

## 8. Database schema
- The application uses JPA entities for submissions and versions.
- Tables are inferred via Hibernate DDL updates.
- Constraints and indexes are minimal.
- No explicit migrations or transactional boundaries are defined.

## 9. External integrations
- The application integrates with the Google Gemini API through a direct HTTP call in the AI service.
- The integration lacks timeout handling, retries, and a provider abstraction.

## 10. Weak points
- Hardcoded configuration values
- No validation layer
- No global exception handling
- No structured logging
- No tests beyond a context load test
- No API documentation
- No Docker or CI/CD setup

## 11. Performance bottlenecks
- The AI analysis call blocks the request path.
- The app lacks caching and pagination.
- The current repository access is simple but would not scale well without indexes and query optimization.

## 12. Security vulnerabilities
- Secrets are hardcoded in configuration
- Open CORS policy
- No authentication or authorization
- No input validation or sanitization
- No secure headers or rate limiting

## 13. Scalability issues
- The monolith is simple and suitable for initial use, but it needs stronger separation and operational controls before production deployment.
- Long-running AI calls could stall request threads.
- No thread pool tuning or async processing strategy is present.

## 14. Code smells
- Typoed repository class names
- Field injection instead of constructor injection
- Direct entity usage in controllers
- Magic strings such as pending analysis text
- Minimal exception handling
- Print-like or ad-hoc error strings instead of structured logging

## 15. Technical debt
- Configuration is not profile-driven
- Database schema is not managed through migrations
- Security and observability are missing
- No deployment automation exists

## 16. Recommended direction
- Introduce DTOs and consistent API responses
- Add validation and structured error handling
- Add Spring Security with JWT support while preserving existing anonymous submission flow
- Move configuration to environment-based YAML profiles
- Add basic observability, logging, and health endpoints
- Add tests, Docker, and CI/CD scaffolding
