# Microservices Learning Project

A Java / Spring Boot microservices application modelling a simple e-commerce backend: users, products, orders and payments, sitting behind an API Gateway, with centralized configuration, service discovery, and event publishing through Kafka.

## Architecture

```
                         ┌────────────────┐
        client ────────▶ │  API Gateway   │  :8082
                         └───────┬────────┘
        /users/**  /products/**  │  /orders/**  /payments/**
              ┌──────────┬───────┴───┬──────────────┐
              ▼          ▼           ▼              ▼
        user-service  product-    order-service  payment-service
           :8081      service        :8084           :8085
                       :8083            │
                                        ├── Feign (via Eureka) ──▶ user-service, product-service
                                        └── Kafka topic "order-created" ──▶ (consumer not yet implemented)

   config-server :8888  ── serves per-service config to all services
   Eureka        :8761  ── service discovery (all services register here)
   MySQL         :3306  ── one database per service
```

## Services

| Module | Port | Purpose | Database |
|---|---|---|---|
| `config-server` | 8888 | Spring Cloud Config Server (native profile); serves the files in `src/main/resources/config/` | – |
| `api-gateway` | 8082 | Spring Cloud Gateway (WebFlux); routes requests by path prefix | – |
| `user-service` | 8081 | CRUD for users | `user_db` |
| `product-service` | 8083 | CRUD for products | `product_service_db` |
| `order-service` | 8084 | Creates and reads orders; validates user and product via Feign; publishes `order-created` event to Kafka | `order_service_db` |
| `payment-service` | 8085 | Creates and reads payments | `payment_service_db` |
| `discovery-server` | 8761 | Eureka server (see [Known gaps](#known-gaps)) | – |

## Tech stack

- Java 17
- Spring Boot 4.0.7, Spring Cloud 2025.1.2
- Spring Cloud: Gateway, Config, Netflix Eureka client, OpenFeign, LoadBalancer
- Spring Data JPA / Hibernate, MySQL (`mysql-connector-j`)
- Apache Kafka (`spring-kafka`)
- Lombok, Maven (wrapper included in each module)

## Prerequisites

- JDK 17+
- MySQL running on `localhost:3306`
- Kafka broker on `localhost:9092` (needed by `order-service`)
- A Eureka server on `localhost:8761`

Create the databases before starting the services:

```sql
CREATE DATABASE user_db;
CREATE DATABASE product_service_db;
CREATE DATABASE order_service_db;
CREATE DATABASE payment_service_db;
```

Tables are created automatically (`spring.jpa.hibernate.ddl-auto=update`).

## Configuration

Each business service has a minimal `application.properties` that only sets its name and imports config from the config server:

```properties
spring.config.import=optional:configserver:http://localhost:8888
```

The real settings (port, datasource, Eureka URL, Kafka) live in `config-server/src/main/resources/config/<service-name>.properties`. Update the MySQL username and password there to match your environment.

## Running locally

Each module is an independent Maven project. Start them in this order, each in its own terminal:

```bash
# 1. Service discovery (Eureka) on :8761
# 2. Config server
cd config-server  && ./mvnw spring-boot:run

# 3. Business services (any order)
cd user-service    && ./mvnw spring-boot:run
cd product-service && ./mvnw spring-boot:run
cd order-service   && ./mvnw spring-boot:run
cd payment-service && ./mvnw spring-boot:run

# 4. API gateway
cd api-gateway && ./mvnw spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

The config server must be up before the business services, otherwise they fall back to default settings (the config import is `optional:`) and will not pick up their port or database.

## API

All requests go through the gateway at `http://localhost:8082`.

| Resource | Endpoints |
|---|---|
| Users | `POST /users`, `GET /users`, `GET /users/{id}`, `PUT /users/{id}`, `DELETE /users/{id}` |
| Products | `POST /products`, `GET /products`, `GET /products/{id}`, `PUT /products/{id}`, `DELETE /products/{id}` |
| Orders | `POST /orders`, `GET /orders`, `GET /orders/{id}` |
| Payments | `POST /payments`, `GET /payments`, `GET /payments/{id}` |

### Example flow

```bash
# Create a user
curl -X POST http://localhost:8082/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Ali Khan","email":"ali@example.com","phone":"03001234567"}'

# Create a product
curl -X POST http://localhost:8082/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Keyboard","price":2500.0,"description":"Mechanical keyboard"}'

# Place an order (user 1 buys 2 of product 1)
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":2}'

# Record a payment for the order
curl -X POST http://localhost:8082/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId":1,"userId":1,"amount":5000.0,"paymentMethod":"CARD"}'
```

### How order creation works

1. `order-service` calls `user-service` (`GET /users/{id}`) and `product-service` (`GET /products/{id}`) through Feign clients, resolved by name via Eureka.
2. It calculates `totalPrice = product.price × quantity`.
3. The order is saved with status `PAYMENT_PENDING` (unless a status is supplied).
4. An `OrderCreatedEvent` (`orderId`, `userId`, `productId`, `quantity`, `totalPrice`) is published to the Kafka topic `order-created`, keyed by order id.

Order statuses: `CREATED`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `PAYMENT_PENDING`.

A payment defaults to status `SUCCESS` when none is provided.

## Project structure

```
microservice-main/
├── api-gateway/        # routes /users, /products, /orders, /payments
├── config-server/      # centralized config (src/main/resources/config/*.properties)
├── discovery-server/   # Eureka server (empty in this repo)
├── user-service/       # controller / service / repository / entity / dto
├── product-service/
├── order-service/      # Feign clients + Kafka producer
└── payment-service/
```

## Known gaps

- **`discovery-server` is empty.** Every service expects Eureka at `localhost:8761`. Add a Spring Boot app with `spring-cloud-starter-netflix-eureka-server` and `@EnableEurekaServer`, or run one separately.
- **No Kafka consumer yet.** `order-service` publishes `order-created`, but nothing consumes it, so payments are not triggered automatically and order status is not updated after payment.
- **Gateway routes are hard-coded to `localhost` ports** rather than using Eureka (`lb://`) service names.
- **Hard-coded Kafka address.** `KafkaProducerConfig` uses `localhost:9092` directly and ignores `spring.kafka.*` in the config server.
- **Credentials are committed.** The database password is stored in plain text in the config-server property files. Move it to environment variables or a secrets store before sharing the repo.
- **Error handling is basic.** Missing records throw a plain `RuntimeException` (HTTP 500) instead of a proper 404, and there is no request validation.
- **No authentication** on any endpoint.

## Possible next steps

- Add the Eureka server and a Kafka consumer in `payment-service`
- Use `lb://` routes in the gateway
- Add Docker Compose for MySQL, Kafka, and all services
- Add global exception handling, validation, and JWT security
- Add resilience (Resilience4j circuit breaker) for the Feign calls
