# AI Code Reviewer

A Java 17 / Spring Boot 3.3 monolith with a static HTML/JavaScript frontend. Users register or sign in, submit source code, and receive Gemini-powered analysis. Submission and version data is scoped to the authenticated account.

## Architecture

- Frontend: static files in `frontend/`
- Backend: Spring MVC, Spring Security (stateless JWT), Spring Data JPA
- Local development database: MySQL
- Production database: PostgreSQL with a separate Flyway migration
- AI provider: Google Gemini through a timeout-limited HTTP client

## Local development

1. Install Java 17 and Docker Desktop.
2. Copy `.env.example` to `.env` and set local MySQL details, a Base64-encoded JWT key (at least 32 random bytes), and `GOOGLE_API_KEY`.
3. Start the development database and backend:

   ```powershell
   docker compose up --build
   ```

4. Configure `frontend/api-config.js` to `http://localhost:9095` and serve `frontend/` through a local static web server. The local frontend origin must be included in `CORS_ALLOWED_ORIGINS`.
5. Open the frontend. Register using an email and a password of at least 12 characters.

The backend API listens on port 9095 locally. Health: `GET /actuator/health`. OpenAPI documentation is available outside the production profile at `/swagger-ui/index.html`.

## API endpoints

| Method | Path | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/code/hello` | Public |
| POST | `/api/v1/code/upload` | Authenticated |
| POST | `/api/v1/code/analyze/{submissionId}` | Owner |
| GET | `/api/v1/code/version/{submissionId}` | Owner |
| POST | `/api/v1/code/version/{submissionId}/new` | Owner |
| GET | `/api/v1/code/submissions/me?page=0&size=20` | Authenticated, paginated |
| POST | `/api/v1/code/compare` | Owner |
| GET | `/actuator/health` | Public, status only |

Protected API calls require `Authorization: Bearer <accessToken>`. Access tokens are short-lived; sign in again after expiration.

## Deploy backend to Render

JSON requests are capped at 256 KiB and source submissions at 50,000 characters.

1. Create a Render PostgreSQL database and a Docker Web Service from this repository. `render.yaml` provides the service definition and health check.
2. Set the environment variables listed in [docs/environment-variables.md](docs/environment-variables.md) in the Render service. Use the database's internal connection host when possible; construct `DB_URL` as a JDBC URL, for example `jdbc:postgresql://<internal-host>:5432/<database>`. For external connections, use the provider's SSL-required JDBC URL.
3. Set `SPRING_PROFILES_ACTIVE=prod`. The app uses the `postgresql` Flyway migration directory and validates the migrated schema; it does not use Hibernate schema updates.
4. Generate `JWT_SECRET` as Base64 of at least 32 cryptographically random bytes. Set the exact production Vercel origin in `CORS_ALLOWED_ORIGINS` (scheme and host only, no path or trailing slash).
5. Configure `GOOGLE_API_KEY` in Render. Secrets belong only in Render's environment settings.
6. Deploy and wait for Render's `/actuator/health` check. Confirm the service starts, Flyway completes, and registration/login work before routing production traffic.

No live Render or database connection has been verified from this repository workspace. The first production deployment runs the PostgreSQL Flyway migration and creates the schema. No existing production data was reported; do not point this service at a database containing data unless the schema and migration have been reviewed first.

## Deploy frontend to Vercel

The frontend is plain static HTML/JavaScript, not a Vite application, so Vite environment variables are not read at runtime. After the Render service is deployed, set `window.AI_CODE_REVIEWER_API_URL` in `frontend/api-config.js` to the actual HTTPS Render service URL (origin only), commit/push that configuration, and redeploy Vercel. Do not put database, JWT-signing, or Gemini credentials in frontend files or Vercel client-side variables.

## Tests and build

From `backend/`:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package -DskipTests
```

Tests use H2 and mocked Gemini responses. A live Render PostgreSQL migration, external Gemini call, and deployed Vercel integration require environment credentials and must be validated after deployment.
