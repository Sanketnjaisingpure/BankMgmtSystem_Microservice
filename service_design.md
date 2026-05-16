# Bank Management System — Service Design

> **Stack:** Java 21 · Spring Boot 3.2 · Spring Cloud (Feign + Eureka) · Apache Kafka · PostgreSQL · Flyway · Lombok

---

## System Overview

The Bank Management System is a microservices architecture composed of **9 services** that together manage the full lifecycle of banking operations — from customer onboarding to accounts, loans, credit cards, transactions, and notifications.

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (:8765)                       │
│              (Single entry point, routes all traffic)            │
└────────────────────────────┬────────────────────────────────────┘
                             │
         ┌───────────────────┼───────────────────────┐
         ▼                   ▼                       ▼
  ┌─────────────┐    ┌─────────────┐        ┌──────────────────┐
  │  Eureka     │    │  customer   │        │   bank-service   │
  │  Registry   │    │  service    │        │   (:8086)        │
  │  (:8761)    │    │  (:8080)    │        └──────────────────┘
  └─────────────┘    └─────────────┘
                             │ Feign
         ┌───────────────────┼───────────────────────┐
         ▼                   ▼                       ▼
  ┌─────────────┐    ┌─────────────┐        ┌──────────────────┐
  │  account    │    │   loan      │        │  credit-card     │
  │  service    │    │  service    │        │  service         │
  │  (:8081)    │    │  (:8083)    │        │  (:8085)         │
  └─────────────┘    └─────────────┘        └──────────────────┘
         │                   │                       │
         └───────────────────┴───────────────────────┘
                             │ Kafka
         ┌───────────────────┼───────────────────────┐
         ▼                   ▼                       ▼
  ┌─────────────┐    ┌─────────────────┐    ┌──────────────────┐
  │ transaction │    │  notification   │    │   common-lib     │
  │  service    │    │  service        │    │  (shared lib)    │
  └─────────────┘    └─────────────────┘    └──────────────────┘
```

---

## Service Catalogue

| Service | Port | DB | Role |
|---------|------|----|------|
| `eureka-registry` | 8761 | — | Service discovery registry |
| `api-gateway` | 8765 | — | Single entry point, request routing |
| `customer-service` | 8080 | `customer_db` | Customer CRUD |
| `account-service` | 8081 | `account_db` | Account management, transactions |
| `loan-service` | 8083 | `loan_db` | Loan lifecycle |
| `credit-card-service` | 8085 | `credit_card_db` | Credit card lifecycle |
| `bank-service` | 8086 | `bank_db` | Bank registration |
| `transaction-service` | — | `transaction_db` | Transaction ledger (Kafka consumer) |
| `notification-service` | — | `notification_db` | Notifications (Kafka consumer) |

---

## 1. customer-service

### Purpose
Manages the customer registry. The foundation of all other services — every account, loan, and card is tied to a `customerId`.

### Entity — `Customer`
| Field | Type | Constraint |
|-------|------|------------|
| `customerId` | `UUID` | PK, auto-generated |
| `firstName` | `String` | required |
| `lastName` | `String` | required |
| `email` | `String` | unique |
| `mobileNumber` | `String` | unique |
| `passwordHash` | `String` | required |
| `createdAt` | `LocalDateTime` | required |
| `updatedAt` | `LocalDateTime` | required |

### API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/customers/create-customer` | Register a new customer |
| `GET` | `/api/v1/customers/find-by-id?customerId=` | Fetch by UUID |
| `GET` | `/api/v1/customers/find-by-email?email=` | Fetch by email |
| `POST` | `/api/v1/customers/update-customer` | Update customer details |
| `DELETE` | `/api/v1/customers/delete-customer?customerId=` | Delete customer |
| `GET` | `/api/v1/customers/find-all-customers` | Paginated list |

### Used By (Feign Consumers)
- `account-service` — validates customer before account creation
- `loan-service` — validates customer before loan application
- `credit-card-service` — validates customer before card application
- `bank-service` — (previously, removed from current design)

---

## 2. bank-service

### Purpose
Administrative registry of banks. Clients must register a bank first and pass its `bankId` when creating accounts. Keeps bank registration decoupled from operational logic.

### Entity — `Bank`
| Field | Type | Constraint |
|-------|------|------------|
| `bankId` | `UUID` | PK |
| `bankName` | `String` | required |
| `bankCode` | `String` | unique |
| `headquartersCity` | `String` | required |
| `ifscPrefix` | `String` | 4 chars exactly |
| `contactEmail` | `String` | required |
| `contactPhone` | `String` | required |
| `bankStatus` | `BankStatus` | ACTIVE / SUSPENDED / CLOSED |
| `createdAt` | `LocalDateTime` | required |
| `updatedAt` | `LocalDateTime` | required |

