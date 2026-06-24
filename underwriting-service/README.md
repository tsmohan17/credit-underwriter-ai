# Underwriting Service

The **AI Underwriting & Credit Risk Analysis Service** is the cognitive heart of the platform. It consumes new applications from the event stream, analyzes applicant risk profiles using Spring AI (LLMs), generates credit decisions, and hosts an advisory chatbot.

---

## 🛠️ Tech Stack & Architecture
* **Java 21**
* **Spring Boot 3.2.5** (Starter Web, Data JPA)
* **Spring AI (OpenAI)** (Integrated with GPT-4o / Azure OpenAI)
* **PostgreSQL** & **H2** (In-memory dev database)
* **Apache Kafka** (Event listener & publisher)
* **Lombok** & **JUnit 5 / Mockito**

---

## 🔌 API Endpoints

### Credit Advisory Chatbot
* **URL:** `POST /api/underwriting/advise`
* **CORS:** Allowed from all origins (`*`)
* **Request Body:**
```json
{
  "message": "How can I improve my credit score?"
}
```
* **Response (Status 200 OK):**
```json
{
  "response": "To improve your credit score, focus on paying off outstanding credit card debts, keep credit utilization below 30%, and maintain a clean repayment history."
}
```

---

## 🧠 Spring AI Prompt & Structured Output
This service uses Spring AI's `BeanOutputParser` to inject specific JSON formatting rules into the prompt. The LLM evaluates:
1. **DTI Ratio:** Combined monthly debt / net income.
2. **Credit Rating:** Score between 300 and 850.
3. **Interest Pricing:** Custom rates between 8.5% and 15% based on risk.

### 🛡️ Out-of-the-Box Fallback Underwriting
If the environment variable `OPENAI_API_KEY` is not present (or set to `mock-key`), the service **automatically falls back to a deterministic, rule-based algorithm** evaluating the DTI ratio. This allows complete offline local execution without API tokens!

---

## ⚡ Event-Driven Integration (Kafka)
* **Subscriber Topic:** `loan-applications` (Listens to `LoanSubmittedEvent` to trigger automated risk evaluation).
* **Publisher Topic:** `credit-evaluations` (Publishes `CreditEvaluatedEvent` containing credit rating, pricing, and AI risk justifications).

---

## 🚀 Setup & Execution
1. Ensure Zookeeper/Kafka and PostgreSQL are running:
   ```bash
   cd ../infra
   docker-compose up -d
   ```
2. Build and run the project:
   ```bash
   # Option A: Run offline using mock rules
   mvn spring-boot:run
   
   # Option B: Run with real AI LLM integration
   $env:OPENAI_API_KEY="your-api-key"
   mvn spring-boot:run
   ```
