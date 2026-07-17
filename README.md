# TrustBridge: Agnostic Escrow Orchestrator

TrustBridge is a vertical-agnostic, milestone-based escrow payment routing layer. Engineered to sit between service providers and clients, it ensures funds are securely held and programmatically released only when pre-defined, mutually agreed-upon milestones are met.

Rather than relying on manual payment verification, TrustBridge utilizes strict state-machine logic and Stripe's virtual IBAN infrastructure to automate secure, low-fee transactions for complex B2B and freelance agreements.

## 🚀 Key Features

* **Hierarchical State Machine (HSM) Orchestration:** Payment milestones are strictly governed by state machines, preventing invalid workflow transitions (e.g., releasing funds before a milestone is cryptographically signed off).
* **Virtual IBAN Payment Routing:** Integrates deeply with Stripe using virtual IBANs to dramatically lower transaction fees compared to standard credit card processing, optimizing for high-volume escrow holds.
* **Database Webhooks & Integrity:** Utilizes real-time database webhooks to synchronize payment states between Stripe and the local database, ensuring high data consistency and zero double-spends.
* **Vertical-Agnostic Design:** Built as a headless orchestration layer, allowing seamless integration into any industry that requires milestone-based trust (freelance, software development, supply chain, etc.).

## 🛠️ Architecture & Tech Stack

* **Backend core:** Java 21 & Spring Boot
* **Frontend interface:** Next.js (React)
* **Database:** PostgreSQL / MariaDB
* **Payments/API:** Stripe API & Webhooks
* **State Management:** Custom Java Finite State Machine implementation

## 🧠 Engineering Decisions & Business Logic

When engineering TrustBridge, a primary constraint was minimizing transaction costs for users holding large sums in escrow. Standard payment processors take a high percentage-based cut. By engineering the backend to leverage **virtual IBANs**, TrustBridge mimics localized bank transfers, capping fees and making the escrow model financially viable for users.

Furthermore, the application logic is fully decoupled from the UI. The Spring Boot backend acts as an agnostic API, meaning the Next.js frontend can be entirely swapped or embedded directly into a third-party client's existing software via API keys.

## ⚙️ Local Setup & Development

### Prerequisites

* Java 21+
* Node.js (for Next.js frontend)
* PostgreSQL / MariaDB instance running locally
* Stripe Developer Account (for API testing keys)

### Backend (Spring Boot) Setup

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/trustbridge.git
   ```
2. Navigate to the backend directory:
   ```
   cd trustbridge-api
   ```
3. Configure your local environment variables in `src/main/resources/application-dev.yml`:
   ```yaml
   spring.datasource.url: jdbc:postgresql://localhost:5432/trustbridge
   stripe.api.key: sk_test_...
   stripe.webhook.secret: whsec_...
   ```
4. Build the project:
   ```
   ./gradlew build
   ```
   or
   ```
   mvn clean install
   ```
5. Run the application:
   ```
   ./gradlew bootRun
   ```

### Frontend (Next.js) Setup

1. Navigate to the web directory:
   ```
   cd trustbridge-web
   ```
2. Install dependencies:
   ```
   npm install
   ```
3. Configure your `.env.local` file with the backend API URL.
4. Run the development server:
   ```
   npm run dev
   ```

## 🧪 Testing

The backend is highly covered by unit and integration tests, particularly around the state machine transitions and webhook idempotency.

Run the test suite via:
```
./gradlew test
```
or
```
mvn test
```

---

Developed by Cameron Mccreadie Chaplin. For professional inquiries regarding backend architecture or Spring Boot development, feel free to reach out.