### State Machine
```
[*] ──► ACTIVE ──► SUSPENDED ──► ACTIVE
                       └──► CLOSED
```

### API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/banks/register` | Register a new bank |
| `GET` | `/api/v1/banks/{bankId}` | Get bank by UUID |
| `GET` | `/api/v1/banks/code/{bankCode}` | Get bank by code |
| `GET` | `/api/v1/banks` | List all banks |
| `PUT` | `/api/v1/banks/{bankId}/status` | Update bank status |
| `DELETE` | `/api/v1/banks/{bankId}` | Delete bank |

### Kafka Events Published
| Topic | Trigger |
|-------|---------|
| `bank-registration-topic` | New bank registered |

---

## 3. account-service

### Purpose
Core financial service. Manages bank accounts, balances, and all money movement (deposit, withdraw, transfer). Validates the bank via Feign before creation. Uses idempotency keys to prevent duplicate transactions.

### Entity — `Account`
| Field | Type | Constraint |
|-------|------|------------|
| `accountId` | `UUID` | PK |
| `customerId` | `UUID` | required |
| `bankId` | `UUID` | required — must reference an ACTIVE bank |
| `accountNumber` | `String` | unique, 12-digit generated |
| `accountType` | `AccountType` | SAVINGS / CURRENT / FIXED_DEPOSIT |
| `status` | `AccountStatus` | ACTIVE / INACTIVE / BLOCKED / CLOSED |
| `balance` | `BigDecimal` | min > 500 on creation |
| `ifscCode` | `String` | required |
| `branchName` | `String` | required |
| `createdAt` | `LocalDateTime` | required |
| `updatedAt` | `LocalDateTime` | required |

### API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/accounts/create-account` | Create account (requires `bankId`) |
| `GET` | `/api/v1/accounts/get-account-by-account-number` | Fetch by account number |
| `GET` | `/api/v1/accounts/get-account-by-Id` | Fetch by UUID |
| `GET` | `/api/v1/accounts/get-all-accounts-by-customer-id` | Paginated by customer |
| `GET` | `/api/v1/accounts/get-balance` | Get current balance |
| `PUT` | `/api/v1/accounts/update-status` | Change account status |
| `PUT` | `/api/v1/accounts/{accountNumber}/depositCredit` | Deposit (idempotent) |
| `PUT` | `/api/v1/accounts/{accountNumber}/withdrawDebit` | Withdraw (idempotent) |
| `PUT` | `/api/v1/accounts/transfer-amount` | Transfer between accounts |
| `DELETE` | `/api/v1/accounts/delete-account-by-Id` | Delete account |

### Feign Dependencies
| Service | Purpose |
|---------|---------|
| `customer-service` | Validate customer exists |
| `bank-service` | Validate bank is ACTIVE before linking |

### Kafka Events Published
| Topic | Event | Trigger |
|-------|-------|---------|
| `account-creation-topic` | `AccountCreationEvent` | Account created |
| `transaction-notification-topic` | `TransactionNotificationEvent` | Deposit / Withdraw / Transfer |
| `transaction-topic` | `TransactionEvent` | Account creation (initial deposit) |
| `transaction-payment-topic` | `TransactionEvent` | Deposit / Withdraw / Transfer |

---

## 4. loan-service

### Purpose
Manages the full loan lifecycle from application to disbursement. Validates customer and account via Feign. Calculates EMI. Credits loan amount to the customer's account on disbursement.

### Entity — `Loan`
| Field | Type | Constraint |
|-------|------|------------|
| `loanId` | `UUID` | PK |
| `customerId` | `UUID` | required |
| `accountNumber` | `String` | required |
| `loanAmount` | `BigDecimal` | required |
| `interestRate` | `Double` | required |
| `emiAmount` | `BigDecimal` | computed on approval |
| `loanStatus` | `LoanStatus` | PENDING / APPROVED / REJECTED / ACTIVE / CLOSED |
| `tenureMonths` | `Integer` | required |
| `createdAt` | `LocalDateTime` | required |
| `updatedAt` | `LocalDateTime` | required |

### State Machine
```
[*] ──► PENDING ──► APPROVED ──► ACTIVE (disbursed)
            └──► REJECTED
```

### API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/loans/apply` | Apply for a loan |
| `PUT` | `/api/v1/loans/{loanId}/approve` | Approve + compute EMI |
| `PUT` | `/api/v1/loans/{loanId}/reject` | Reject application |
| `PUT` | `/api/v1/loans/{loanId}/disburse` | Disburse to account |
| `GET` | `/api/v1/loans/{loanId}` | Get loan details |

