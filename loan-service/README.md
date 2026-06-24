# Loan Service

The **Loan Ingestion & Lifecycle Management Service** acts as the entry gateway for applicants. It ingests new applications, manages customer state, persists data in a relational database, and broadcasts submission events to the event stream.

---

## 🛠️ Tech Stack & Architecture
* **Java 21**
* **Spring Boot 3.2.5** (Starter Web, Data JPA, Validation)
* **PostgreSQL** (Active database) & **H2** (In-memory fallback for development)
* **Apache Kafka** (Event publisher & subscriber)
* **Lombok** & **JUnit 5 / Mockito**

---

## 🔌 API Endpoints

### 1. Submit Loan Application
* **URL:** `POST /api/loans`
* **CORS:** Allowed from all origins (`*`)
* **Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "9876543210",
  "panNumber": "ABCDE1234F",
  "monthlyIncome": 7500.0,
  "existingEmi": 1000.0,
  "loanAmount": 30000.0,
  "tenureMonths": 36
}
```
* **Response (Status 201 Created):**
```json
{
  "id": 1,
  "customerName": "John Doe",
  "email": "john.doe@example.com",
  "panNumber": "ABCDE1234F",
  "monthlyIncome": 7500.0,
  "existingEmi": 1000.0,
  "loanAmount": 30000.0,
  "tenureMonths": 36,
  "status": "SUBMITTED",
  "createdAt": "2026-06-23T13:40:00"
}
```

### 2. Fetch Application details
* **URL:** `GET /api/loans/{id}`
* **Response (Status 200 OK):** Returns full details including status, interest rate, and AI decision details.

### 3. Fetch All Applications
* **URL:** `GET /api/loans`

---

## ⚡ Event-Driven Integration (Kafka)
* **Publisher Topic:** `loan-applications` (Sends `LoanSubmittedEvent` to notify underwriting engine).
* **Subscriber Topic:** `credit-evaluations` (Listens to updates from `underwriting-service` to transition application states to `APPROVED` or `DECLINED` and record interest rates).

---

## 🚀 Setup & Execution
1. Ensure the shared Docker infrastructure is running:
   ```bash
   cd ../infra
   docker-compose up -d
   ```
2. Build and run the project:
   ```bash
   mvn spring-boot:run
   ```
   *(By default, the `dev` profile runs with an in-memory H2 database. Run with `-Dspring.profiles.active=prod` to connect to PostgreSQL).*
