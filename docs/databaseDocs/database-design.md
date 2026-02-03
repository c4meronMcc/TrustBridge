# TrustBridge Database Design Documentation

**Version**: 0.1.0-MVP
**Last Updated**: 2025-01-17
**Status**: Planning Phase

---

## Table of Contents

1. [Overview](#overview)
2. [Design Principles](#design-principles)
3. [Core Entities](#core-entities)
4. [Table Schemas](#table-schemas)
5. [Relationships & ERD](#relationships--erd)
6. [Business Rules](#business-rules)
7. [Future Enhancements](#future-enhancements)

---

## Overview

TrustBridge's database is designed to support an escrow payment platform for freelancers. The schema handles:

- **User Management**: Freelancers and clients with flexible role switching
- **Job Lifecycle**: From creation to completion with dual approval
- **Milestone Payments**: Optional incremental payment tracking
- **Escrow Integration**: Tracking escrow.com API transactions
- **Dispute Resolution**: Handling payment disputes and arbitration

### Database Technology
- **RDBMS**: PostgreSQL 16
- **Migration Tool**: Flyway
- **ORM**: Spring Data JPA / Hibernate

---

## Design Principles

### 1. Financial Data Integrity
- Use `DECIMAL(19, 4)` for all monetary values (precision critical)
- Soft deletes only (never hard delete financial records)
- Immutable transaction logs (escrow_transactions table)

### 2. Audit & Compliance
- `created_at` and `updated_at` on every table
- Soft delete with `deleted_at` timestamp
- Store full API responses in JSONB for debugging/compliance

### 3. Flexibility & Extensibility
- JSONB columns for semi-structured data (attachments, metadata)
- Status enums for clear state machines
- UUID tokens for secure payment links

### 4. Performance
- Proper indexes on foreign keys and frequently queried columns
- Denormalization where appropriate (calculated fields)
- Efficient queries for dashboard/reporting

### 5. Security
- BCrypt password hashing
- UUID tokens for payment links (not sequential IDs)
- Rate limiting data (failed login attempts)

---

## Core Entities

### MVP Tables (Phase 1)
1. **users** - Authentication and basic user info
2. **jobs** - Work projects between freelancers and clients
3. **milestones** - Optional payment checkpoints within jobs
4. **payment_requests** - Generated payment links sent to clients
5. **escrow_transactions** - Tracks escrow.com API interactions
6. **job_completions** - Dual approval workflow tracking
7. **disputes** - Conflict resolution and arbitration

### Future Tables (Phase 2+)
- **freelancer_profiles** - Extended freelancer info (portfolio, rates, bio)
- **client_profiles** - Extended client info (company, industry)
- **reviews** - Ratings and feedback system
- **notifications** - Email/SMS tracking
- **audit_logs** - Full change history for compliance
- **escrow_webhooks** - Webhook event log from escrow.com
- **categories** - Job categorization (plumbing, web dev, etc.)

---

## Table Schemas

### 1. users - Core Identity & Authentication

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,

  -- Authentication
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL, -- BCrypt hashed

  -- OAuth Support (future)
  oauth_provider VARCHAR(50), -- GOOGLE, GITHUB, LINKEDIN, NULL for email/password
  oauth_provider_id VARCHAR(255), -- ID from OAuth provider

  -- Email Verification
  email_verified BOOLEAN DEFAULT FALSE,
  email_verification_token VARCHAR(255),
  email_verification_expires_at TIMESTAMP,

  -- Password Reset (future)
  reset_token VARCHAR(255),
  reset_token_expires_at TIMESTAMP,

  -- User Type & Status
  user_type VARCHAR(20) NOT NULL DEFAULT 'CLIENT', -- FREELANCER, CLIENT, BOTH
  status VARCHAR(50) DEFAULT 'PENDING_VERIFICATION',
    -- PENDING_VERIFICATION, ACTIVE, SUSPENDED, BANNED, BRUTE_FORCE_LOCKED

  -- Profile Information
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  business_name VARCHAR(255), -- Optional for freelancers
  phone_number VARCHAR(20), -- E.164 format recommended
  country_code VARCHAR(3), -- ISO 3166-1 alpha-3
  timezone VARCHAR(50), -- IANA timezone

  -- Security & Audit
  failed_login_attempts INTEGER DEFAULT 0,
  last_failed_login_at TIMESTAMP,
  last_login_at TIMESTAMP,
  brute_force_locked_until TIMESTAMP, -- NULL = not locked

  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP -- Soft delete
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
```

#### Business Rules:
- **User Role Switching**: Users can be `BOTH` freelancer and client
- **Client Dashboard**: Clients have minimal dashboard - primarily email-based approval workflow
- **Brute Force Protection**: After 5 failed login attempts, account locked for 1 hour
  - User receives email notification of lock
  - Email contains "Was this you?" link
  - If not them, prompt password reset
- **Email Verification**: Required before first use (development can be simplified)
- **Password Complexity**: 8 chars minimum, 1 number, 1 special char, 1 uppercase (enforce in app layer)

---

### 2. jobs - Work Projects

```sql
CREATE TABLE jobs (
  id BIGSERIAL PRIMARY KEY,

  -- Relationships
  freelancer_id BIGINT NOT NULL REFERENCES users(id),
  client_id BIGINT NOT NULL REFERENCES users(id),
  parent_job_id BIGINT REFERENCES jobs(id), -- For job templates/repeats

  -- Job Details
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,

  -- Pricing
  total_amount DECIMAL(19, 4) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'GBP', -- ISO 4217
  payment_type VARCHAR(20) DEFAULT 'MILESTONE',
    -- FULL_UPFRONT, MILESTONE, HOURLY (future), PARTIAL_UPFRONT
  partial_upfront_amount DECIMAL(19, 4), -- If payment_type = PARTIAL_UPFRONT

  -- Job Lifecycle
  status VARCHAR(50) DEFAULT 'DRAFT',
    -- DRAFT, PENDING_PAYMENT, PAYMENT_RECEIVED, IN_PROGRESS,
    -- PENDING_COMPLETION, COMPLETED, CANCELLED, DISPUTED, REFUNDED

  -- Dates
  estimated_start_date DATE,
  estimated_completion_date DATE,
  actual_start_date DATE,
  actual_completion_date DATE,

  -- Additional Information
  contract_terms TEXT, -- Agreement terms, scope, etc.
  attachments_json JSONB,
    -- { "files": [{"url": "...", "name": "...", "type": "..."}] }

  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_jobs_freelancer_id ON jobs(freelancer_id);
CREATE INDEX idx_jobs_client_id ON jobs(client_id);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_created_at ON jobs(created_at);
CREATE INDEX idx_jobs_deleted_at ON jobs(deleted_at);
```

#### Business Rules:
- **Job States**: Clear state machine (see lifecycle diagram below)
- **Payment Types**:
  - `FULL_UPFRONT`: Client pays entire amount before work starts
  - `MILESTONE`: Payment released per milestone completion
  - `PARTIAL_UPFRONT`: Deposit + milestone/final payment
  - `HOURLY`: **Future feature** - requires time tracking mechanism
- **Hourly Work Challenge**: Escrow typically requires fixed amounts. Potential solutions:
  - **Option A**: Weekly/biweekly escrow holds (client pays estimated hours upfront)
  - **Option B**: Running tab with periodic settlements (client approves timesheets)
  - **Option C**: Maximum budget cap (client deposits max, unused returns)
  - **Recommendation**: Start with fixed-price only, add hourly in Phase 2
- **No Team Jobs**: MVP supports 1 freelancer, 1 client per job (simplicity)

#### Job Lifecycle State Machine:

```
DRAFT -> PENDING_PAYMENT -> PAYMENT_RECEIVED -> IN_PROGRESS ->
  PENDING_COMPLETION -> COMPLETED

  | (anytime)
  v
  CANCELLED

  | (from IN_PROGRESS or PENDING_COMPLETION)
  v
  DISPUTED -> (resolved) -> COMPLETED or REFUNDED
```

---

### 3. milestones - Payment Checkpoints

```sql
CREATE TABLE milestones (
  id BIGSERIAL PRIMARY KEY,

  -- Relationships
  job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
  parent_milestone_id BIGINT REFERENCES milestones(id), -- Future: sub-milestones

  -- Milestone Details
  title VARCHAR(255) NOT NULL,
  description TEXT,
  sequence_order INTEGER NOT NULL, -- 1, 2, 3...

  -- Pricing
  amount DECIMAL(19, 4) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'GBP',

  -- Status & Approval
  status VARCHAR(50) DEFAULT 'PENDING',
    -- PENDING, IN_PROGRESS, COMPLETED_BY_FREELANCER,
    -- APPROVED_BY_CLIENT, PAID, SKIPPED, DISPUTED

  -- Deliverables
  deliverables_json JSONB,
    -- { "items": [{"url": "...", "description": "...", "uploaded_at": "..."}] }

  -- Dates
  due_date DATE,
  freelancer_completed_at TIMESTAMP,
  client_approved_at TIMESTAMP,
  paid_at TIMESTAMP,

  -- Dependencies (future)
  depends_on_milestone_id BIGINT REFERENCES milestones(id),

  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_milestones_job_id ON milestones(job_id);
CREATE INDEX idx_milestones_status ON milestones(status);
CREATE INDEX idx_milestones_sequence_order ON milestones(job_id, sequence_order);
```

#### Business Rules:
- **Optional**: Jobs can have 0 milestones (full upfront payment)
- **Flexible Ordering**: Milestones can be completed out of order
- **Editable**: Can add/edit milestones after job creation (requires mutual agreement)
- **Skipping Milestones**: Allowed, but skipped milestone = no payment for that milestone
  - If freelancer wants payment for skipped milestone, must negotiate with client (outside platform)
- **Dual Approval**: Both freelancer AND client must approve each milestone
  - Freelancer marks as `COMPLETED_BY_FREELANCER`
  - Client reviews and marks as `APPROVED_BY_CLIENT`
  - System releases payment and marks as `PAID`
- **Sub-milestones**: Interesting idea, but **NOT MVP** (too complex)

---

### 4. payment_requests - Payment Links

```sql
CREATE TABLE payment_requests (
  id BIGSERIAL PRIMARY KEY,

  -- Relationships
  job_id BIGINT NOT NULL REFERENCES jobs(id),
  milestone_id BIGINT REFERENCES milestones(id), -- NULL = full job payment

  -- Payment Link
  payment_link_token UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),

  -- Amount
  amount DECIMAL(19, 4) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'GBP',

  -- Status
  status VARCHAR(50) DEFAULT 'PENDING',
    -- PENDING, PAID, EXPIRED, CANCELLED

  -- Payment Method
  payment_method VARCHAR(50) DEFAULT 'ESCROW_COM',
    -- ESCROW_COM, STRIPE (future), PAYPAL (future)

  -- Tracking
  view_count INTEGER DEFAULT 0,
  last_viewed_at TIMESTAMP,
  reminder_sent_count INTEGER DEFAULT 0,
  last_reminder_sent_at TIMESTAMP,

  -- Dates
  expires_at TIMESTAMP NOT NULL, -- Default: 7 days from creation
  paid_at TIMESTAMP,

  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_payment_requests_job_id ON payment_requests(job_id);
CREATE INDEX idx_payment_requests_milestone_id ON payment_requests(milestone_id);
CREATE INDEX idx_payment_requests_token ON payment_requests(payment_link_token);
CREATE INDEX idx_payment_requests_status ON payment_requests(status);
CREATE INDEX idx_payment_requests_expires_at ON payment_requests(expires_at);
```

---

### 5. escrow_transactions - External API Tracking

```sql
CREATE TABLE escrow_transactions (
  id BIGSERIAL PRIMARY KEY,

  -- Relationships
  payment_request_id BIGINT NOT NULL REFERENCES payment_requests(id),

  -- Escrow.com Data
  escrow_transaction_id VARCHAR(255) UNIQUE NOT NULL, -- From escrow.com API
  escrow_status VARCHAR(50) DEFAULT 'PENDING',
    -- PENDING, HELD, RELEASED, REFUNDED, DISPUTED, CANCELLED

  -- Amounts
  amount DECIMAL(19, 4) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  escrow_fee DECIMAL(19, 4), -- Escrow.com's fee
  net_amount DECIMAL(19, 4), -- Amount after fees

  -- API Response Storage
  escrow_api_response JSONB, -- Full API response for debugging

  -- Dates
  escrow_created_at TIMESTAMP,
  escrow_updated_at TIMESTAMP,
  escrow_held_at TIMESTAMP, -- When funds confirmed in escrow
  escrow_released_at TIMESTAMP, -- When funds sent to freelancer
  escrow_refunded_at TIMESTAMP, -- When funds returned to client

  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_escrow_transactions_payment_request_id ON escrow_transactions(payment_request_id);
CREATE INDEX idx_escrow_transactions_escrow_transaction_id ON escrow_transactions(escrow_transaction_id);
CREATE INDEX idx_escrow_transactions_status ON escrow_transactions(escrow_status);
```

---

### 6. job_completions - Dual Approval Workflow

```sql
CREATE TABLE job_completions (
  id BIGSERIAL PRIMARY KEY,

  -- Relationships
  job_id BIGINT UNIQUE NOT NULL REFERENCES jobs(id), -- One completion per job

  -- Freelancer Approval
  freelancer_approved BOOLEAN DEFAULT FALSE,
  freelancer_approved_at TIMESTAMP,
  freelancer_notes TEXT,

  -- Client Approval
  client_approved BOOLEAN DEFAULT FALSE,
  client_approved_at TIMESTAMP,
  client_notes TEXT,

  -- Completion Evidence
  completion_evidence_json JSONB,
    -- { "photos": [...], "documents": [...], "links": [...] }

  -- Client Feedback (optional, for future reviews)
  client_rating INTEGER CHECK (client_rating >= 1 AND client_rating <= 5),
  client_feedback TEXT,

  -- Both Approved
  both_approved_at TIMESTAMP, -- Populated when both = TRUE

  -- Auto-Approval (future feature)
  auto_approval_date TIMESTAMP, -- If no response by this date, auto-approve
  auto_approved BOOLEAN DEFAULT FALSE,

  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_job_completions_job_id ON job_completions(job_id);
```

#### Business Rules:
- **Per-Job Completion**: One record per job (not per milestone)
- **Dual Approval Required**: Both parties must approve
  - Freelancer marks work complete -> `freelancer_approved = TRUE`
  - Client reviews -> `client_approved = TRUE`
  - System sets `both_approved_at` and triggers payment release
- **Retraction**: Either party can retract approval BEFORE the other approves
  - Once both approved, no retraction (creates `both_approved_at` timestamp)
- **Auto-Approval**: If client doesn't respond within X days (e.g., 7 days), auto-approve
  - Send reminders at day 3, 5, 6
  - On day 7, if no response, auto-approve and release funds
  - Prevents freelancers being held hostage by unresponsive clients
- **Dispute Override**: If one party disputes after other approves, goes to dispute resolution
- **Completion Evidence**: Freelancer can upload photos, documents, links as proof of work
  - Useful for dispute arbitration

---

### 7. disputes - Conflict Resolution

```sql
CREATE TABLE disputes (
  id BIGSERIAL PRIMARY KEY,

  -- Relationships
  job_id BIGINT NOT NULL REFERENCES jobs(id),
  milestone_id BIGINT REFERENCES milestones(id), -- NULL if disputing entire job

  -- Dispute Origin
  raised_by_user_id BIGINT NOT NULL REFERENCES users(id), -- Who raised it
  dispute_type VARCHAR(50) NOT NULL,
    -- QUALITY_ISSUE, NON_DELIVERY, PAYMENT_ISSUE, SCOPE_CREEP, OTHER

  -- Details
  reason TEXT NOT NULL, -- Why dispute was raised
  evidence_json JSONB,
    -- { "files": [...], "screenshots": [...], "messages": [...] }

  -- Status & Resolution
  status VARCHAR(50) DEFAULT 'OPEN',
    -- OPEN, UNDER_REVIEW, RESOLVED, ESCALATED, CLOSED
  resolution VARCHAR(50),
    -- REFUND_CLIENT, PAY_FREELANCER, PARTIAL_REFUND_50_50,
    -- PARTIAL_REFUND_CUSTOM, NO_ACTION
  resolution_notes TEXT,
  partial_refund_amount DECIMAL(19, 4), -- If PARTIAL_REFUND_CUSTOM

  -- Resolution
  resolved_at TIMESTAMP,
  resolved_by_user_id BIGINT REFERENCES users(id), -- Admin who resolved (future)

  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_disputes_job_id ON disputes(job_id);
CREATE INDEX idx_disputes_raised_by_user_id ON disputes(raised_by_user_id);
CREATE INDEX idx_disputes_status ON disputes(status);
CREATE INDEX idx_disputes_created_at ON disputes(created_at);
```

#### Business Rules:
- **Who Can Raise**: **Clients only** (primary use case in MVP)
- **When Can Raise**: After work is submitted for approval
- **Resolution**: Escrow.com handles arbitration (recommended)
- **Evidence**: Allow file uploads (screenshots, contracts, messages)
- **Communication Tracking**: Store all dispute-related messages in evidence_json

---

## Relationships & ERD

### Entity Relationship Diagram (Text)

```
users (1) ----< (N) jobs [as freelancer]
users (1) ----< (N) jobs [as client]
users (1) ----< (N) disputes [raised_by]

jobs (1) ----< (N) milestones
jobs (1) ----< (N) payment_requests
jobs (1) ---- (1) job_completions
jobs (1) ----< (N) disputes

milestones (1) ----< (N) payment_requests

payment_requests (1) ---- (1) escrow_transactions
```

---

## Business Rules Summary

### User Management
1. Users can be both freelancer AND client (role switching)
2. Clients have minimal dashboard - primarily email-based workflow
3. Email verification required before first use
4. Brute force protection: 5 failed logins = 1 hour lockout
5. OAuth support (Google, GitHub, LinkedIn) alongside email/password

### Job Lifecycle
1. Jobs start as DRAFT -> becomes PENDING_PAYMENT when freelancer sends payment link
2. Once client pays -> PAYMENT_RECEIVED -> freelancer can start work (IN_PROGRESS)
3. When work done -> freelancer marks PENDING_COMPLETION
4. Both parties approve -> COMPLETED -> funds released
5. Auto-approval after 7 days if client doesn't respond
6. Either party can cancel anytime -> CANCELLED -> refund issued

### Milestone Payments
1. Milestones are optional (jobs can be full upfront payment)
2. Can complete milestones out of order
3. Skipping milestone = no payment for that milestone
4. Dual approval required: freelancer completes, client approves
5. Each milestone has its own payment request/escrow transaction

### Payment & Escrow
1. All payments go through escrow.com (MVP)
2. Payment links expire after 7 days (configurable)
3. Clients can pay via embeddable widget OR email link
4. Multi-currency support (ISO 4217 codes)
5. Escrow fees tracked separately from net amount
6. Funds released only after dual approval

### Disputes
1. Clients can raise disputes (freelancers cannot in MVP)
2. Disputes override pending approvals
3. Evidence uploaded (screenshots, contracts, messages)
4. Resolution options: full refund, full payment, partial splits
5. Escrow.com handles arbitration (recommended)

### Security
1. BCrypt password hashing
2. UUID tokens for payment links (not sequential IDs)
3. Soft deletes only (never hard delete financial data)
4. Full API response logging for compliance
5. Audit timestamps on all tables

---

## Future Enhancements (Phase 2+)

### Planned Tables:

1. **freelancer_profiles** - Extended freelancer data
2. **client_profiles** - Extended client data
3. **reviews** - Ratings & feedback system
4. **notifications** - Email/SMS tracking
5. **audit_logs** - Full change history
6. **escrow_webhooks** - Webhook event log
7. **categories** - Job categorization

### Planned Features:

- Hourly rate support with time tracking
- Team jobs with multiple freelancers
- Recurring jobs / job templates
- Advanced notifications (SMS, push)
- Reputation system
- Analytics dashboard

---

**End of Database Design Document v0.1.0**

*This is a living document. Update as schema evolves.*