### Kafka Events Published
| Topic | Event | Trigger |
|-------|-------|---------|
| `loan-application-topic` | `LoanApplicationEvent` | Loan applied |
| `loan-status-topic` | `LoanStatusEvent` | Approved / Rejected |
| `loan-disbursement-topic` | `LoanDisbursementEvent` | Loan disbursed |

---

## 5. credit-card-service

### Purpose
Full credit card lifecycle — application, approval, activation, charges, payments, blocking, and closure. Validates customer and account ownership via Feign. Debits linked account for credit card payments.

### Entity — `CreditCard`
| Field | Type | Constraint |
|-------|------|------------|
| `cardId` | `UUID` | PK |
| `cardNumber` | `String` | masked `****-****-****-XXXX` |
| `customerId` | `UUID` | required |
| `accountNumber` | `String` | linked account for payments |
| `cardHolderName` | `String` | required |
| `creditLimit` | `BigDecimal` | default 50,000 |
| `availableLimit` | `BigDecimal` | updated on charge/payment |
| `outstandingBalance` | `BigDecimal` | updated on charge/payment |
| `minimumDueAmount` | `BigDecimal` | computed |
| `annualFee` | `BigDecimal` | default 499 |
| `interestRate` | `Double` | default 36% APR |
| `cardStatus` | `CardStatus` | PENDING / APPROVED / ACTIVE / BLOCKED / REJECTED / CLOSED |
| `expiryDate` | `LocalDate` | 5 years from creation |
| `billingCycleDay` | `Integer` | 1–28 |
| `createdAt` | `LocalDateTime` | required |
| `updatedAt` | `LocalDateTime` | required |

### State Machine
```
[*] ──► PENDING ──► APPROVED ──► ACTIVE ──► BLOCKED ──► ACTIVE
             └──► REJECTED         │            └──► CLOSED
                                   └──► CLOSED
```

### API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/credit-cards/apply` | Apply for a card |
| `PUT` | `/api/v1/credit-cards/{cardId}/approve` | Approve |
| `PUT` | `/api/v1/credit-cards/{cardId}/reject` | Reject |
| `PUT` | `/api/v1/credit-cards/{cardId}/activate` | Activate |
| `PUT` | `/api/v1/credit-cards/{cardId}/block` | Block |
| `PUT` | `/api/v1/credit-cards/{cardId}/unblock` | Unblock |
| `PUT` | `/api/v1/credit-cards/{cardId}/close` | Close permanently |
| `POST` | `/api/v1/credit-cards/{cardId}/charge` | Make a purchase |
| `POST` | `/api/v1/credit-cards/{cardId}/payment` | Pay outstanding balance |
| `GET` | `/api/v1/credit-cards/{cardId}` | Get card details |
| `GET` | `/api/v1/credit-cards/customer/{customerId}` | All cards for customer |

### Feign Dependencies
| Service | Purpose |
|---------|---------|
| `customer-service` | Validate customer on application |
| `account-service` | Validate account ownership; debit for payments |

### Kafka Events Published
| Topic | Event | Trigger |
|-------|-------|---------|
| `credit-card-application-topic` | `CreditCardApplicationEvent` | Card applied |
| `credit-card-status-topic` | `CreditCardStatusEvent` | Status change |
| `credit-card-transaction-topic` | `CreditCardTransactionEvent` | Charge / Payment |
| `transaction-payment-topic` | `TransactionEvent` | CC payment debited from account |

---

## 6. transaction-service

### Purpose
Immutable ledger of all financial events. Pure Kafka consumer — never exposes write endpoints. Records all deposits, withdrawals, transfers, and credit card payments in one place.

### Entity — `Transaction`
| Field | Type | Constraint |
|-------|------|------------|
| `transactionId` | `UUID` | PK |
| `transactionType` | `TransactionType` | DEPOSIT / WITHDRAW / TRANSFER |
| `transactionStatus` | `TransactionStatus` | SUCCESS / FAILED / PENDING |
| `amount` | `BigDecimal` | required |
| `sourceAccountNumber` | `String` | required |
| `destinationAccountNumber` | `String` | required |
| `transactionDescription` | `String` | required |
| `createdAt` | `LocalDateTime` | required |

### Kafka Topics Consumed
| Topic | Published By |
|-------|-------------|
| `transaction-topic` | account-service (account creation) |
| `transaction-payment-topic` | account-service, credit-card-service |

---

## 7. notification-service

### Purpose
Cross-cutting notification hub. Listens to all Kafka topics from all services and persists a rich notification record. Supports querying by customer, type, source, status, reference, and date range.

