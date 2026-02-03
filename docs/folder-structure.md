# Java Spring Boot: Lean Hybrid Architecture Guide

## 1. Overview

This document defines the architectural standards for the Lean Hybrid Folder Structure. This pattern balances the benefits of Package-by-Feature (encapsulation of business logic) with Package-by-Layer (shared infrastructure and data models).

### Core Philosophy

**Features are the "Actors"**: They contain the "Why" and "How" of business processes.

**Common/Domain are the "Resources"**: They contain the technical tools and data models that the actors use.

## 2. Project Directory Structure

```
src/main/java/com/yourcompany/project/
│
├── common/                         <-- [SHARED INFRASTRUCTURE]
│   ├── config/                     <-- Framework-level @Configuration
│   ├── exception/                  <-- GlobalExceptionHandler, Base exceptions
│   ├── security/                   <-- JWT, AuthFilters, SecurityConfig
│   ├── util/                       <-- Pure technical helpers (Date, String)
│   └── dto/                        <-- Standardized cross-cutting Response objects
│
├── domain/                         <-- [SHARED DATA MODELS]
│   └── entities/                   <-- JPA Entities (Order, User, Product)
│
└── features/                       <-- [BUSINESS LOGIC MODULES]
    ├── orders/
    │   ├── OrderController.java    <-- API Endpoints
    │   ├── OrderService.java       <-- Business Rules & Logic
    │   ├── OrderRepository.java    <-- Database Access for this feature
    │   └── dto/                    <-- Feature-specific request/response objects
    │
    └── billing/
        ├── BillingController.java
        ├── BillingService.java
        ├── BillingRepository.java
        └── dto/
```

## 3. Classification Rules

Use the following rules to decide where a new class belongs:

| Component Type | Classification | Rule / Reason |
|----------------|----------------|---------------|
| JPA Entities | Common (Domain) | Since multiple features (e.g., Orders and Billing) reference the same tables, centralizing Entities prevents Circular Dependencies. |
| Services | Feature Specific | Services hold business rules unique to a feature. They act as the "Bodyguard" of that feature's data. |
| Controllers | Feature Specific | These are the entry points for a specific business flow. |
| Repositories | Feature Specific | Even though they point to shared Entities, the queries are usually specific to what that feature needs to accomplish. |
| DTOs | Feature Specific | If a DTO is a "View" created for a specific API response, keep it with the feature. |
| Security/Auth | Common | Security is a cross-cutting concern that wraps the entire application. |
| Global Exceptions | Common | Standard errors like ResourceNotFound or Unauthorized apply to all features. |

## 4. Communication Rules (The "Service Bodyguard" Pattern)

To keep the architecture maintainable and prevent "Spaghetti Code," all developers must adhere to the following communication flow:

### Rule 1: Service-to-Service Only

If Feature A needs data owned by Feature B, it must call Feature B's Service. It is forbidden to import Feature B's Repository directly.

✅ `ShippingService -> OrderService -> OrderRepository`

❌ `ShippingService -> OrderRepository`

### Rule 2: Entity Visibility

Any Service is allowed to import and return Entities from the shared domain package. However, they should ideally convert these to DTOs before sending them to the Web/Controller layer.

### Rule 3: Repository Scope

A Repository inside a feature folder should only be used by the Service in that same folder. If you find yourself needing the same complex query in two features, consider if that logic belongs in a "Shared Service" or if the query should be duplicated for the sake of decoupling.

## 5. Handling Overlaps

### The "Double Repository" Scenario

When two features use the same Entity (e.g., both orders and billing use the Order entity):

- Create `OrderRepository` in the orders feature for management (Create/Update/Delete).
- Create `BillingOrderRepository` in the billing feature for financial queries (Calculations/Totals).

This keeps the repositories "Lean" and focused only on the feature's requirements.

### The "Service Gap"

If Feature A needs data that Feature B's service doesn't provide:

- **Do not bypass the service.**
- Add a new method to Feature B's service to expose the necessary data.

This ensures that any security or validation logic in Feature B is applied to the new data request.

## 6. Benefits of this Structure

**Navigability**: New developers can find all logic related to "Orders" in one folder.

**Safety**: Centralized entities prevent the most common Spring Boot startup error: the Circular Dependency loop.

**Refactor-ability**: Because features are encapsulated, moving a feature (like billing) into its own Microservice is significantly easier.