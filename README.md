# AI Code Reviewer

A production-oriented monolithic Spring Boot application for submitting code, tracking versions, and receiving AI-powered code review feedback.

## Features
- Submit code and create initial versions
- Review latest version and compare versions
- REST API with versioned endpoints
- Validation and structured error handling
- OpenAPI documentation
- Docker and CI/CD scaffolding

## Tech stack
- Java 21
- Spring Boot 3.3
- Spring Data JPA
- MySQL
- Spring Security
- Flyway
- OpenAPI/Swagger
- Docker

## Getting started
1. Start MySQL and ensure a database named `aicodereview` exists.
2. Configure environment variables such as `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `GOOGLE_API_KEY`.
3. Run:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
4. Open the frontend files directly in a browser or serve them with a static server.

## API docs
- Swagger UI: http://localhost:9095/swagger-ui/index.html
- OpenAPI JSON: http://localhost:9095/v3/api-docs

## Docker
```bash
docker compose up --build
```
