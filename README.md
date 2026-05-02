# 🏦 Bank Management System

A comprehensive **microservices-based banking system** built with Spring Boot & Spring Cloud. This project demonstrates enterprise-grade banking operations including customer management, account handling, transactions, loans, credit cards, and event-driven notifications.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          BANK MANAGEMENT SYSTEM                              │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────┐                                                       │
│  │   Eureka Server   │  ← Service Discovery & Load Balancing (:8761)        │
│  └────────┬─────────┘                                                       │
│           │                                                                  │
│  ┌────────▼──────────────────────────────────────────────────────────────┐  │
│  │                        Microservices Layer                             │  │
│  │                                                                       │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐ │  │
│  │  │ Customer │ │ Account  │ │   Loan   │ │  Credit  │ │Transaction │ │  │
│  │  │ Service  │ │ Service  │ │ Service  │ │   Card   │ │  Service   │ │  │
│  │  │  :8080   │ │  :8081   │ │  :8084   │ │ Service  │ │   :8083    │ │  │
│  │  │          │ │          │ │          │ │  :8085   │ │            │ │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └────────────┘ │  │
│  └───────────────────────────┬───────────────────────────────────────────┘  │
│                              │                                               │
│                     ┌────────▼────────┐                                     │
│                     │  Apache Kafka   │  ← Event-Driven Messaging           │
│                     └────────┬────────┘                                     │
│                              │                                               │
│                     ┌────────▼────────┐                                     │
│                     │  Notification   │  ← Kafka Consumer (:8082)           │
│                     │    Service      │                                      │
│                     └─────────────────┘                                     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                       PostgreSQL Databases                            │  │
│  │  customer_db │ account_db │ loan_db │ credit_card_db │ transaction_db │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                     common-lib (Shared Library)                        │  │
│  │         DTOs • Events • Exceptions • Kafka Constants • ENUMs          │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Category | Technology |
|----------|------------|
| **Backend Framework** | Spring Boot 3.x |
| **Cloud Platform** | Spring Cloud 2023.x |
| **Service Discovery** | Netflix Eureka Server |
| **Inter-service Communication** | OpenFeign |
| **Messaging** | Apache Kafka |
| **Database** | PostgreSQL |
| **ORM** | Spring Data JPA / Hibernate |
| **Build Tool** | Maven (Multi-module) |
| **Java Version** | Java 17+ (Virtual Threads enabled) |
| **Utilities** | Lombok, ModelMapper, Bean Validation |

---

## Microservices Overview

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| **Eureka Registry** | 8761 | — | Service discovery server |
| **Customer Service** | 8080 | `customer_db` | Customer CRUD & lookup |
| **Account Service** | 8081 | `account_db` | Account management, deposits, withdrawals, transfers |
| **Notification Service** | 8082 | `notification_db` | Kafka consumer — persists & sends notifications |
| **Transaction Service** | 8083 | `transaction_db` | Kafka consumer — records all financial transactions |
| **Loan Service** | 8084 | `loan_db` | Loan lifecycle — apply, approve, reject, disburse |
| **Credit Card Service** | 8085 | `credit_card_db` | Credit card lifecycle — apply, charge, payment, block |
| **Bank Service** | — | — | Core banking operations (scaffold) |
| **Common Lib** | — | — | Shared DTOs, events, exceptions, Kafka constants |

---

## Service Details

### 1. Customer Service `:8080`

Manages customer profiles and identity information.

**Base Path:** `/api/v1/customers`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/create-customer` | Create a new customer |
| `POST` | `/update-customer` | Update customer details |
| `GET` | `/find-by-id?customerId={id}` | Get customer by UUID |
| `GET` | `/find-by-email?email={email}` | Get customer by email |
| `GET` | `/find-all-customers?pageNumber=0&pageSize=10` | List all customers (paginated) |
| `DELETE` | `/delete-customer?customerId={id}` | Delete a customer |

**Sample Request — Create Customer:**
```json
POST /api/v1/customers/create-customer
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "mobileNumber": "+1234567890",
  "address": "123 Main St, City",
  "dateOfBirth": "1990-01-01"
}
```

---

### 2. Account Service `:8081`

Manages bank accounts — creation, balance inquiries, deposits, withdrawals, and inter-account transfers. Supports idempotent financial operations via `Idempotency-key` headers.

**Base Path:** `/api/v1/accounts`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/create-account` | Create a new bank account |
| `GET` | `/get-account-by-account-number?accountNumber={num}` | Get account by number |
| `GET` | `/get-account-by-Id?accountId={id}` | Get account by UUID |
| `GET` | `/get-all-accounts-by-customer-id?customerId={id}` | List customer's accounts (paginated) |
| `GET` | `/get-balance?accountNumber={num}` | Get account balance |
| `PUT` | `/update-status?accountNumber={num}&status={status}` | Update account status |
| `PUT` | `/{accountNumber}/depositCredit?amount={amt}` | Deposit / Credit funds |
| `PUT` | `/{accountNumber}/withdrawDebit?amount={amt}` | Withdraw / Debit funds |
| `PUT` | `/transfer-amount` | Transfer between accounts |
| `DELETE` | `/delete-account-by-Id?accountId={id}` | Delete an account |

