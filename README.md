# TrustBridge — Smart Escrow Orchestration for the Modern Workforce

Freelancers get stiffed. Clients overpay for work that never gets finished. TrustBridge sits between them — locking funds at the start of a job and releasing them only when the work is done. No handshakes. No lawyers. No risk.

---

## What It Does

TrustBridge is a milestone-based escrow platform with intelligent payment routing. Funds are locked when a job starts, released when evidence of completion is submitted, and every transition is enforced by a strict state machine — meaning no money moves unless the conditions are met.

On top of that, TrustBridge automatically selects the cheapest payment rail based on transaction value — so a carpenter handling a £5,000 job isn't losing £100+ to card processing fees when a bank transfer costs a fraction of that.

---

## Dynamic Payment Routing

| Transaction Value | Payment Rail | User Experience | Fee Structure |
|---|---|---|---|
| < £1,000 | Stripe Cards / Wallets | Apple Pay / Google Pay | 1.5% + 20p |
| £1,000 – £20,000 | Open Banking / PISP | Pay by Bank (Instant) | ~1.0% (Capped) |
| > £20,000 | Escrow.com API | Regulated Escrow Partner | Broker Fee (Risk Mitigation) |

The platform selects the rail automatically. The user just pays.

For lower value transactions, frictionless checkout matters more than fee optimisation. Asking a client to set up a bank transfer for a £300 deposit creates unnecessary friction and risks losing the transaction entirely. Apple Pay and Google Pay remove that barrier — the client pays in two taps and the job starts immediately. The slightly higher processing fee at this value is an acceptable trade-off for a seamless experience.

For higher value transactions the calculation flips. At £5,000 the fee difference between card processing and Open Banking can exceed £100 — at that point the friction of a bank transfer is worth it and TrustBridge guides the user accordingly.

---

## Core Features

### State Machine Enforcement
Built on Spring State Machine. Every job moves through a defined lifecycle — `CREATED → FUNDED → IN_PROGRESS → EVIDENCE_SUBMITTED → RELEASED`. Illegal transitions are blocked at the system level. Funds cannot move without cryptographic evidence or mutual consent.

### Milestone-Based Job Structure
Jobs are broken into milestones, each with its own trigger condition. Triggers can be immediate, evidence-based, or webhook-driven from external tools.

```json
{
  "job_id": "job_8821",
  "total_value": 5000,
  "milestones": [
    { "name": "Deposit", "amount": 1000, "trigger": "IMMEDIATE" },
    { "name": "Delivery", "amount": 4000, "trigger": "EVIDENCE_UPLOAD" }
  ]
}
```

### Zero-Trust Evidence Layer
Payouts are triggered by proof of work — file uploads, API webhooks, or manual sign-off. Every piece of evidence is immutably logged against the job it relates to.

### Virtual Account Reconciliation
For high-value transfers, TrustBridge generates a unique virtual IBAN per job, enabling 1:1 automated reconciliation. Powered through Stripe's banking infrastructure.

### Structured Dispute Resolution
When a dispute is raised, both parties submit their proposed payment split. The system then offers three resolution options — accept the other party's proposal, propose your own, or accept the middle ground. If the client accepts the freelancer's proposal outright, that is automatically selected as it favours the more vulnerable party. This process repeats for up to three rounds before escalating to arbitration.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 / Spring Boot 3.2 |
| State Machine | Spring State Machine |
| Database | PostgreSQL 16 (Write-only Audit Ledgers) |
| Payments | Stripe Connect + Escrow.com API |
| Infrastructure | Docker / AWS eu-west-2 (London) |
| Security | OAuth2 / OpenID Connect |

---

## Roadmap

**Phase 1 — UK Domestic MVP**
Stripe Pay-by-Bank integration, full job and milestone lifecycle, state machine enforcement.

**Phase 2 — Evidence API**
Allow external platforms (GitHub, Trello, Procore) to trigger milestone releases via webhook. Completion of a GitHub issue closes a milestone. A Trello card move releases a payment. Work tools become payment triggers.

**Phase 3 — Automated Dispute Resolution**
AI-assisted parsing of uploaded contracts versus submitted evidence to inform and accelerate dispute outcomes.

**Phase 4 — Agency Enterprise Tier**
For agencies operating at scale, TrustBridge will introduce a prepaid credit model allowing upfront deposits of £25,000 to £50,000. Funds at this tier are held by Escrow.com as the regulated entity — TrustBridge acts as the orchestration layer, not the fund holder. By this stage the platform will be running directly on TrueLayer without Stripe as the intermediary, eliminating per-transaction processing costs entirely. This allows TrustBridge to pass significantly discounted rates back to agency clients while maintaining margin through the platform. Agencies get a premium, low-friction experience at reduced cost. This tier is reserved for post-scale once direct TrueLayer infrastructure is fully operational.

---

## Who It's For

- Freelancers and contractors who want deposits protected and payments guaranteed
- Clients who want assurance that funds are only released when work is delivered
- Agencies managing multi-milestone projects across multiple contractors

---

## Access

TrustBridge is currently in private development. Early access will be released in controlled cohorts via invite code. Join the waitlist to be considered for the first release.

**[Join the Waitlist](#)**

---

## Legal

Copyright © 2026 TrustBridge. All rights reserved.

TrustBridge operates under the UK Commercial Agent framework. Funds held within the platform are subject to the terms and conditions of the applicable payment rail provider.
