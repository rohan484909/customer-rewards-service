# Customer Rewards Service

A Spring Boot REST API that calculates reward points earned by retail customers
based on their purchase transactions over a three-month period.

---

## Points Calculation Rules

| Purchase Amount        | Points Earned                          |
|------------------------|----------------------------------------|
| ≤ $50                  | 0 points                               |
| $50 – $100             | 1 point per dollar above $50           |
| > $100                 | 1 pt/dollar for $50–$100 **+** 2 pts/dollar above $100 |

**Example:** A $120 purchase earns `2×$20 + 1×$50 = 90 points`

---

## Tech Stack

| Layer       | Technology                   |
|-------------|------------------------------|
| Framework   | Spring Boot 3.2              |
| Language    | Java 17                      |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Build       | Maven                        |
| Testing     | JUnit 5 + Mockito            |

---

## Quick Start

```bash
# Clone
git clone https://github.com/<your-username>/customer-rewards-service.git
cd customer-rewards-service

# Build & run
./mvnw spring-boot:run

# The API is now live at http://localhost:8080
```

The app auto-seeds 5 customers and ~35 transactions spread across 3 months
when it starts for the first time.

---

## REST Endpoints

### 1. All customers – last 3 months
```
GET /api/rewards
```
Returns an array of reward summaries, one per customer.

**Sample Response**
```json
[
  {
    "customerId": 1,
    "customerName": "Alice Johnson",
    "customerEmail": "alice@example.com",
    "monthlyRewards": [
      { "year": 2025, "month": 1, "monthName": "January", "points": 120, "transactionCount": 3 },
      { "year": 2025, "month": 2, "monthName": "February", "points": 320, "transactionCount": 2 },
      { "year": 2025, "month": 3, "monthName": "March", "points": 115, "transactionCount": 2 }
    ],
    "totalPoints": 555
  }
]
```

---

### 2. Single customer – last 3 months
```
GET /api/rewards/{customerId}
```

| Parameter    | Type | Description            |
|--------------|------|------------------------|
| `customerId` | Long | The customer's database ID |

---

### 3. Single customer – custom date range
```
GET /api/rewards/{customerId}/period?start=YYYY-MM-DD&end=YYYY-MM-DD
```

| Parameter    | Type       | Description                       |
|--------------|------------|-----------------------------------|
| `customerId` | Long       | The customer's database ID        |
| `start`      | LocalDate  | Start date inclusive (ISO format) |
| `end`        | LocalDate  | End date inclusive (ISO format)   |

---

## Running Tests

```bash
./mvnw test
```

Eight unit tests cover:
- All point-tier boundary conditions (0, 30, 50, 75, 100, 120, 200 dollars)
- Null / zero input guards
- Monthly aggregation and grand total
- Customer with no transactions

---

## H2 Console (dev only)

Browse the live database at:  
`http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:rewardsdb`  
User: `sa` | Password: *(empty)*

---

## Project Structure

```
src/
├── main/java/com/retailer/rewards/
│   ├── RewardsApplication.java       # Entry point
│   ├── controller/
│   │   └── RewardsController.java    # REST endpoints
│   ├── service/
│   │   └── RewardsService.java       # Business logic & point calculation
│   ├── model/
│   │   ├── Customer.java             # JPA entity
│   │   ├── Transaction.java          # JPA entity
│   │   ├── MonthlyReward.java        # Response DTO
│   │   └── CustomerRewardSummary.java# Response DTO
│   ├── repository/
│   │   ├── CustomerRepository.java
│   │   └── TransactionRepository.java
│   └── data/
│       └── DataSeeder.java           # Sample data loader
└── test/java/com/retailer/rewards/
    └── RewardsServiceTest.java       # Unit tests
```
