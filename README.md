# URL Shortener Service

## Requirements
- Java 21
- PostgreSQL 18

## Installation

### Database Setup
1. Install PostgreSQL.
2. Create a database named `urlshortener` in pgAdmin.
3. Update `spring.datasource.url`, `spring.datasource.username` and `spring.datasource.password` in `application.properties` with your local PostgreSQL configuration.
> **For production**, use a cloud secret management service such as  `AWS Secrets Manager` or `Azure Key Vault` to store database credentials.
4. Run the application.

### Notes
- `Tomcat`, `Jackson`, `logback` versions has been overridden for security. Remove later when Spring Boot updates to safer versions. 

### References
[How to Setup PortgreSQL Connection](https://jdbc.postgresql.org/documentation/use/)