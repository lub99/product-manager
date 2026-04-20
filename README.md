# Product Manager REST API

A REST API for managing products, built with Spring Boot 3.

## Tech Stack

- Java 21
- Spring Boot 3.4.x
- Hibernate ORM / Spring Data JPA
- PostgreSQL + Flyway (migrations)
- MapStruct, Lombok
- Spring Security + JWT (OAuth2 Resource Server)
- SpringDoc OpenAPI (Swagger UI)
- Micrometer + InfluxDB + Grafana (metrics)
- Testcontainers (integration tests)

## Prerequisites

- Java 21+
- Docker & Docker Compose
- *(No system Maven needed — Maven Wrapper is included)*

## Local Setup

### 1. Clone the repository

```bash
git clone <repo-url>
cd product-manager
```

### 2. Configure local properties

Copy the template and customise as needed:

```bash
cp src/main/resources/application-local.template.yml \
   src/main/resources/application-local.yml
```

> `application-local.yml` is gitignored. The template contains safe defaults that match the Docker Compose configuration — no changes are required for a standard local setup.

### 3. Start infrastructure

**Minimal (PostgreSQL only):**

```bash
docker compose up postgres -d
```

**Full stack (PostgreSQL + InfluxDB + Grafana):**

```bash
docker compose up -d
```

### 4. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows, use `.\mvnw.cmd` where the README shows `./mvnw` (same wrapper; Bash/Git Bash can still use `./mvnw`).

The API is available at: **http://localhost:8080/product-manager**

---

### 5. Seed the admin user

The app has no automatic admin seeding. Connect to the database and run:

```sql
INSERT INTO users (email, password, role)
VALUES (
  'admin@example.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'ADMIN'
);
```

> The hash above is the BCrypt-encoded value of `admin123` (cost=10). Change both the email and password for any non-local environment.


## Authentication

All product endpoints require a JWT access token. The registration endpoint creates `USER`-role accounts (read-only). The `ADMIN` user seeded above can create products.

### Register (creates USER role)

```http
POST /product-manager/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "yourpassword"
}
```

### Login

```http
POST /product-manager/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

Response:
```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<uuid>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Use the token

Add to all subsequent requests:

```
Authorization: Bearer <accessToken>
```

---

## API Endpoints

| Method | URL | Role required | Description |
|--------|-----|---------------|-------------|
| POST | `/auth/register` | public | Register a new user |
| POST | `/auth/login` | public | Login and get tokens |
| POST | `/auth/refresh` | public | Refresh access token |
| POST | `/auth/logout` | public | Revoke refresh token |
| POST | `/product-manager/api/products` | ADMIN | Create a product |
| GET | `/product-manager/api/products/{id}` | authenticated | Get product by ID |
| GET | `/product-manager/api/products` | authenticated | List all products |

## Example Request

```http
POST /product-manager/api/products
Content-Type: application/json
Authorization: Bearer <accessToken>

{
  "code": "PROD000001",
  "name": "Widget",
  "priceEur": 9.99,
  "isAvailable": true
}
```

## Example Response

```json
{
  "id": 1,
  "code": "PROD000001",
  "name": "Widget",
  "priceEur": 9.99,
  "priceUsd": 11.79,
  "isAvailable": true
}
```

---

## Swagger UI

Interactive API explorer available at:

**http://localhost:8080/product-manager/swagger-ui/index.html**

> Swagger UI is available locally at the URL above.

---

## Running Tests

Unit tests only (fast):

```bash
./mvnw test
```

Unit + integration tests (Testcontainers — requires Docker):

```bash
./mvnw verify
```

---

## Monitoring (full stack only)

| Service | URL | Credentials |
|---------|-----|-------------|
| Actuator health | http://localhost:8081/actuator/health | — |
| Actuator metrics | http://localhost:8081/actuator/metrics | — |
| Grafana | http://localhost:3000 | admin / grafanapassword |
| InfluxDB | http://localhost:8086 | admin / adminpassword |
