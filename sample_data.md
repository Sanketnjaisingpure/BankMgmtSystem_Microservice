# 🧪 Sample Test Data — 2 Samples Per Endpoint

---

## 1. 🏢 Bank Service

### 1.1 Register a Bank — POST `/api/v1/banks/register`

**Sample 1:**
```json
{
  "bankName": "State Bank of India",
  "bankCode": "SBIN",
  "headquartersCity": "Mumbai",
  "ifscPrefix": "SBIN",
  "contactEmail": "contact@sbi.co.in",
  "contactPhone": "1800112211"
}
```

**Sample 2:**
```json
{
  "bankName": "HDFC Bank",
  "bankCode": "HDFC",
  "headquartersCity": "Mumbai",
  "ifscPrefix": "HDFC",
  "contactEmail": "support@hdfcbank.com",
  "contactPhone": "18002586181"
}
```

---

### 1.2 Get Bank by ID — GET `/api/v1/banks/{bankId}`

**Sample 1:**
```
GET /api/v1/banks/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Sample 2:**
```
GET /api/v1/banks/f9e8d7c6-b5a4-3210-fedc-ba9876543210
```

---

### 1.3 Get Bank by Short Code — GET `/api/v1/banks/code/{code}`

**Sample 1:**
```
GET /api/v1/banks/code/SBIN
```

**Sample 2:**
```
GET /api/v1/banks/code/HDFC
```

---

### 1.5 Update Bank Status — PUT `/api/v1/banks/{bankId}/status?status=`

**Sample 1:**
```
PUT /api/v1/banks/a1b2c3d4-e5f6-7890-abcd-ef1234567890/status?status=SUSPENDED
```

**Sample 2:**
```
PUT /api/v1/banks/f9e8d7c6-b5a4-3210-fedc-ba9876543210/status?status=CLOSED
```

---

### 1.6 Delete Bank — DELETE `/api/v1/banks/{bankId}`

**Sample 1:**
```
DELETE /api/v1/banks/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Sample 2:**
```
DELETE /api/v1/banks/f9e8d7c6-b5a4-3210-fedc-ba9876543210
```

---

## 2. 👥 Customer Service

### 2.1 Create a Customer — POST `/api/v1/customers/create-customer`

**Sample 1:**
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "email": "rahul.sharma@example.com",
  "passwordHash": "SecurePass@123",
  "mobileNumber": "9876543210"
}
```

**Sample 2:**
```json
{
  "firstName": "Priya",
  "lastName": "Patel",
  "email": "priya.patel@example.com",
  "passwordHash": "MySecure@456",
  "mobileNumber": "9123456789"
}
```

---

### 2.2 Get Customer by ID — GET `/api/v1/customers/find-by-id?customerId=`

**Sample 1:**
```
GET /api/v1/customers/find-by-id?customerId=c1d2e3f4-a5b6-7890-cdef-012345678901
```

**Sample 2:**
```
GET /api/v1/customers/find-by-id?customerId=d4e5f6a7-b8c9-0123-defg-234567890123
```

---

### 2.3 Get Customer by Email — GET `/api/v1/customers/find-by-email?email=`

**Sample 1:**
```
GET /api/v1/customers/find-by-email?email=rahul.sharma@example.com
```

**Sample 2:**
```
GET /api/v1/customers/find-by-email?email=priya.patel@example.com
```

---

### 2.4 Update Customer — POST `/api/v1/customers/update-customer`

**Sample 1:**
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma Updated",
  "email": "rahul.sharma@example.com",
  "mobileNumber": "9999911111"
}
```

**Sample 2:**
```json
{
  "firstName": "Priya",
  "lastName": "Mehta",
  "email": "priya.patel@example.com",
  "mobileNumber": "9888822222"
}
```

---

### 2.5 Find All Customers — GET `/api/v1/customers/find-all-customers`

**Sample 1:**
```
GET /api/v1/customers/find-all-customers?pageNumber=0&pageSize=5&sort=firstName&order=asc
```

**Sample 2:**
```
GET /api/v1/customers/find-all-customers?pageNumber=1&pageSize=10&sort=lastName&order=desc
```

---

### 2.6 Delete Customer — DELETE `/api/v1/customers/delete-customer?customerId=`

**Sample 1:**
```
DELETE /api/v1/customers/delete-customer?customerId=c1d2e3f4-a5b6-7890-cdef-012345678901
```

**Sample 2:**
```
DELETE /api/v1/customers/delete-customer?customerId=d4e5f6a7-b8c9-0123-defg-234567890123
```

---

## 3. 💳 Account Service

### 3.1 Open a New Account — POST `/api/v1/accounts/create-account`

