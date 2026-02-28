# TrustBridge: The Smart Escrow Orchestrator

TrustBridge is a high-integrity payment routing platform that automates trust between Payers and Payees. It functions as a State-Machine-as-a-Service, ensuring funds are only released when milestones are met, while dynamically routing transactions to minimize fees and risk.

## 🏗 System Logic & Architecture

TrustBridge acts as an intelligent middleman, selecting the most cost-effective and secure payment rail based on transaction value.

### 💸 Dynamic Routing Strategy

| Transaction Value | Payment Rail | User Experience | Fee Structure |
|-------------------|--------------|-----------------|---------------|
| < £1,000 | Stripe Cards / Wallets | Apple Pay / Google Pay | 1.5% + 20p (Convenience) |
| £1,000 - £20,000 | Open Banking / PISP | "Pay by Bank" (Instant) | ~1.0% (Capped) |
| > £20,000 | Escrow.com API | Regulated Escrow Partner | Broker Fee (Risk Mitigation) |

## 🔒 Core Features

- **State Machine Enforcement**: Built on Spring State Machine, preventing illegal financial transitions. Funds cannot move from LOCKED to RELEASED without cryptographic evidence or mutual consent.

- **Commercial Agent Model**: Operates under the UK Commercial Agent Exemption, acting as the authorized negotiator for the Payee to keep regulatory overhead lean.

- **Zero-Trust Evidence**: Payouts are triggered by "Proof of Work" (file uploads, API webhooks, or manual sign-off) which are immutably logged.

- **Virtual Account Reconciliation**: For high-value transfers, TrustBridge generates unique virtual IBANs for every job, enabling 1:1 automated reconciliation.

## 🛠 Tech Stack

- **Backend**: Java 21 / Spring Boot 3.2 (ACID Compliant)
- **Persistence**: PostgreSQL 16 (Write-only Audit Ledgers)
- **Payments**: Stripe Connect (Standard) + Escrow.com API
- **Infrastructure**: Docker / AWS eu-west-2 (London)
- **Security**: OAuth2 / OpenID Connect

## 🚀 Quick Start for Developers

### 1. Define a Job State

The platform revolves around the Job Lifecycle. Define your milestones via our API:

```json
{
  "job_id": "job_8821",
  "total_value": 5000,
  "milestones": [
    {"name": "Deposit", "amount": 1000, "trigger": "IMMEDIATE"},
    {"name": "Delivery", "amount": 4000, "trigger": "EVIDENCE_UPLOAD"}
  ]
}
```

### 2. Implementation

TrustBridge will automatically generate the correct Payment Link:

- If the milestone is £500, the link enables Apple Pay.
- If the milestone is £5,000, the link forces Open Banking to save the freelancer £100+ in fees.

## 🗺 Roadmap

- **Phase 1**: UK Domestic MVP (Stripe Pay-by-Bank & State Machine).
- **Phase 2**: "Evidence API" – Allow external apps (GitHub, Procore, Trello) to trigger payment releases.
- **Phase 3**: Automated Dispute Resolution – AI-assisted parsing of contracts vs. uploaded evidence.

## 📄 Proprietary Notice

Copyright © 2026 TrustBridge Financial Ltd. Registered in England and Wales.

*Building the infrastructure for the future of decentralized work.*
