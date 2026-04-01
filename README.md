# Bank Management System

A comprehensive microservices-based banking system built with Spring Boot. This project demonstrates enterprise-grade banking operations including customer management, account handling, transactions, loans, credit cards, and notifications.

## Architecture Overview

This system follows a microservices architecture pattern with the following components:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BANK MANAGEMENT SYSTEM                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────┐                                                            │
│  │ API Gateway │  ← Single entry point for all client requests             │
│  └──────┬──────┘                                                            │
│         │                                                                   │
│  ┌──────▼─────────────────────────────────────────────────────────────┐    │
│  │                    Eureka Service Registry                         │    │
│  │              (Service Discovery & Load Balancing)                  │    │
│  └──────┬─────────┬─────────┬─────────┬─────────┬─────────┬──────────┘    │
│         │         │         │         │         │         │                │
│  ┌──────▼──┐ ┌───▼────┐ ┌──▼─────┐ ┌──▼──────┐ ┌──▼────────┐ ┌──▼───────┐ │
│  │ Customer│ │ Account│ │Bank    │ │ Loan   │ │Credit Card│ │Transaction│ │
│  │ Service │ │ Service│ │Service │ │Service │ │  Service  │ │  Service  │ │
│  │  :8080  │ │ :8081  │ │ :8082  │ │ :8083  │ │  :8084    │ │  :8085    │ │
│  └────┬────┘ └────┬───┘ └───┬────┘ └───┬────┘ └─────┬─────┘ └─────┬─────┘ │
│       │           │         │          │            │             │       │
│       └───────────┴─────────┴──────────┴────────────┴─────────────┘       │
│                           │                                               │
│                    ┌──────▼──────┐                                      │
│                    │Notification │                                      │
│                    │  Service    │                                      │
│                    └─────────────┘                                      │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                         PostgreSQL Database                         │  │
│  │  (customer_db, account_db, loan_db, card_db, transaction_db, etc.) │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Technology Stack

| Category | Technology |
|----------|------------|
| **Backend Framework** | Spring Boot 3.2.5 |
| **Cloud Platform** | Spring Cloud 2023.0.1 |
| **Service Discovery** | Netflix Eureka Server |
| **API Gateway** | Spring Cloud Gateway |
| **Inter-service Communication** | OpenFeign |
| **Database** | PostgreSQL |
| **ORM** | Spring Data JPA with Hibernate |
| **Build Tool** | Maven 3.x |
| **Java Version** | Java 17+ |
| **Utilities** | Lombok, ModelMapper, Validation API |

## Microservices Overview

| Service | Port | Description | Database |
|---------|------|-------------|----------|
| **Eureka Registry** | 8761 | Service discovery server | - |
| **API Gateway** | 8080/8081 | Entry point, routing, filtering | - |
| **Customer Service** | 8080 | Customer CRUD operations | customer_db |
| **Account Service** | 8081 | Bank account management | account_db |
| **Bank Service** | 8082 | Core banking operations | bank_db |
| **Loan Service** | 8083 | Loan applications & management | loan_db |
| **Credit Card Service** | 8084 | Credit card operations | card_db |
| **Transaction Service** | 8085 | Money transfers & transactions | transaction_db |
| **Notification Service** | 8086 | Email/SMS notifications | notification_db |
| **Common Lib** | - | Shared DTOs, entities, utilities | - |

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 12+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Project Structure

