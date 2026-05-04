# Generic Notification Entity — Design

## Current State

Your current `Notification` entity is minimal:

```java
// Current Entity
notificationId  → UUID (PK)
customerId      → UUID
channelType     → ChannelType (EMAIL, SMS, PUSH_NOTIFICATION)
message         → String
sentAt          → LocalDateTime
retryCount      → int
status          → String  // "SUCCESS" / free text
```

### Problems
1. **No way to identify the source** — You can't tell if a notification came from account-service, loan-service, or credit-card-service.
2. **No notification type** — All notifications look the same (account creation, withdrawal, loan approval, etc.).
3. **No reference tracking** — No `referenceId` to correlate back to the original entity (accountNumber, loanId, cardId).
4. **`status` is a raw String** — Should be an enum for consistency.
5. **No `subject`/`title`** — Only a message body, no short summary.

---

## Proposed Generic Entity

```
┌──────────────────────────────────────────────────────────┐
│                     NOTIFICATION                         │
├──────────────────────────────────────────────────────────┤
│  notificationId    UUID (PK, auto-generated)             │
│  customerId        UUID (who receives this)              │
│  sourceService     SourceService ENUM                    │
│  notificationType  NotificationType ENUM                 │
│  channelType       ChannelType ENUM                      │
│  referenceId       String (accountNo / loanId / cardId)  │
│  subject           String (short title/summary)          │
│  message           String (full message body)            │
│  metadata          String (JSON — extensible payload)    │
│  status            NotificationStatus ENUM               │
│  retryCount        int (default 0)                       │
│  createdAt         LocalDateTime (when created)          │
│  sentAt            LocalDateTime (when actually sent)    │
└──────────────────────────────────────────────────────────┘
```

## New Enums

### `SourceService`
Identifies which microservice triggered the notification:
- `ACCOUNT_SERVICE`
- `LOAN_SERVICE`
- `CREDIT_CARD_SERVICE`
- `TRANSACTION_SERVICE`

### `NotificationType`
Classifies what happened:
- `ACCOUNT_CREATED`
- `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`
- `LOAN_APPLIED`, `LOAN_APPROVED`, `LOAN_REJECTED`, `LOAN_DISBURSED`
- `CREDIT_CARD_APPLIED`, `CREDIT_CARD_APPROVED`, `CREDIT_CARD_REJECTED`, `CREDIT_CARD_BLOCKED`, `CREDIT_CARD_UNBLOCKED`, `CREDIT_CARD_CLOSED`
- `CREDIT_CARD_CHARGE`, `CREDIT_CARD_PAYMENT`

### `NotificationStatus` (replaces raw String)
- `PENDING`
- `SENT`
- `FAILED`
- `RETRYING`

### `ChannelType` (unchanged)
- `EMAIL`, `SMS`, `PUSH_NOTIFICATION`

## Key Design Decisions

| Decision | Rationale |
|---|---|
| `referenceId` as `String` | Accounts use accountNumber (String), loans/cards use UUID. String accommodates both. |
| `metadata` as JSON String | Stores event-specific data (amount, balance, transactionType, etc.) without adding columns. **Never need to change the entity again.** |
| `createdAt` + `sentAt` split | `createdAt` = when notification was queued. `sentAt` = when it was actually delivered. Useful for retry tracking. |
| `subject` field | Short summary (e.g., "Account Created") for listing/display purposes. |
| Enum for `status` | Type-safe, no magic strings. |

## Files to Create/Modify

| File | Action |
|---|---|
| `notification-service/.../ENUM/SourceService.java` | **New** |
| `notification-service/.../ENUM/NotificationType.java` | **New** |
| `notification-service/.../ENUM/NotificationStatus.java` | **New** |
| `notification-service/.../ENUM/ChannelType.java` | Unchanged |
| `notification-service/.../model/Notification.java` | **Rewrite** |
| `notification-service/.../repository/NotificationRepository.java` | Minor query updates |

> [!IMPORTANT]
> The `NotificationService.java` (Kafka listeners) and `NotificationRequestDTO.java` will need updates too, but those are **separate tasks** after the entity is finalized.
