# Architecture Overview

```mermaid
flowchart LR
    User[Browser / Frontend] -->|HTTP| Controller[Spring MVC Controller]
    Controller --> Service[Application Services]
    Service --> Repo[Spring Data JPA Repositories]
    Repo --> DB[(MySQL)]
    Service --> AI[Gemini AI Provider]
    Controller --> Docs[Swagger UI]
```

## Responsibilities
- Controllers expose the REST API and map DTOs to service calls.
- Services implement business logic, version creation, and AI analysis orchestration.
- Repositories store submissions and version history.
- Configuration and filters handle security, logging, validation, and rate limiting.