### Entity — `Notification`
| Field | Type | Constraint |
|-------|------|------------|
| `notificationId` | `UUID` | PK |
| `customerId` | `UUID` | required |
| `sourceService` | `SourceService` | ACCOUNT_SERVICE / LOAN_SERVICE / CREDIT_CARD_SERVICE / TRANSACTION_SERVICE |
| `notificationType` | `NotificationType` | see enum below |
| `channelType` | `ChannelType` | EMAIL / SMS / PUSH_NOTIFICATION |
| `referenceId` | `String` | accountNumber / loanId / cardId |
| `subject` | `String` | short title |
| `message` | `String` | full message body |
| `metadata` | `String` | JSON blob (amount, balance, etc.) |
| `status` | `NotificationStatus` | PENDING / SENT / FAILED / RETRYING |
| `retryCount` | `int` | default 0 |
| `createdAt` | `LocalDateTime` | when queued |
| `sentAt` | `LocalDateTime` | when delivered |

### NotificationType Values
```
Account:     ACCOUNT_CREATED, DEPOSIT, WITHDRAWAL, TRANSFER
Loan:        LOAN_APPLIED, LOAN_APPROVED, LOAN_REJECTED, LOAN_DISBURSED
Credit Card: CREDIT_CARD_APPLIED, CREDIT_CARD_APPROVED, CREDIT_CARD_REJECTED,
             CREDIT_CARD_ACTIVATED, CREDIT_CARD_BLOCKED, CREDIT_CARD_UNBLOCKED,
             CREDIT_CARD_CLOSED, CREDIT_CARD_CHARGE, CREDIT_CARD_PAYMENT
```

### Kafka Topics Consumed
| Topic | Published By |
|-------|-------------|
| `account-creation-topic` | account-service |
| `transaction-notification-topic` | account-service |
| `loan-application-topic` | loan-service |
| `loan-status-topic` | loan-service |
| `loan-disbursement-topic` | loan-service |
| `credit-card-application-topic` | credit-card-service |
| `credit-card-status-topic` | credit-card-service |
| `credit-card-transaction-topic` | credit-card-service |

---

## 8. common-lib

### Purpose
Shared library packaged as a Maven artifact (`com.bank:common-lib:1.0-SNAPSHOT`). Contains everything imported by multiple services.

### Contents
```
common-lib/src/main/java/com/bank/
├── config/
│   ├── KafkaConstants.java      ← All topic + group name constants
│   ├── KafkaTopic.java
│   └── MapperConfig.java        ← ModelMapper bean
├── ENUM/
│   ├── TransactionType.java     ← DEPOSIT, WITHDRAW, TRANSFER
│   └── TransactionStatus.java   ← SUCCESS, FAILED, PENDING
├── event/
│   ├── AccountCreationEvent.java
│   ├── TransactionEvent.java
│   ├── TransactionNotificationEvent.java
│   ├── LoanApplicationEvent.java
│   ├── LoanStatusEvent.java
│   ├── LoanDisbursementEvent.java
│   ├── CreditCardApplicationEvent.java
│   ├── CreditCardStatusEvent.java
│   └── CreditCardTransactionEvent.java
├── dto/
│   └── CustomerDTO.java         ← shared Feign response DTO
├── exception/
│   └── ResourceNotFoundException.java
└── handler/
    └── GlobalExceptionHandler.java
```

---

## Infrastructure

### Kafka Topics — Master List
| Topic | Publisher | Consumer(s) |
|-------|-----------|-------------|
| `account-creation-topic` | account-service | notification-service |
| `transaction-notification-topic` | account-service | notification-service |
| `transaction-topic` | account-service | transaction-service |
| `transaction-payment-topic` | account-service, credit-card-service | transaction-service |
| `loan-application-topic` | loan-service | notification-service |
| `loan-status-topic` | loan-service | notification-service |
| `loan-disbursement-topic` | loan-service | notification-service |
| `credit-card-application-topic` | credit-card-service | notification-service |
| `credit-card-status-topic` | credit-card-service | notification-service |
| `credit-card-transaction-topic` | credit-card-service | notification-service |
| `bank-registration-topic` | bank-service | notification-service |

### Feign Call Map
| Caller | Calls | Method |
|--------|-------|--------|
| `account-service` | `customer-service` | GET customer by ID |
| `account-service` | `bank-service` | GET bank by ID (validate ACTIVE) |
| `loan-service` | `customer-service` | GET customer by ID |
| `loan-service` | `account-service` | GET account; PUT deposit (disburse) |
| `credit-card-service` | `customer-service` | GET customer by ID |
| `credit-card-service` | `account-service` | GET account; PUT withdraw (payment) |
