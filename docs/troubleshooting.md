# Troubleshooting

## Render service does not start

- Confirm `SPRING_PROFILES_ACTIVE=prod`.
- Check the startup log for unresolved `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, or `CORS_ALLOWED_ORIGINS`.
- Verify `DB_URL` starts with `jdbc:postgresql://` and points to a reachable PostgreSQL database.
- Review Flyway startup errors before retrying. Do not enable Hibernate `ddl-auto=update` to work around a migration failure.
- Confirm Render health check uses `/actuator/health`.

## Browser reports a CORS error

- Set `CORS_ALLOWED_ORIGINS` to the exact Vercel origin including `https://`, without a path or trailing slash.
- Redeploy the backend after changing the value.
- The static frontend reads its backend URL from `frontend/api-config.js`; Vite-specific environment variables are not available in this repository's plain HTML/JavaScript setup.

## Authentication fails

- Register an account or sign in through the frontend.
- Confirm requests to protected endpoints contain the `Authorization: Bearer ...` header.
- Access tokens expire; sign in again to get a new token.
- Use a Base64-encoded `JWT_SECRET` containing at least 32 random bytes. Changing the signing secret invalidates existing tokens.

## AI analysis fails

- Confirm `GOOGLE_API_KEY` and `GEMINI_MODEL` are valid for the configured provider account.
- The API returns a generic provider error; inspect status-only backend logs and provider console for details.
- Requests are limited to 50,000 source characters and have explicit connect/read timeouts.

## Local development

- Start MySQL and verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- The default development profile uses port 9095. Configure the local frontend origin in `CORS_ALLOWED_ORIGINS`.
- Run `.\mvnw.cmd clean test` from the backend directory.
