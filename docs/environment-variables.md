# Environment variables

Configure these values in Render's backend service. Do not commit credentials.

| Variable | Required | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Yes | Set to `prod` on Render; selects PostgreSQL configuration and migrations. |
| `DB_URL` | Yes | JDBC PostgreSQL URL, e.g. `jdbc:postgresql://<host>:5432/<database>`. Use the database provider's SSL-required URL for external connections. |
| `DB_USERNAME` | Yes | PostgreSQL account username. |
| `DB_PASSWORD` | Yes | PostgreSQL account password. |
| `JWT_SECRET` | Yes | Base64-encoded cryptographically random key of at least 32 bytes. |
| `CORS_ALLOWED_ORIGINS` | Yes | Comma-separated exact browser origins, e.g. `https://<your-vercel-domain>`. Do not include a path or trailing slash. |
| `GOOGLE_API_KEY` | Yes for analysis | Gemini API credential; stored only in backend environment. |
| `PORT` | Render-managed | Spring Boot binds to Render's assigned port automatically. Do not hardcode a production port. |
| `DB_POOL_MAX_SIZE` | No | Hikari maximum connections; defaults to `5`. Keep within the database plan's connection limit. |
| `SERVER_TOMCAT_MAX_THREADS` | No | Maximum request threads; defaults to `50` for the production profile. |
| `JWT_EXPIRATION_MS` | No | Access-token lifetime in milliseconds; defaults to one hour. |
| `GEMINI_MODEL` | No | Gemini model name; defaults to the current application setting. |
| `GEMINI_BASE_URL` | No | Gemini API base URL override. |
| `GEMINI_ENDPOINT` | No | Full Gemini endpoint override, if needed. |
| `SERVER_PORT` | Local only | Local server port; defaults to `9095`. `PORT` takes precedence. |
| `APP_PROFILE` | Local compatibility | Legacy profile selector; prefer `SPRING_PROFILES_ACTIVE`. |

Generate a signing key locally without printing it to logs or source control:

```powershell
([Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)))
```

Use the generated output as the Render `JWT_SECRET` value. Generate distinct keys for local, test, and production environments.
