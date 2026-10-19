# URL Shortener Service

**A service for creating short links from long URLs.** It can be used in both guest and logged-in mode. With login, authenticated using JWT, users can save their short links, get the list of short links they created, and delete unused short links.

## Requirements
- Java 21
- PostgreSQL 18

## Setup and Run

### 1. Database
1. Install PostgreSQL.
2. Create a database named `urlshortener` in your preferred PostgreSQL client, such as pgAdmin, DBeaver.
3. Use `database/schema.sql` to create the tables.

### 2. Configuration
1. Create `application-local.properties` by copying `application-local.properties.example`.
2. Fill in the required values in `application-local.properties`, never push them to git.

### 3. Run
Run the application from your IDE, or:
```bash
./mvnw spring-boot:run
```

The app starts at `http://localhost:8080`.

### 4. Try the API
1. Open [Swagger UI](http://localhost:8080/swagger-ui.html) in your browser.
2. For endpoints that need login, call `/api/login` and copy `token` from response. Click **Authorize** (top right) and paste token without `Bearer ` prefix.
3. To test a redirect, paste the short URL in a real browser address bar.

### Run Tests
```bash
./mvnw test
```

## API Endpoints
| Method | Path | Login | Description |
|---|---|---|---|
| POST | `/api/register` | No | Create an account |
| POST | `/api/login` | No | Get a JWT token |
| POST | `/api/shorten` | Optional | Shorten a URL (saved to your account if logged in) |
| GET | `/{code}` | No | Redirect to the original URL |
| GET | `/api/urls` | Yes | List your short URLs, newest first |
| DELETE | `/api/urls/{id}` | Yes | Delete one of your short URLs |

## Example API Request

Import `postman/url-shortener.postman_collection.json` into Postman.

## Production Setup
- Configure deployment to use `URL_SHORTENER_*` environment variables from **AWS Secrets Manager**, which override values in `application-local.properties`.
- Set `springdoc.swagger-ui.enabled` and `springdoc.api-docs.enabled` in `application.properties` to `false`, so the API docs are not public.

## Notes
- `Tomcat`, `Jackson`, `logback` versions has been overridden for security. Remove later when Spring Boot updates to safer versions. 

## References
- [How to Setup PortgreSQL Connection](https://jdbc.postgresql.org/documentation/use/)
- [Design Document](https://nalinsue.atlassian.net/wiki/external/NmE0Njk5OWZhOWI1NDY5ZjhmZGNlNTdhN2ZiYzE2OGE)