```
BankMgmtSystem/
├── pom.xml                    # Parent POM
├── README.md                  # This file
│
├── eureka-registry/           # Service Discovery
│   └── src/
│
├── api-gateway/               # API Gateway
│   └── src/
│
├── common-lib/                # Shared library
│   └── src/
│
├── customer-service/          # Customer Management
│   └── src/
│
├── account-service/           # Account Management
│   └── src/
│
├── bank-service/              # Core Banking
│   └── src/
│
├── loan-service/              # Loan Management
│   └── src/
│
├── credit-card-service/       # Credit Card Operations
│   └── src/
│
├── transaction-service/       # Transaction Processing
│   └── src/
│
└── notification-service/      # Notification System
    └── src/
```

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd BankMgmtSystem
```

### 2. Database Setup

Create the required PostgreSQL databases:

```sql
CREATE DATABASE customer_db;
CREATE DATABASE account_db;
CREATE DATABASE bank_db;
CREATE DATABASE loan_db;
CREATE DATABASE card_db;
CREATE DATABASE transaction_db;
CREATE DATABASE notification_db;
```

### 3. Environment Configuration

Set the following environment variables (or configure in `application.properties`):

```bash
# Database Credentials
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

Or on Windows:
```cmd
set DB_USERNAME=your_username
set DB_PASSWORD=your_password
```

### 4. Build the Project

```bash
# Build all modules
mvn clean install

# Skip tests (optional)
mvn clean install -DskipTests
```

### 5. Start Services (In Order)

**Step 1: Start Eureka Registry**
```bash
cd eureka-registry
mvn spring-boot:run
```
Access at: http://localhost:8761

**Step 2: Start API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
```

**Step 3: Start Core Services**
```bash
# Terminal 1 - Customer Service
cd customer-service
mvn spring-boot:run

# Terminal 2 - Account Service
cd account-service
mvn spring-boot:run

# Terminal 3 - Transaction Service
cd transaction-service
mvn spring-boot:run

# Terminal 4 - Loan Service
cd loan-service
mvn spring-boot:run

# Terminal 5 - Credit Card Service
cd credit-card-service
mvn spring-boot:run

# Terminal 6 - Bank Service
cd bank-service
mvn spring-boot:run

# Terminal 7 - Notification Service
cd notification-service
mvn spring-boot:run
```

## API Endpoints

### Customer Service (Port 8080)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/customers/find-by-id?customerId={id}` | Get customer by ID |
| GET | `/api/v1/customers/find-by-email?email={email}` | Get customer by email |
| GET | `/api/v1/customers/find-all-customers?pageNumber=0&pageSize=10` | List all customers (paginated) |
| POST | `/api/v1/customers/create-customer` | Create new customer |
| POST | `/api/v1/customers/update-customer` | Update customer |
| DELETE | `/api/v1/customers/delete-customer?customerId={id}` | Delete customer |

### Sample Request - Create Customer

```json
POST /api/v1/customers/create-customer
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "mobileNumber": "+1234567890",
  "address": "123 Main St, City",
  "dateOfBirth": "1990-01-01"
}
```

## Service Communication

Services communicate using **OpenFeign** clients:

```java
@FeignClient(name = "customer-service", url = "${customer.service.url}")
public interface CustomerClient {
    @GetMapping("/api/v1/customers/find-by-id")
    CustomerDTO getCustomer(@RequestParam UUID customerId);
}
```

## Key Features

- **Microservices Architecture**: Independent deployable services
- **Service Discovery**: Automatic registration with Eureka
- **API Gateway**: Single entry point with routing
- **Database Per Service**: Each service has its own PostgreSQL database
- **DTO Pattern**: Data Transfer Objects for clean API contracts
- **Validation**: Input validation using Bean Validation API
- **Logging**: SLF4J with structured logging
- **Pagination**: Standardized paginated responses

## Monitoring & Discovery

- **Eureka Dashboard**: http://localhost:8761
- View all registered services and their health status

## Future Enhancements

- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] JWT-based authentication & authorization
- [ ] Circuit breaker (Resilience4j)
- [ ] Distributed tracing (Sleuth + Zipkin)
- [ ] Message queue integration (RabbitMQ/Kafka)
- [ ] Redis caching
- [ ] Swagger/OpenAPI documentation
- [ ] Unit & integration tests

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit changes (`git commit -am 'Add new feature'`)
4. Push to branch (`git push origin feature/your-feature`)
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please contact the development team.

---

**Built with using Spring Boot & Spring Cloud**