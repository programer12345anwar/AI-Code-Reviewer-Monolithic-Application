# Deployment checklist

## Before deploying

- [ ] Confirm Java 17 and the backend test/package commands pass.
- [ ] Create a Render PostgreSQL database and review its connection/SSL guidance.
- [ ] Create a Render Docker Web Service from this repository using `render.yaml`.
- [ ] Add service environment values: `SPRING_PROFILES_ACTIVE`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, and `GOOGLE_API_KEY`.
- [ ] Set `DB_URL` to a JDBC PostgreSQL URL. Use the internal database host when service networking permits; use the provider's SSL-required URL for external connections.
- [ ] Generate a distinct Base64 JWT key from at least 32 cryptographically random bytes.
- [ ] Set `CORS_ALLOWED_ORIGINS` to the exact Vercel production origin(s), without a path or trailing slash.
- [ ] Never put database credentials, the JWT key, or Gemini key into frontend files, Git, or a Vercel client-side environment variable.

## Deploy and verify

- [ ] Wait for Flyway to apply `db/migration/postgresql/V1__init_schema.sql`.
- [ ] Confirm the Render health check `/actuator/health` returns healthy.
- [ ] Register a new account and sign in from the deployed frontend.
- [ ] Verify unauthenticated code endpoints return 401.
- [ ] Verify a second account cannot read or modify the first account's submission/version IDs.
- [ ] Submit a small source file and confirm analysis is saved and displayed.
- [ ] Confirm history paging and version comparison work.
- [ ] Confirm browser preflight succeeds for the configured Vercel origin and fails for an unrelated origin.
- [ ] Verify logs do not include source code, bearer tokens, API keys, or upstream response bodies.
- [ ] Configure the actual Render HTTPS origin in `frontend/api-config.js`, push, and redeploy Vercel.

## Operations

- [ ] Set database backups/retention appropriate to the Render plan.
- [ ] Monitor database connection usage; default pool size is five.
- [ ] Keep the service on one instance unless edge rate limiting is configured for the multi-instance deployment.
- [ ] Rotate `JWT_SECRET` deliberately; doing so invalidates all existing access tokens.
- [ ] Re-run tests and review Flyway changes before every schema deployment.