**Sample 1:**
```json
{
  "customerId": "c1d2e3f4-a5b6-7890-cdef-012345678901",
  "accountType": "SAVINGS",
  "ifscCode": "SBIN0001234",
  "branchName": "Connaught Place, New Delhi",
  "balance": 50000.00,
  "bankId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Sample 2:**
```json
{
  "customerId": "d4e5f6a7-b8c9-0123-defg-234567890123",
  "accountType": "CURRENT",
  "ifscCode": "HDFC0005678",
  "branchName": "Bandra West, Mumbai",
  "balance": 100000.00,
  "bankId": "f9e8d7c6-b5a4-3210-fedc-ba9876543210"
}
```

---

### 3.2 Get Account by Account Number — GET `/api/v1/accounts/get-account-by-account-number?accountNumber=`

**Sample 1:**
```
GET /api/v1/accounts/get-account-by-account-number?accountNumber=ACC1001234567
```

**Sample 2:**
```
GET /api/v1/accounts/get-account-by-account-number?accountNumber=ACC1009876543
```

---

### 3.3 Get All Accounts by Customer ID — GET `/api/v1/accounts/get-all-accounts-by-customer-id?customerId=`

**Sample 1:**
```
GET /api/v1/accounts/get-all-accounts-by-customer-id?customerId=c1d2e3f4-a5b6-7890-cdef-012345678901
```

**Sample 2:**
```
GET /api/v1/accounts/get-all-accounts-by-customer-id?customerId=d4e5f6a7-b8c9-0123-defg-234567890123
```

---

### 3.5 Check Balance — GET `/api/v1/accounts/get-balance?accountNumber=`

**Sample 1:**
```
GET /api/v1/accounts/get-balance?accountNumber=ACC1001234567
```

**Sample 2:**
```
GET /api/v1/accounts/get-balance?accountNumber=ACC1009876543
```

---

### 3.6 Deposit Funds — PUT `/api/v1/accounts/{accountNumber}/depositCredit?amount=`

**Sample 1:**
```
PUT /api/v1/accounts/ACC1001234567/depositCredit?amount=20000.00
Headers: Idempotency-key: 550e8400-e29b-41d4-a716-446655440010
```

**Sample 2:**
```
PUT /api/v1/accounts/ACC1009876543/depositCredit?amount=50000.00
Headers: Idempotency-key: 550e8400-e29b-41d4-a716-446655440011
```

---

### 3.7 Withdraw Funds — PUT `/api/v1/accounts/{accountNumber}/withdrawDebit?amount=`

**Sample 1:**
```
PUT /api/v1/accounts/ACC1001234567/withdrawDebit?amount=5000.00
Headers: Idempotency-key: 550e8400-e29b-41d4-a716-446655440020
```

**Sample 2:**
```
PUT /api/v1/accounts/ACC1009876543/withdrawDebit?amount=15000.00
Headers: Idempotency-key: 550e8400-e29b-41d4-a716-446655440021
```

---

### 3.8 Transfer Amount — PUT `/api/v1/accounts/transfer-amount`

**Sample 1:**
```json
{
  "sourceAccountNumber": "ACC1001234567",
  "destinationAccountNumber": "ACC1009876543",
  "amount": 10000.00
}
```
`Headers: Idempotency-key: 550e8400-e29b-41d4-a716-446655440030`

**Sample 2:**
```json
{
  "sourceAccountNumber": "ACC1009876543",
  "destinationAccountNumber": "ACC1001234567",
  "amount": 3500.00
}
```
`Headers: Idempotency-key: 550e8400-e29b-41d4-a716-446655440031`

---

### 3.9 Update Account Status — PUT `/api/v1/accounts/update-status`

**Sample 1:**
```
PUT /api/v1/accounts/update-status?accountNumber=ACC1001234567&status=BLOCKED
```

**Sample 2:**
```
PUT /api/v1/accounts/update-status?accountNumber=ACC1009876543&status=INACTIVE
```

---

### 3.10 Delete Account — DELETE `/api/v1/accounts/delete-account-by-Id?accountId=`

**Sample 1:**
```
DELETE /api/v1/accounts/delete-account-by-Id?accountId=aa11bb22-cc33-dd44-ee55-ff6677889900
```

**Sample 2:**
```
DELETE /api/v1/accounts/delete-account-by-Id?accountId=bb22cc33-dd44-ee55-ff66-aabb11223344
```

---

## 4. 📈 Loan Service

### 4.1 Apply for a Loan — POST `/api/v1/loans/apply`

**Sample 1:**
```json
{
  "customerId": "c1d2e3f4-a5b6-7890-cdef-012345678901",
  "accountNumber": "ACC1001234567",
  "loanAmount": 100000.00,
  "tenureMonths": 12
}
```

**Sample 2:**
```json
{
  "customerId": "d4e5f6a7-b8c9-0123-defg-234567890123",
  "accountNumber": "ACC1009876543",
  "loanAmount": 500000.00,
  "tenureMonths": 36
}
```

---

### 4.2 Approve Loan — PUT `/api/v1/loans/{loanId}/approve`

**Sample 1:**
```
PUT /api/v1/loans/loan-uuid-1111-aaaa-bbbb-ccccddddeeee/approve
```

**Sample 2:**
```
PUT /api/v1/loans/loan-uuid-2222-ffff-gggg-hhhhiiiijjjj/approve
```

---

### 4.3 Reject Loan — PUT `/api/v1/loans/{loanId}/reject`

**Sample 1:**
```
PUT /api/v1/loans/loan-uuid-3333-kkkk-llll-mmmmnnnnooo/reject
```

**Sample 2:**
```
PUT /api/v1/loans/loan-uuid-4444-pppp-qqqq-rrrrsssstttt/reject
```

---

### 4.4 Disburse Loan — PUT `/api/v1/loans/{loanId}/disburse`

**Sample 1:**
```
PUT /api/v1/loans/loan-uuid-1111-aaaa-bbbb-ccccddddeeee/disburse
```

**Sample 2:**
```
PUT /api/v1/loans/loan-uuid-2222-ffff-gggg-hhhhiiiijjjj/disburse
```

---

### 4.5 Get Loan Details — GET `/api/v1/loans/{loanId}`

**Sample 1:**
```
GET /api/v1/loans/loan-uuid-1111-aaaa-bbbb-ccccddddeeee
```

**Sample 2:**
```
GET /api/v1/loans/loan-uuid-2222-ffff-gggg-hhhhiiiijjjj
```

---

## 5. 💳 Credit Card Service

### 5.1 Apply for a Credit Card — POST `/api/v1/credit-cards/apply`

**Sample 1:**
```json
{
  "customerId": "c1d2e3f4-a5b6-7890-cdef-012345678901",
  "accountNumber": "ACC1001234567"
}
```

**Sample 2:**
```json
{
  "customerId": "d4e5f6a7-b8c9-0123-defg-234567890123",
  "accountNumber": "ACC1009876543"
}
```

---

### 5.2 Approve Card — PUT `/api/v1/credit-cards/{cardId}/approve`
### 5.3 Reject Card — PUT `/api/v1/credit-cards/{cardId}/reject`
### 5.4 Activate Card — PUT `/api/v1/credit-cards/{cardId}/activate`
### 5.5 Block Card — PUT `/api/v1/credit-cards/{cardId}/block`
### 5.6 Unblock Card — PUT `/api/v1/credit-cards/{cardId}/unblock`
### 5.7 Close Card — PUT `/api/v1/credit-cards/{cardId}/close`

**Sample 1 (for all above):**
```
PUT /api/v1/credit-cards/card-uuid-1111-aaaa-bbbb-ccccddddeeee/{action}
```

**Sample 2 (for all above):**
```
PUT /api/v1/credit-cards/card-uuid-2222-ffff-gggg-hhhhiiiijjjj/{action}
```

---

### 5.8 Charge Card — POST `/api/v1/credit-cards/{cardId}/charge`

**Sample 1:**
```json
{
  "amount": 4999.00,
  "description": "Amazon Purchase - Mobile Phone"
}
```

**Sample 2:**
```json
{
  "amount": 1200.00,
  "description": "Swiggy Food Order"
}
```

---

### 5.9 Bill Payment — POST `/api/v1/credit-cards/{cardId}/payment`

**Sample 1:**
```json
{
  "amount": 4999.00,
  "description": "Full Outstanding Balance Payment"
}
```

**Sample 2:**
```json
{
  "amount": 1000.00,
  "description": "Minimum Due Payment - May 2026"
}
```

---

### 5.10 Get Card by ID — GET `/api/v1/credit-cards/{cardId}`

**Sample 1:**
```
GET /api/v1/credit-cards/card-uuid-1111-aaaa-bbbb-ccccddddeeee
```

**Sample 2:**
```
GET /api/v1/credit-cards/card-uuid-2222-ffff-gggg-hhhhiiiijjjj
```

---

### 5.11 Get All Cards by Customer — GET `/api/v1/credit-cards/customer/{customerId}`

**Sample 1:**
```
GET /api/v1/credit-cards/customer/c1d2e3f4-a5b6-7890-cdef-012345678901
```

**Sample 2:**
```
GET /api/v1/credit-cards/customer/d4e5f6a7-b8c9-0123-defg-234567890123
```
