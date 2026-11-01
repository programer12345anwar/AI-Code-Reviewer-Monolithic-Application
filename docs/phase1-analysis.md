# Production readiness audit

## Architecture and execution flow

The project is a Java 17 / Spring Boot 3.3.2 monolith. Static HTML/JavaScript calls Spring MVC controllers, which delegate to services and Spring Data JPA repositories. MySQL is retained for local development; Render production uses PostgreSQL. Flyway manages schemas. Gemini is the only external service integration.

1. A user registers or signs in; the backend returns a short-lived JWT.
2. The frontend submits code with that bearer token.
3. The backend derives the owner ID from the verified token, creates the submission and first version, then requests analysis from Gemini.
4. History returns paginated metadata. Version, analysis, and compare operations verify ownership before reading or writing code.

## API inventory

| Method | Route | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/code/hello` | Public |
| POST | `/api/v1/code/upload` | JWT |
| POST | `/api/v1/code/analyze/{submissionId}` | JWT + owner |
| GET | `/api/v1/code/version/{submissionId}` | JWT + owner |
| POST | `/api/v1/code/version/{submissionId}/new` | JWT + owner |
| GET | `/api/v1/code/submissions/me?page=0&size=20` | JWT |
| POST | `/api/v1/code/compare` | JWT + owner |
| GET | `/actuator/health` | Public, status only |

Legacy `/api/code` aliases remain for code-review endpoints.

## Database and migrations

- Original schema: MySQL `BINARY(16)` UUIDs, `LONGTEXT` source/analysis, indexed user/submission IDs, unique `(submission_id, version_number)`.
- Local MySQL migration history is preserved; `V2__create_user_account.sql` adds the account table without deleting existing rows.
- Production PostgreSQL uses a separate fresh-install migration under `db/migration/postgresql`; it creates UUID, text, unique email, owner, and version constraints/indexes.
- Production uses Flyway and `ddl-auto=validate`, not schema auto-update. Hikari pool defaults to five connections.
- No production data was reported. Existing local MySQL data is not automatically transferred to Render PostgreSQL; do not point at or migrate any non-empty database without reviewing its contents and schema.

## External services and frontend

- Gemini API key is backend-only; outbound connect/read timeouts are five/30 seconds and output tokens are capped.
- Frontend uses a configured API origin in `frontend/api-config.js`; its value must be set to the actual Render origin after the service is created.
- CORS requires exact configured browser origins. The project is static HTML/JavaScript, not Vite; client Vite variables are not evaluated.
- GitHub integration was not found. File selection is browser-side; the backend accepts JSON source, not multipart uploads.

## Initial blockers found and remediation

| Priority | Initial finding | Remediation |
|---|---|---|
| Critical | Anonymous endpoints trusted caller-supplied local-storage user IDs; any caller could access records by ID. | Added register/login, BCrypt hashes, signed short-lived JWTs, authenticated owner-scoped service calls, and frontend sign-in/out. |
| High | Wildcard CORS and credentialed origin patterns. | Replaced duplicate policies with one allowlist from `CORS_ALLOWED_ORIGINS`; credentials disabled. |
| High | Frontend hardcoded localhost. | Centralized API origin in `api-config.js`; requires actual Render URL before Vercel redeploy. |
| High | Missing production profile/Render port support, development DB fallback credentials. | Added `prod` PostgreSQL profile, mandatory DB/JWT/CORS settings, `${PORT}` binding, Render Docker service definition, and `.env.example` placeholders. |
| High | MySQL-only schema for a Render PostgreSQL target. | Added PostgreSQL driver/Flyway support and separate production migration while preserving MySQL development migrations. |
| High | Unlimited source length; Gemini had no explicit timeout and provider bodies could be logged/returned. | Added a 256 KiB JSON-body cap, 50,000-character source validation, token cap, finite timeouts, safe generic provider errors, and reduced provider logging. |
| High | No abuse control and rate-limit map could grow indefinitely. | Added process-local route-sensitive limits, map cap, bounded AI/login rates, and preflight/health exclusions. |
| High | User filename was interpolated into HTML. | History now constructs DOM nodes and inserts untrusted filenames as text. |
| Medium | Missing resource failures mapped to 500; history unbounded. | Added 404 handling and paginated metadata-only history. |
| Medium | Hibernate SQL/bind logging and actuator metrics exposed. | Removed SQL/bind debug defaults and restricted actuator exposure to health/info. |
| Low | Java target documentation differed (17 vs. 21); docs were stale. | Aligned Docker/runtime guidance to Java 17 and refreshed architecture, deployment, and troubleshooting documentation. |

## Remaining deployment checks

- The exact Vercel production origin and actual Render database/service values must be supplied in Render/Vercel configuration; they are intentionally not invented or committed.
- The PostgreSQL migration has not been run against a live Render PostgreSQL instance from this workspace.
- Deployment, live Gemini behavior, production TLS/CORS, and Vercel integration remain unverified until deployed.
- The rate limiter is in-process; add edge-level protection before running multiple Render instances.
- Email verification, password reset, refresh tokens, and account deletion are not implemented.
