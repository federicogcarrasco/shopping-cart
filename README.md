# Shopping Cart API

## Tech Stack

- Java 17
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- H2 (in-memory database)
- Lombok
- OpenAPI / Swagger UI

## Requirements

- Java 17+
- Maven 3.8+

## Setup

### 1 — Clone the repository

```bash
git clone https://github.com/federicogcarrasco/shopping-cart.git
```

### 2 — Run the application

```bash
cd shopping-cart
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## API Documentation

Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

H2 Console is available at:
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:shoppingcartdb`
- Username: `sa`
- Password: *(empty)*

## Seed Data

The database is pre-loaded with the following data:

**Users** — password for all users is `password`:

| Username | Role  |
|----------|-------|
| admin    | ADMIN |
| john     | USER  |
| jane     | USER  |

**Categories and discounts:**

| Category    | Discount |
|-------------|----------|
| Electronics | 10%      |
| Clothing    | 20%      |
| Food        | 5%       |
| Books       | 15%      |
| Sports      | 0%       |

**Products:**

| Name                       | Price    | Category    |
|----------------------------|----------|-------------|
| Laptop                     | $1500.00 | Electronics |
| Smartphone                 | $800.00  | Electronics |
| Headphones                 | $150.00  | Electronics |
| T-Shirt                    | $25.00   | Clothing    |
| Jeans                      | $60.00   | Clothing    |
| Jacket                     | $120.00  | Clothing    |
| Rice 1kg                   | $2.50    | Food        |
| Pasta 500g                 | $1.80    | Food        |
| Olive Oil                  | $8.00    | Food        |
| Clean Code                 | $35.00   | Books       |
| The Pragmatic Programmer   | $40.00   | Books       |
| Football                   | $30.00   | Sports      |
| Tennis Racket              | $85.00   | Sports      |

## Logs

Application logs are stored in the `logs/` directory, which is excluded from version control.
Each log file rotates daily and when it reaches 10MB, keeping up to 30 days of history.

## Metrics

The API exposes usage metrics via Spring Boot Actuator and Micrometer.

### Available endpoints

| Endpoint                        | Description                              |
|---------------------------------|------------------------------------------|
| `/actuator/health`              | Application health status                |
| `/actuator/metrics`             | List of all available metrics            |
| `/actuator/metrics/{metric}`    | Detail of a specific metric              |
| `/actuator/prometheus`          | All metrics in Prometheus format         |

### Querying metrics directly

You can query any metric in JSON format without any external tool:

```
GET http://localhost:8080/actuator/metrics/http.server.requests
```

Filter by tag (e.g. only POST requests to `/api/carts`):
```
GET http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/carts&tag=method:POST
```

### HTTP request metrics (auto-generated per endpoint)

| Metric | Description |
|--------|-------------|
| `http.server.requests` — `count` | Total number of calls |
| `http.server.requests` — `status=2xx` | Successful calls |
| `http.server.requests` — `status=4xx/5xx` | Failed calls |
| `http.server.requests` — `p50/p95/p99` | Response time percentiles |

### Custom business metrics

| Metric | Description |
|--------|-------------|
| `carts.created` | Number of carts created |
| `carts.items.added` | Number of items added to carts |
| `carts.items.removed` | Number of items removed from carts |
| `carts.orders.processing` | Number of orders sent to async processing |
| `carts.orders.processed` | Number of orders successfully processed |
| `carts.orders.failed` | Number of orders that failed during processing |
| `carts.orders.processing.time` | Time taken to process each order |
