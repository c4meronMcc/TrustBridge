# TrustBridge

TrustBridge is a secure, milestone-based escrow payment routing platform designed to bridge the trust gap between service providers and clients. Built as a vertical-agnostic infrastructure layer, TrustBridge allows users to lock funds securely via direct bank rails and release them automatically or manually upon successful milestone completion.

Think of it as Stripe, but built from the ground up for secure escrow and programmable payment routing.

## 🚀 Features

- **Vertical-Agnostic Escrow**: Designed to integrate seamlessly across any industry—whether for freelancers, digital marketplaces, or corporate service agreements.
- **Direct Pay-by-Bank Integration**: Eliminates the overhead of virtual IBANs by using direct, open-banking payment rails linked straight to major financial institutions.
- **Milestone-Based Routing**: Funds are tied to specific, measurable project milestones, ensuring transparency and security for both parties.
- **Turnkey & Developer-Friendly**: Built with a clean API architecture, making it easy to drop into existing platforms as a dedicated payment layer.

## 🛠️ Tech Stack & Architecture

TrustBridge is built with high performance, strict types, and robust architectural patterns in mind:

- **Backend**: Java / Spring Boot
- **Architecture**: Event-driven design utilizing State Machines to handle complex, immutable payment lifecycles safely.
- **Payment Rail**: Open Banking APIs (Pay-by-Bank)

## 🔄 How It Works

```
[ Client ] --(1. Initiates Pay-by-Bank)--> [ TrustBridge Escrow State Machine ]
                                                          |
                                            (2. Holds Funds Securely)
                                                          |
[ Provider ] --(3. Completes Milestone)-------------------+
                                                          |
                                            (4. Routes Released Funds)
                                                          v
                                                    [ Provider Bank ]
```

1. **Agreement**: Client and Provider agree on milestones and payment terms.
2. **Funding**: The client funds the milestone securely using a direct pay-by-bank transfer.
3. **State Locked**: TrustBridge locks the funds, moving the transaction state to `FUNDED`.
4. **Handoff / Release**: Once the milestone criteria are met, the state transitions, and funds are instantly routed directly to the provider's bank account.

## 💻 Getting Started

### Prerequisites

- Java 17 or higher
- Maven / Gradle
- Access to your configured Open Banking sandboxes/credentials

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/trustbridge.git
   cd trustbridge
   ```

2. Configure your environment variables in `application.yml` (e.g., database connections, banking API keys).

3. Build and run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

## 🛡️ Security & Compliance

TrustBridge treats transaction states as absolute law. By leveraging a strict backend state machine framework, it prevents race conditions, double-spending, or unauthorized fund releases. All direct bank communication adheres strictly to Open Banking standards.

## 📄 License

Copyright © 2026 Cameron Mccreadie Chaplin. All rights reserved.
