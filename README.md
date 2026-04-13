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
cd shoppingcart
```

### 2 — Run the application

```bash
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
