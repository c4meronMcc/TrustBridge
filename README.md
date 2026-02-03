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

# Scope
**The Algorithmic Payment Protocol**

Scope is a vertical fintech infrastructure platform that bridges the "Trust Gap" in the high-value gig economy. It replaces subjective human arbitration with objective, API-driven verification — automating the secure release of funds for developers and freelancers through programmatic escrow governed by code, not opinion.

---

## The Problem

Freelance and service-economy transactions suffer from a fundamental lack of trust infrastructure, creating two failure modes on every side of the transaction:

**Deposit Friction** — Clients are hesitant to pay upfront deposits to strangers, fearing the work will never be delivered.

**Payment Latency** — Freelancers are forced onto "Net-30" or "Net-60" terms, effectively acting as an interest-free bank for their clients, with no guarantee of final payment.

Traditional solutions fall short. Direct bank transfers offer no protection. Legacy escrow services are expensive, slow, and rely on manual human arbitration to resolve disputes.

---

## How It Works

Scope acts as a **State-Aware Payment Router**, sitting between the client and the freelancer. It enforces two core mechanisms:

### Sequential Funding

Rather than requiring a large upfront deposit, Scope uses an **Atomic Milestone** architecture to keep risk low for both parties:

- Projects are broken into granular deliverables on maximum 30-day cycles.
- The client funds only the currently active milestone.
- Neither party ever has more than a defined amount at risk at any given time.

### Digital Witnesses

Scope replaces manual review with API-driven verification tailored to the type of work:

- **For developers:** A Pull Request merged into the target branch on GitHub acts as cryptographic proof of delivery, automatically triggering fund release.
- **For creatives:** A file upload starts a 72-hour "Passive Trust" window. If the client does not contest the submission, funds are released by default.

---

## Technical Architecture

Scope is engineered to meet enterprise financial standards, with a focus on ACID compliance, strict type safety, and full auditability.

### Backend — Java Spring Boot

The core application is built on **Java 21** and **Spring Boot 3.2**. Java's strict type system is a deliberate choice — it prevents the floating-point errors that are common in JavaScript-based financial systems.

### Finite State Machine (FSM)

All ledger state transitions are governed by a strict FSM, acting as the central logic gatekeeper to prevent illegal financial operations:

```
DRAFT → LOCKED → IN_PROGRESS → VERIFYING → PAID
```

A project cannot reach `PAID` unless it has successfully passed through both `LOCKED` and `VERIFYING`, effectively eliminating race-condition bugs.

### Infrastructure & Security

| Layer              | Technology / Approach                                                                 |
|--------------------|---------------------------------------------------------------------------------------|
| Database           | PostgreSQL 16 (relational, fully transactional)                                       |
| Payments           | Stripe Connect API with Express Accounts for fund routing, KYC, and AML compliance   |
| Webhook Security   | HMAC-SHA256 signature verification on all external signals (GitHub, Stripe, etc.)     |
| Idempotency        | Redis-based Event ID caching to prevent double-spending on network retries            |

---

## Roadmap

| Phase | Status   | Scope                                                                  |
|-------|----------|------------------------------------------------------------------------|
| 1     | ✅ Done   | Core Ledger Architecture, User Authentication (Spring Security), Project Definition APIs |
| 2     | 🔄 Active | Sequential Funding logic, Stripe Connect integration                   |
| 3     | 📋 Up next | Digital Witness GitHub webhook listener, Beta launch for local developer cohorts |

---

## Tech Stack

- **Runtime:** Java 21
- **Framework:** Spring Boot 3.2
- **Database:** PostgreSQL 16
- **Cache:** Redis
- **Payments:** Stripe Connect
- **Auth:** Spring Security
- **Verification:** GitHub Webhooks (HMAC-SHA256)

---

*Built in Cardiff, United Kingdom.*