# Troubleshooting

## Build issues
- Ensure Java 21 is installed and active.
- Run `./mvnw test` from the backend directory.

## Database issues
- Verify MySQL is running and the configured database exists.
- Check `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

## AI analysis issues
- Confirm `GOOGLE_API_KEY` is set.
- If the provider returns an error, review the backend logs.