> **Note:** Deposit, withdraw, and transfer endpoints require an `Idempotency-key` request header to prevent duplicate transactions.

**Publishes Kafka Events:**
- `account-creation-topic` — on new account creation
- `transaction-notification-topic` — on deposit/withdrawal/transfer
- `transaction-topic` — transaction records for the Transaction Service

---

### 3. Loan Service `:8084`

Manages the full loan lifecycle with an event-driven notification pipeline.

**Base Path:** `/api/v1/loans`

**Loan State Machine:**
```
PENDING ──→ APPROVED ──→ ACTIVE (disbursed)
   │
   └──→ REJECTED
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/apply` | Submit a new loan application |
| `PUT` | `/{loanId}/approve` | Approve a pending loan (EMI calculated here) |
| `PUT` | `/{loanId}/reject` | Reject a pending loan |
| `PUT` | `/{loanId}/disburse` | Disburse an approved loan (credits customer account) |
| `GET` | `/{loanId}` | Get loan details |

**Key Business Rules:**
- EMI is calculated at approval time using the reducing-balance formula
- Disbursement credits the loan amount to the customer's linked account via Account Service (Feign)
- Uses `loanId` as the idempotency key to prevent double-crediting on retries
- If the Feign deposit call fails, the loan stays in APPROVED (not incorrectly marked ACTIVE)

**Publishes Kafka Events:**
- `loan-application-topic` — on new application
- `loan-status-topic` — on approval/rejection
- `loan-disbursement-topic` — on disbursement

---

### 4. Credit Card Service `:8085`

Manages the full credit card lifecycle — application, approval, activation, charges, payments, blocking, and closure.

**Base Path:** `/api/v1/credit-cards`

**Card State Machine:**
```
PENDING ──→ APPROVED ──→ ACTIVE ──→ BLOCKED ──→ ACTIVE (unblock)
   │                        │          │
   └──→ REJECTED            └──→ CLOSED └──→ CLOSED
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/apply` | Apply for a new credit card |
| `PUT` | `/{cardId}/approve` | Approve a pending application |
| `PUT` | `/{cardId}/reject` | Reject a pending application |
| `PUT` | `/{cardId}/activate` | Activate an approved card |
| `PUT` | `/{cardId}/block` | Block an active card |
| `PUT` | `/{cardId}/unblock` | Unblock a blocked card |
| `PUT` | `/{cardId}/close` | Close a card permanently |
| `POST` | `/{cardId}/charge` | Make a purchase/charge |
| `POST` | `/{cardId}/payment` | Make a payment toward balance |
| `GET` | `/{cardId}` | Get card details |
| `GET` | `/customer/{customerId}` | Get all cards for a customer |

**Key Business Rules:**
- **Charge** reduces `availableLimit` and increases `outstandingBalance` — blocked if exceeds limit
- **Payment** debits the linked bank account first (via Account Service Feign), then updates card balances
- **Close** only allowed when `outstandingBalance == 0`
- **Payments on blocked cards** are allowed (customer should still be able to pay)
- Default credit limit, APR, and annual fee are configurable via `application.properties`

**Publishes Kafka Events:**
- `credit-card-application-topic` — on new application
- `credit-card-status-topic` — on status changes (approve/reject/block/unblock/close)
- `credit-card-transaction-topic` — on charges and payments

---

### 5. Transaction Service `:8083`

A **Kafka consumer** that listens for financial transaction events from Account Service and persists them as auditable transaction records.

**Kafka Listeners:**
| Topic | Description |
|-------|-------------|
| `transaction-topic` | Records deposit/withdrawal transactions |
| `transaction-payment-topic` | Records transfer/payment transactions |

> This service has no REST endpoints — it operates purely as an event consumer.

---

### 6. Notification Service `:8082`

A **Kafka consumer** that listens for events across all services and persists notifications for customer alerts.

**Kafka Listeners:**
| Topic | Event Source | Description |
|-------|-------------|-------------|
| `account-creation-topic` | Account Service | New account created |
| `transaction-notification-topic` | Account Service | Deposit/withdrawal/transfer completed |
| `loan-application-topic` | Loan Service | New loan application submitted |
| `loan-status-topic` | Loan Service | Loan approved/rejected |
| `loan-disbursement-topic` | Loan Service | Loan disbursed |

> Uses manual Kafka acknowledgment — failed messages are retried automatically.

---

### 7. Common Library (`common-lib`)

A shared Maven module providing cross-cutting concerns to all services.

| Package | Contents |
|---------|----------|
| `com.bank.config` | `KafkaConstants` — centralized topic/group names |
| `com.bank.dto` | `CustomerDTO` — shared customer data contract |
| `com.bank.event` | Kafka event POJOs (Account, Loan, Credit Card, Transaction) |
| `com.bank.exception` | `ResourceNotFoundException`, `BadRequestException`, `DuplicateResourceException` |
| `com.bank.handler` | `GlobalException` — centralized exception handling |
| `com.bank.ENUM` | `TransactionType`, `TransactionStatus` |

