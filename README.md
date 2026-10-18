# URL Shortener Service

## Requirements
- Java 21
- PostgreSQL 18

## Installation

### Project Setup
1. Create `application-local.properties` by copying `application-local.properties.example`.
2. Fill in the required values in `application-local.properties`, never push them to git.
4. Run the application.
5. Try sending requests using [Swagger UI](http://localhost:8080/swagger-ui.html) in your browser.
6. For endpoints that need authorization, call `/api/login` and copy `token` from response. Click **Authorize** (top right) and paste token without `Bearer ` prefix.

### Database Setup
1. Install PostgreSQL.
2. Create a database named `urlshortener` in your preferred PostgreSQL client such as pgAdmin, DBeaver
3. Use `database/schema.sql` to create tables in the database.

### Production Setup
- Configure deployment to use `URL_SHORTENER_*` environment variables from **AWS Secrets Manager**, which override values in `application-local.properties`.

### Notes
- `Tomcat`, `Jackson`, `logback` versions has been overridden for security. Remove later when Spring Boot updates to safer versions. 

### References
[How to Setup PortgreSQL Connection](https://jdbc.postgresql.org/documentation/use/)