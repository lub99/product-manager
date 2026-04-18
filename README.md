# Product Manager REST API

A simple REST API for managing products, built with Spring Boot 3.

## Tech Stack

- Java 21
- Maven
- Spring Boot 3.4.x
- Hibernate ORM / Spring Data JPA
- PostgreSQL
- Flyway (database migrations)
- MapStruct (object mapping)
- Lombok

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

## Local Setup

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd product-manager
   ```

2. **Start PostgreSQL with Docker Compose**
   ```bash
   docker compose up -d
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **The API is available at** `http://localhost:8080`

## API Endpoints

| Method | URL                   | Description          |
|--------|-----------------------|----------------------|
| POST   | `/api/products`       | Create a new product |
| GET    | `/api/products/{id}`  | Get product by ID    |
| GET    | `/api/products`       | List all products    |

## Example Request

```http
POST /api/products
Content-Type: application/json

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

## Running Tests

```bash
mvn test
```

> Integration tests use Testcontainers and require Docker to be running.
