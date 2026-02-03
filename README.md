<div align="center">

  <p>&nbsp;</p>

  <img src="assets/LogoAndName.png" alt="TrustBridge Logo" width="300">

  <br/>

### The Hybrid Payment Router for the Construction Industry

[![CI/CD Pipeline](https://img.shields.io/badge/Build-Passing-success?style=for-the-badge&logo=github)](https://github.com/your-repo/actions)
[![Security Audit](https://img.shields.io/badge/Security-A%2B-blue?style=for-the-badge&logo=veracode)](https://veracode.com)
[![Stack](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=for-the-badge&logo=spring-boot)](https://spring.io)
[![License](https://img.shields.io/badge/License-Proprietary-red?style=for-the-badge)](#)

<p align="center">
  <a href="#-executive-summary">Summary</a> •
  <a href="#-system-architecture">Architecture</a> •
  <a href="#-security--compliance">Security</a> •
  <a href="#-quick-start-local-development">Quick Start</a>
</p>

</div>

---

## 📖 Executive Summary

**TrustBridge** is a vertical fintech platform designed to solve the cash-flow latency crisis in the UK construction sector.

Unlike generic payment gateways, TrustBridge operates as a **Smart Payment Router**. It dynamically routes transactions based on risk, volume, and geography to optimize for **margin** (via Open Banking) and **compliance** (via regulated Escrow partners).

**Core Value Proposition:**
* **Cost Efficiency:** **1%** transaction fees for domestic payments (vs. 2.9% industry standard).
* **Risk Mitigation:** "No-Code" Escrow workflows that protect both Contractor and Client.
* **Vertical Logic:** Built-in milestone management and evidence-based release triggers.

---

## 🏗 System Architecture

TrustBridge employs a **Hybrid Routing Engine** to switch between payment rails dynamically.

```mermaid
graph TD
    %% Actors
    User([Client / Payer]) --> API[TrustBridge API Gateway]

    %% The Core Decision Engine
    subgraph "Core Decision Engine"
        API --> Router{Transaction Router}
        Router -- "UK Domestic & < £25k" --> PathA[Tier A: Open Banking]
        Router -- "International / Cards" --> PathB[Tier B: Global Card Rail]
        Router -- "High Value (> £25k)" --> PathC[Tier C: Regulated Escrow]
    end

    %% The Rails
    subgraph "Financial Rails"
        PathA --> TrueLayer[TrueLayer API]
        PathB --> Stripe[Stripe Connect]
        PathC --> Escrow[Escrow.com API]
    end

    %% Settlement
    TrueLayer --> TB_Merchant[TB Merchant Acct]
    Stripe --> TB_Connect[Stripe Connected Acct]
    Escrow --> Wire[Wire Transfer Vault]
```

### Routing Logic (Proprietary)

The system automatically selects the compliance and fee structure based on the user's geolocation and transaction size.

| Tier | Condition | Infrastructure Rail | Fee Structure | Compliance Model |
|------|-----------|-------------------|---------------|------------------|
| Tier A | UK Domestic (GBP) | TrueLayer (PISP) | 1.0% (Platform Fee) | Commercial Agent (FCA Exemption) |
| Tier B | International (USD/EUR) | Stripe Connect | 5.0% (Surcharge) | Marketplace Facilitator |
| Tier C | High Value (> £25k) | Escrow.com | Broker Fee | Fully Regulated Escrow Partner |

---

## 🔒 Security & Compliance

TrustBridge adheres to strict financial compliance standards.

### 🛡️ Regulatory Status

- **UK Operations:** Operates under the Commercial Agent Exemption (The Electronic Money Regulations 2011). TrustBridge acts strictly as the commercial agent authorized to negotiate or conclude the sale of services on behalf of the Payee (Freelancer).
- **Data Sovereignty:** All UK user data is residency-locked to eu-west-2 (London).

### 🔐 Technical Security Measures

- **Zero-Trust Payouts:** Funds cannot be released without cryptographically signed approval from the Client OR a Dispute Resolution verdict.
- **Immutable Audit Logs:** All state transitions (e.g., FUNDED -> RELEASED) are recorded in a write-only ledger (transactions table).
- **State Machine Enforcement:** Financial states are managed by Spring State Machine to prevent illegal transitions (e.g., preventing a "Draft" job from triggering a "Payout").

---

## 🛠 Technology Stack

Designed for ACID Compliance, High Availability, and Auditability.

<table align="center">
<tr>
<td align="center" width="96">
<img src="https://user-images.githubusercontent.com/25181517/117201156-9a724800-adec-11eb-9a9d-3cd0f67da4bc.png" width="48" alt="Java" /><br>Java 21
</td>
<td align="center" width="96">
<img src="https://user-images.githubusercontent.com/25181517/183890595-779a7e64-3f43-4631-b083-3d35c701f194.png" width="48" alt="Spring Boot" /><br>Spring Boot 3
</td>
<td align="center" width="96">
<img src="https://user-images.githubusercontent.com/25181517/117208740-bfb78400-adf5-11eb-97bb-0907296cf0a5.png" width="48" alt="PostgreSQL" /><br>PostgreSQL 16
</td>
<td align="center" width="96">
<img src="https://user-images.githubusercontent.com/25181517/117207330-263a0280-adf4-11eb-9b97-0ac5b40bc3db.png" width="48" alt="Docker" /><br>Docker
</td>
</tr>
</table>

- **Core Framework:** Spring Boot 3.2 (Web, Data JPA, Security)
- **Database Migration:** Flyway
- **Testing:** JUnit 5, Testcontainers, Mockito
- **Integration:** OpenFeign (for external Banking APIs)

---

## 🚀 Quick Start (Local Development)

### Prerequisites

- ☕ Java 21 or higher
- 🐳 Docker & Docker Compose
- 🔑 TrueLayer Sandbox Keys (Contact Admin)

### Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/trustbridge-io/core-platform.git
cd core-platform
```

#### 2. Initialize Infrastructure

```bash
# Spins up PostgreSQL and Redis containers
docker-compose up -d
```

#### 3. Configure Environment

Create a `.env` file in the root directory:

```ini
TRUELAYER_CLIENT_ID=your_id
TRUELAYER_CLIENT_SECRET=your_secret
DB_URL=jdbc:postgresql://localhost:5432/trustbridge
```

#### 4. Run Application

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

---

## 🗺️ Roadmap

### Phase 1: UK Domestic MVP (Current)

- [x] Core Banking Ledger (jobs, milestones, ledgers).
- [x] TrueLayer Payment Initiation (PISP) integration.
- [ ] Evidence Upload System (S3 Integration).
- [ ] Dispute Resolution Dashboard.

### Phase 2: International Expansion (Future)

- [ ] Stripe Connect (Express) implementation.
- [ ] Multi-currency support (USD, EUR).
- [ ] Automated Tax Calculation (VAT/Sales Tax).

---

## 📄 License & Proprietary Notice

**Copyright © 2025 TrustBridge Financial Ltd.**  
All rights reserved. This software is proprietary and confidential. Unauthorized copying, transfer, or use of this file, via any medium, is strictly prohibited.

TrustBridge Ltd is a company registered in England and Wales.

---

<div align="center">
<sub>Built with precision in London, UK 🇬🇧</sub>
</div>
<br>