---

## Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `account-creation-topic` | Account Service | Notification Service | New account alerts |
| `transaction-notification-topic` | Account Service | Notification Service | Transaction alerts |
| `transaction-topic` | Account Service | Transaction Service | Transaction records |
| `transaction-payment-topic` | Account Service | Transaction Service | Payment records |
| `loan-application-topic` | Loan Service | Notification Service | Loan application alerts |
| `loan-status-topic` | Loan Service | Notification Service | Loan status change alerts |
| `loan-disbursement-topic` | Loan Service | Notification Service | Loan disbursement alerts |
| `credit-card-application-topic` | Credit Card Service | Notification Service | Card application alerts |
| `credit-card-status-topic` | Credit Card Service | Notification Service | Card status change alerts |
| `credit-card-transaction-topic` | Credit Card Service | Notification Service | Card charge/payment alerts |

---

## Inter-Service Communication

```
┌──────────┐  Feign   ┌──────────┐  Feign   ┌──────────┐
│ Loan     │────────→│ Customer │←────────│  Credit  │
│ Service  │         │ Service  │         │   Card   │
│          │────┐    └──────────┘    ┌────│ Service  │
└──────────┘    │                    │    └──────────┘
                │    ┌──────────┐    │
                └───→│ Account  │←───┘
                     │ Service  │
                     └──────────┘
```

- **Loan Service → Customer Service**: Validate customer exists
- **Loan Service → Account Service**: Validate account ownership, credit loan amount on disbursement
- **Credit Card Service → Customer Service**: Validate customer exists
- **Credit Card Service → Account Service**: Validate account ownership, debit payment from bank account

---

## Project Structure

```
BankMgmtSystem/
├── pom.xml                         # Parent POM (multi-module)
├── README.md
├── common-lib/                     # Shared library
│   └── src/main/java/com/bank/
│       ├── config/                 # KafkaConstants
│       ├── dto/                    # Shared DTOs
│       ├── event/                  # Kafka event POJOs
│       ├── exception/              # Custom exceptions
│       ├── handler/                # Global exception handler
│       └── ENUM/                   # Shared enums
├── eureka-registry/                # Service Discovery (:8761)
├── customer-service/               # Customer CRUD (:8080)
├── account-service/                # Account Management (:8081)
├── notification-service/           # Kafka Consumer — Notifications (:8082)
├── transaction-service/            # Kafka Consumer — Transaction Records (:8083)
├── loan-service/                   # Loan Lifecycle (:8084)
├── credit-card-service/            # Credit Card Lifecycle (:8085)
└── bank-service/                   # Core Banking (scaffold)
```

---

## Prerequisites

- **Java** 17 or higher
- **Maven** 3.8+
- **PostgreSQL** 12+
- **Apache Kafka** (with Zookeeper or KRaft)
- **IDE** — IntelliJ IDEA, Eclipse, or VS Code

---

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
CREATE DATABASE loan_db;
CREATE DATABASE credit_card_db;
CREATE DATABASE transaction_db;
CREATE DATABASE notification_db;
```

### 3. Start Kafka

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka Broker
bin/kafka-server-start.sh config/server.properties
```

### 4. Build the Project

```bash
mvn clean install -DskipTests
```

### 5. Start Services (In Order)

```bash
# 1. Eureka Registry (must start first)
cd eureka-registry && mvn spring-boot:run

# 2. Core Services (any order)
cd customer-service && mvn spring-boot:run
cd account-service && mvn spring-boot:run
cd loan-service && mvn spring-boot:run
cd credit-card-service && mvn spring-boot:run

# 3. Event Consumers
cd transaction-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

### 6. Verify

- **Eureka Dashboard**: http://localhost:8761 — all services should appear as registered

---

## Key Features

- **Microservices Architecture** — independently deployable services
- **Database Per Service** — each service owns its PostgreSQL database
- **Event-Driven** — Kafka for async notifications and transaction recording
- **Idempotent Operations** — duplicate-safe deposits, withdrawals, and transfers
- **State Machines** — enforced lifecycle transitions for loans and credit cards
- **OpenFeign** — declarative inter-service REST communication
- **Virtual Threads** — enabled for high-throughput I/O
- **Structured Logging** — SLF4J with contextual key-value pairs
- **Global Exception Handling** — centralized via `common-lib`
- **Shared Library** — DTOs, events, and constants in `common-lib`

---

## Future Enhancements

- [ ] API Gateway (Spring Cloud Gateway)
- [ ] JWT-based authentication & authorization
- [ ] Circuit breaker (Resilience4j)
- [ ] Distributed tracing (Micrometer + Zipkin)
- [ ] Docker & Docker Compose
- [ ] Kubernetes deployment
- [ ] Redis caching
- [ ] Swagger / OpenAPI documentation
- [ ] Unit & integration tests

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit changes (`git commit -am 'Add new feature'`)
4. Push to branch (`git push origin feature/your-feature`)
5. Create a Pull Request

## License

This project is licensed under the MIT License.

---

**Built with ☕ using Spring Boot & Spring Cloud**