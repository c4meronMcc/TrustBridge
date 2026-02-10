[//]: # (# TrustBridge Database Design Documentation)

[//]: # ()
[//]: # (**Version**: 0.1.0-MVP)

[//]: # (**Last Updated**: 2025-01-17)

[//]: # (**Status**: Planning Phase)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Table of Contents)

[//]: # ()
[//]: # (1. [Overview]&#40;#overview&#41;)

[//]: # (2. [Design Principles]&#40;#design-principles&#41;)

[//]: # (3. [Core Entities]&#40;#core-entities&#41;)

[//]: # (4. [Table Schemas]&#40;#table-schemas&#41;)

[//]: # (5. [Relationships & ERD]&#40;#relationships--erd&#41;)

[//]: # (6. [Business Rules]&#40;#business-rules&#41;)

[//]: # (7. [Future Enhancements]&#40;#future-enhancements&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Overview)

[//]: # ()
[//]: # (TrustBridge's database is designed to support an escrow payment platform for freelancers. The schema handles:)

[//]: # ()
[//]: # (- **User Management**: Freelancers and clients with flexible role switching)

[//]: # (- **Job Lifecycle**: From creation to completion with dual approval)

[//]: # (- **Milestone Payments**: Optional incremental payment tracking)

[//]: # (- **Escrow Integration**: Tracking escrow.com API transactions)

[//]: # (- **Dispute Resolution**: Handling payment disputes and arbitration)

[//]: # ()
[//]: # (### Database Technology)

[//]: # (- **RDBMS**: PostgreSQL 16)

[//]: # (- **Migration Tool**: Flyway)

[//]: # (- **ORM**: Spring Data JPA / Hibernate)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Design Principles)

[//]: # ()
[//]: # (### 1. Financial Data Integrity)

[//]: # (- Use `DECIMAL&#40;19, 4&#41;` for all monetary values &#40;precision critical&#41;)

[//]: # (- Soft deletes only &#40;never hard delete financial records&#41;)

[//]: # (- Immutable transaction logs &#40;escrow_transactions table&#41;)

[//]: # ()
[//]: # (### 2. Audit & Compliance)

[//]: # (- `created_at` and `updated_at` on every table)

[//]: # (- Soft delete with `deleted_at` timestamp)

[//]: # (- Store full API responses in JSONB for debugging/compliance)

[//]: # ()
[//]: # (### 3. Flexibility & Extensibility)

[//]: # (- JSONB columns for semi-structured data &#40;attachments, metadata&#41;)

[//]: # (- Status enums for clear state machines)

[//]: # (- UUID tokens for secure payment links)

[//]: # ()
[//]: # (### 4. Performance)

[//]: # (- Proper indexes on foreign keys and frequently queried columns)

[//]: # (- Denormalization where appropriate &#40;calculated fields&#41;)

[//]: # (- Efficient queries for dashboard/reporting)

[//]: # ()
[//]: # (### 5. Security)

[//]: # (- BCrypt password hashing)

[//]: # (- UUID tokens for payment links &#40;not sequential IDs&#41;)

[//]: # (- Rate limiting data &#40;failed login attempts&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Core Entities)

[//]: # ()
[//]: # (### MVP Tables &#40;Phase 1&#41;)

[//]: # (1. **users** - Authentication and basic user info)

[//]: # (2. **jobs** - Work projects between freelancers and clients)

[//]: # (3. **milestones** - Optional payment checkpoints within jobs)

[//]: # (4. **payment_requests** - Generated payment links sent to clients)

[//]: # (5. **escrow_transactions** - Tracks escrow.com API interactions)

[//]: # (6. **job_completions** - Dual approval workflow tracking)

[//]: # (7. **disputes** - Conflict resolution and arbitration)

[//]: # ()
[//]: # (### Future Tables &#40;Phase 2+&#41;)

[//]: # (- **freelancer_profiles** - Extended freelancer info &#40;portfolio, rates, bio&#41;)

[//]: # (- **client_profiles** - Extended client info &#40;company, industry&#41;)

[//]: # (- **reviews** - Ratings and feedback system)

[//]: # (- **notifications** - Email/SMS tracking)

[//]: # (- **audit_logs** - Full change history for compliance)

[//]: # (- **escrow_webhooks** - Webhook event log from escrow.com)

[//]: # (- **categories** - Job categorization &#40;plumbing, web dev, etc.&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Table Schemas)

[//]: # ()
[//]: # (### 1. users - Core Identity & Authentication)

[//]: # ()
[//]: # ()
[//]: # (```sql)

[//]: # (CREATE TABLE users &#40;)

[//]: # (  id BIGSERIAL PRIMARY KEY,)

[//]: # ()
[//]: # (  -- Authentication)

[//]: # (  email VARCHAR&#40;255&#41; UNIQUE NOT NULL,)

[//]: # (  password_hash VARCHAR&#40;255&#41; NOT NULL, -- BCrypt hashed)

[//]: # ()
[//]: # (  -- OAuth Support &#40;future&#41;)

[//]: # (  oauth_provider VARCHAR&#40;50&#41;, -- GOOGLE, GITHUB, LINKEDIN, NULL for email/password)

[//]: # (  oauth_provider_id VARCHAR&#40;255&#41;, -- ID from OAuth provider)

[//]: # ()
[//]: # (  -- Email Verification)

[//]: # (  email_verified BOOLEAN DEFAULT FALSE,)

[//]: # (  email_verification_token VARCHAR&#40;255&#41;,)

[//]: # (  email_verification_expires_at TIMESTAMP,)

[//]: # ()
[//]: # (  -- Password Reset &#40;future&#41;)

[//]: # (  reset_token VARCHAR&#40;255&#41;,)

[//]: # (  reset_token_expires_at TIMESTAMP,)

[//]: # ()
[//]: # (  -- User Type & Status)

[//]: # (  user_type VARCHAR&#40;20&#41; NOT NULL DEFAULT 'CLIENT', -- FREELANCER, CLIENT, BOTH)

[//]: # (  status VARCHAR&#40;50&#41; DEFAULT 'PENDING_VERIFICATION',)

[//]: # (    -- PENDING_VERIFICATION, ACTIVE, SUSPENDED, BANNED, BRUTE_FORCE_LOCKED)

[//]: # ()
[//]: # (  -- Profile Information)

[//]: # (  first_name VARCHAR&#40;100&#41; NOT NULL,)

[//]: # (  last_name VARCHAR&#40;100&#41; NOT NULL,)

[//]: # (  business_name VARCHAR&#40;255&#41;, -- Optional for freelancers)

[//]: # (  phone_number VARCHAR&#40;20&#41;, -- E.164 format recommended)

[//]: # (  country_code VARCHAR&#40;3&#41;, -- ISO 3166-1 alpha-3)

[//]: # (  timezone VARCHAR&#40;50&#41;, -- IANA timezone)

[//]: # ()
[//]: # (  -- Security & Audit)

[//]: # (  failed_login_attempts INTEGER DEFAULT 0,)

[//]: # (  last_failed_login_at TIMESTAMP,)

[//]: # (  last_login_at TIMESTAMP,)

[//]: # (  brute_force_locked_until TIMESTAMP, -- NULL = not locked)

[//]: # ()
[//]: # (  -- Timestamps)

[//]: # (  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  deleted_at TIMESTAMP -- Soft delete)

[//]: # (&#41;;)

[//]: # ()
[//]: # (-- Indexes)

[//]: # (CREATE INDEX idx_users_email ON users&#40;email&#41;;)

[//]: # (CREATE INDEX idx_users_status ON users&#40;status&#41;;)

[//]: # (CREATE INDEX idx_users_user_type ON users&#40;user_type&#41;;)

[//]: # (CREATE INDEX idx_users_deleted_at ON users&#40;deleted_at&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (#### Business Rules:)

[//]: # (- **User Role Switching**: Users can be `BOTH` freelancer and client)

[//]: # (- **Client Dashboard**: Clients have minimal dashboard - primarily email-based approval workflow)

[//]: # (- **Brute Force Protection**: After 5 failed login attempts, account locked for 1 hour)

[//]: # (  - User receives email notification of lock)

[//]: # (  - Email contains "Was this you?" link)

[//]: # (  - If not them, prompt password reset)

[//]: # (- **Email Verification**: Required before first use &#40;development can be simplified&#41;)

[//]: # (- **Password Complexity**: 8 chars minimum, 1 number, 1 special char, 1 uppercase &#40;enforce in app layer&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### 2. jobs - Work Projects)

[//]: # ()
[//]: # (```sql)

[//]: # (CREATE TABLE jobs &#40;)

[//]: # (  id BIGSERIAL PRIMARY KEY,)

[//]: # ()
[//]: # (  -- Relationships)

[//]: # (  freelancer_id BIGINT NOT NULL REFERENCES users&#40;id&#41;,)

[//]: # (  client_id BIGINT NOT NULL REFERENCES users&#40;id&#41;,)

[//]: # (  parent_job_id BIGINT REFERENCES jobs&#40;id&#41;, -- For job templates/repeats)

[//]: # ()
[//]: # (  -- Job Details)

[//]: # (  title VARCHAR&#40;255&#41; NOT NULL,)

[//]: # (  description TEXT NOT NULL,)

[//]: # ()
[//]: # (  -- Pricing)

[//]: # (  total_amount DECIMAL&#40;19, 4&#41; NOT NULL,)

[//]: # (  currency VARCHAR&#40;3&#41; NOT NULL DEFAULT 'GBP', -- ISO 4217)

[//]: # (  payment_type VARCHAR&#40;20&#41; DEFAULT 'MILESTONE',)

[//]: # (    -- FULL_UPFRONT, MILESTONE, HOURLY &#40;future&#41;, PARTIAL_UPFRONT)

[//]: # (  partial_upfront_amount DECIMAL&#40;19, 4&#41;, -- If payment_type = PARTIAL_UPFRONT)

[//]: # ()
[//]: # (  -- Job Lifecycle)

[//]: # (  status VARCHAR&#40;50&#41; DEFAULT 'DRAFT',)

[//]: # (    -- DRAFT, PENDING_PAYMENT, PAYMENT_RECEIVED, IN_PROGRESS,)

[//]: # (    -- PENDING_COMPLETION, COMPLETED, CANCELLED, DISPUTED, REFUNDED)

[//]: # ()
[//]: # (  -- Dates)

[//]: # (  estimated_start_date DATE,)

[//]: # (  estimated_completion_date DATE,)

[//]: # (  actual_start_date DATE,)

[//]: # (  actual_completion_date DATE,)

[//]: # ()
[//]: # (  -- Additional Information)

[//]: # (  contract_terms TEXT, -- Agreement terms, scope, etc.)

[//]: # (  attachments_json JSONB,)

[//]: # (    -- { "files": [{"url": "...", "name": "...", "type": "..."}] })

[//]: # ()
[//]: # (  -- Timestamps)

[//]: # (  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  deleted_at TIMESTAMP)

[//]: # (&#41;;)

[//]: # ()
[//]: # (-- Indexes)

[//]: # (CREATE INDEX idx_jobs_freelancer_id ON jobs&#40;freelancer_id&#41;;)

[//]: # (CREATE INDEX idx_jobs_client_id ON jobs&#40;client_id&#41;;)

[//]: # (CREATE INDEX idx_jobs_status ON jobs&#40;status&#41;;)

[//]: # (CREATE INDEX idx_jobs_created_at ON jobs&#40;created_at&#41;;)

[//]: # (CREATE INDEX idx_jobs_deleted_at ON jobs&#40;deleted_at&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (#### Business Rules:)

[//]: # (- **Job States**: Clear state machine &#40;see lifecycle diagram below&#41;)

[//]: # (- **Payment Types**:)

[//]: # (  - `FULL_UPFRONT`: Client pays entire amount before work starts)

[//]: # (  - `MILESTONE`: Payment released per milestone completion)

[//]: # (  - `PARTIAL_UPFRONT`: Deposit + milestone/final payment)

[//]: # (  - `HOURLY`: **Future feature** - requires time tracking mechanism)

[//]: # (- **Hourly Work Challenge**: Escrow typically requires fixed amounts. Potential solutions:)

[//]: # (  - **Option A**: Weekly/biweekly escrow holds &#40;client pays estimated hours upfront&#41;)

[//]: # (  - **Option B**: Running tab with periodic settlements &#40;client approves timesheets&#41;)

[//]: # (  - **Option C**: Maximum budget cap &#40;client deposits max, unused returns&#41;)

[//]: # (  - **Recommendation**: Start with fixed-price only, add hourly in Phase 2)

[//]: # (- **No Team Jobs**: MVP supports 1 freelancer, 1 client per job &#40;simplicity&#41;)

[//]: # ()
[//]: # (#### Job Lifecycle State Machine:)

[//]: # ()
[//]: # (```)

[//]: # (DRAFT -> PENDING_PAYMENT -> PAYMENT_RECEIVED -> IN_PROGRESS ->)

[//]: # (  PENDING_COMPLETION -> COMPLETED)

[//]: # ()
[//]: # (  | &#40;anytime&#41;)

[//]: # (  v)

[//]: # (  CANCELLED)

[//]: # ()
[//]: # (  | &#40;from IN_PROGRESS or PENDING_COMPLETION&#41;)

[//]: # (  v)

[//]: # (  DISPUTED -> &#40;resolved&#41; -> COMPLETED or REFUNDED)

[//]: # (```)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### 3. milestones - Payment Checkpoints)

[//]: # ()
[//]: # (```sql)

[//]: # (CREATE TABLE milestones &#40;)

[//]: # (  id BIGSERIAL PRIMARY KEY,)

[//]: # ()
[//]: # (  -- Relationships)

[//]: # (  job_id BIGINT NOT NULL REFERENCES jobs&#40;id&#41; ON DELETE CASCADE,)

[//]: # (  parent_milestone_id BIGINT REFERENCES milestones&#40;id&#41;, -- Future: sub-milestones)

[//]: # ()
[//]: # (  -- Milestone Details)

[//]: # (  title VARCHAR&#40;255&#41; NOT NULL,)

[//]: # (  description TEXT,)

[//]: # (  sequence_order INTEGER NOT NULL, -- 1, 2, 3...)

[//]: # ()
[//]: # (  -- Pricing)

[//]: # (  amount DECIMAL&#40;19, 4&#41; NOT NULL,)

[//]: # (  currency VARCHAR&#40;3&#41; NOT NULL DEFAULT 'GBP',)

[//]: # ()
[//]: # (  -- Status & Approval)

[//]: # (  status VARCHAR&#40;50&#41; DEFAULT 'PENDING',)

[//]: # (    -- PENDING, IN_PROGRESS, COMPLETED_BY_FREELANCER,)

[//]: # (    -- APPROVED_BY_CLIENT, PAID, SKIPPED, DISPUTED)

[//]: # ()
[//]: # (  -- Deliverables)

[//]: # (  deliverables_json JSONB,)

[//]: # (    -- { "items": [{"url": "...", "description": "...", "uploaded_at": "..."}] })

[//]: # ()
[//]: # (  -- Dates)

[//]: # (  due_date DATE,)

[//]: # (  freelancer_completed_at TIMESTAMP,)

[//]: # (  client_approved_at TIMESTAMP,)

[//]: # (  paid_at TIMESTAMP,)

[//]: # ()
[//]: # (  -- Dependencies &#40;future&#41;)

[//]: # (  depends_on_milestone_id BIGINT REFERENCES milestones&#40;id&#41;,)

[//]: # ()
[//]: # (  -- Timestamps)

[//]: # (  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  deleted_at TIMESTAMP)

[//]: # (&#41;;)

[//]: # ()
[//]: # (-- Indexes)

[//]: # (CREATE INDEX idx_milestones_job_id ON milestones&#40;job_id&#41;;)

[//]: # (CREATE INDEX idx_milestones_status ON milestones&#40;status&#41;;)

[//]: # (CREATE INDEX idx_milestones_sequence_order ON milestones&#40;job_id, sequence_order&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (#### Business Rules:)

[//]: # (- **Optional**: Jobs can have 0 milestones &#40;full upfront payment&#41;)

[//]: # (- **Flexible Ordering**: Milestones can be completed out of order)

[//]: # (- **Editable**: Can add/edit milestones after job creation &#40;requires mutual agreement&#41;)

[//]: # (- **Skipping Milestones**: Allowed, but skipped milestone = no payment for that milestone)

[//]: # (  - If freelancer wants payment for skipped milestone, must negotiate with client &#40;outside platform&#41;)

[//]: # (- **Dual Approval**: Both freelancer AND client must approve each milestone)

[//]: # (  - Freelancer marks as `COMPLETED_BY_FREELANCER`)

[//]: # (  - Client reviews and marks as `APPROVED_BY_CLIENT`)

[//]: # (  - System releases payment and marks as `PAID`)

[//]: # (- **Sub-milestones**: Interesting idea, but **NOT MVP** &#40;too complex&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### 4. payment_requests - Payment Links)

[//]: # ()
[//]: # (```sql)

[//]: # (CREATE TABLE payment_requests &#40;)

[//]: # (  id BIGSERIAL PRIMARY KEY,)

[//]: # ()
[//]: # (  -- Relationships)

[//]: # (  job_id BIGINT NOT NULL REFERENCES jobs&#40;id&#41;,)

[//]: # (  milestone_id BIGINT REFERENCES milestones&#40;id&#41;, -- NULL = full job payment)

[//]: # ()
[//]: # (  -- Payment Link)

[//]: # (  payment_link_token UUID UNIQUE NOT NULL DEFAULT gen_random_uuid&#40;&#41;,)

[//]: # ()
[//]: # (  -- Amount)

[//]: # (  amount DECIMAL&#40;19, 4&#41; NOT NULL,)

[//]: # (  currency VARCHAR&#40;3&#41; NOT NULL DEFAULT 'GBP',)

[//]: # ()
[//]: # (  -- Status)

[//]: # (  status VARCHAR&#40;50&#41; DEFAULT 'PENDING',)

[//]: # (    -- PENDING, PAID, EXPIRED, CANCELLED)

[//]: # ()
[//]: # (  -- Payment Method)

[//]: # (  payment_method VARCHAR&#40;50&#41; DEFAULT 'ESCROW_COM',)

[//]: # (    -- ESCROW_COM, STRIPE &#40;future&#41;, PAYPAL &#40;future&#41;)

[//]: # ()
[//]: # (  -- Tracking)

[//]: # (  view_count INTEGER DEFAULT 0,)

[//]: # (  last_viewed_at TIMESTAMP,)

[//]: # (  reminder_sent_count INTEGER DEFAULT 0,)

[//]: # (  last_reminder_sent_at TIMESTAMP,)

[//]: # ()
[//]: # (  -- Dates)

[//]: # (  expires_at TIMESTAMP NOT NULL, -- Default: 7 days from creation)

[//]: # (  paid_at TIMESTAMP,)

[//]: # ()
[//]: # (  -- Timestamps)

[//]: # (  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

[//]: # (&#41;;)

[//]: # ()
[//]: # (-- Indexes)

[//]: # (CREATE INDEX idx_payment_requests_job_id ON payment_requests&#40;job_id&#41;;)

[//]: # (CREATE INDEX idx_payment_requests_milestone_id ON payment_requests&#40;milestone_id&#41;;)

[//]: # (CREATE INDEX idx_payment_requests_token ON payment_requests&#40;payment_link_token&#41;;)

[//]: # (CREATE INDEX idx_payment_requests_status ON payment_requests&#40;status&#41;;)

[//]: # (CREATE INDEX idx_payment_requests_expires_at ON payment_requests&#40;expires_at&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### 5. escrow_transactions - External API Tracking)

[//]: # ()
[//]: # (```sql)

[//]: # (CREATE TABLE escrow_transactions &#40;)

[//]: # (  id BIGSERIAL PRIMARY KEY,)

[//]: # ()
[//]: # (  -- Relationships)

[//]: # (  payment_request_id BIGINT NOT NULL REFERENCES payment_requests&#40;id&#41;,)

[//]: # ()
[//]: # (  -- Escrow.com Data)

[//]: # (  escrow_transaction_id VARCHAR&#40;255&#41; UNIQUE NOT NULL, -- From escrow.com API)

[//]: # (  escrow_status VARCHAR&#40;50&#41; DEFAULT 'PENDING',)

[//]: # (    -- PENDING, HELD, RELEASED, REFUNDED, DISPUTED, CANCELLED)

[//]: # ()
[//]: # (  -- Amounts)

[//]: # (  amount DECIMAL&#40;19, 4&#41; NOT NULL,)

[//]: # (  currency VARCHAR&#40;3&#41; NOT NULL,)

[//]: # (  escrow_fee DECIMAL&#40;19, 4&#41;, -- Escrow.com's fee)

[//]: # (  net_amount DECIMAL&#40;19, 4&#41;, -- Amount after fees)

[//]: # ()
[//]: # (  -- API Response Storage)

[//]: # (  escrow_api_response JSONB, -- Full API response for debugging)

[//]: # ()
[//]: # (  -- Dates)

[//]: # (  escrow_created_at TIMESTAMP,)

[//]: # (  escrow_updated_at TIMESTAMP,)

[//]: # (  escrow_held_at TIMESTAMP, -- When funds confirmed in escrow)

[//]: # (  escrow_released_at TIMESTAMP, -- When funds sent to freelancer)

[//]: # (  escrow_refunded_at TIMESTAMP, -- When funds returned to client)

[//]: # ()
[//]: # (  -- Timestamps)

[//]: # (  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

[//]: # (&#41;;)

[//]: # ()
[//]: # (-- Indexes)

[//]: # (CREATE INDEX idx_escrow_transactions_payment_request_id ON escrow_transactions&#40;payment_request_id&#41;;)

[//]: # (CREATE INDEX idx_escrow_transactions_escrow_transaction_id ON escrow_transactions&#40;escrow_transaction_id&#41;;)

[//]: # (CREATE INDEX idx_escrow_transactions_status ON escrow_transactions&#40;escrow_status&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### 6. job_completions - Dual Approval Workflow)

[//]: # ()
[//]: # (```sql)

[//]: # (CREATE TABLE job_completions &#40;)

[//]: # (  id BIGSERIAL PRIMARY KEY,)

[//]: # ()
[//]: # (  -- Relationships)

[//]: # (  job_id BIGINT UNIQUE NOT NULL REFERENCES jobs&#40;id&#41;, -- One completion per job)

[//]: # ()
[//]: # (  -- Freelancer Approval)

[//]: # (  freelancer_approved BOOLEAN DEFAULT FALSE,)

[//]: # (  freelancer_approved_at TIMESTAMP,)

[//]: # (  freelancer_notes TEXT,)

[//]: # ()
[//]: # (  -- Client Approval)

[//]: # (  client_approved BOOLEAN DEFAULT FALSE,)

[//]: # (  client_approved_at TIMESTAMP,)

[//]: # (  client_notes TEXT,)

[//]: # ()
[//]: # (  -- Completion Evidence)

[//]: # (  completion_evidence_json JSONB,)

[//]: # (    -- { "photos": [...], "documents": [...], "links": [...] })

[//]: # ()
[//]: # (  -- Client Feedback &#40;optional, for future reviews&#41;)

[//]: # (  client_rating INTEGER CHECK &#40;client_rating >= 1 AND client_rating <= 5&#41;,)

[//]: # (  client_feedback TEXT,)

[//]: # ()
[//]: # (  -- Both Approved)

[//]: # (  both_approved_at TIMESTAMP, -- Populated when both = TRUE)

[//]: # ()
[//]: # (  -- Auto-Approval &#40;future feature&#41;)

[//]: # (  auto_approval_date TIMESTAMP, -- If no response by this date, auto-approve)

[//]: # (  auto_approved BOOLEAN DEFAULT FALSE,)

[//]: # ()
[//]: # (  -- Timestamps)

[//]: # (  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

[//]: # (&#41;;)

[//]: # ()
[//]: # (-- Indexes)

[//]: # (CREATE INDEX idx_job_completions_job_id ON job_completions&#40;job_id&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (#### Business Rules:)

[//]: # (- **Per-Job Completion**: One record per job &#40;not per milestone&#41;)

[//]: # (- **Dual Approval Required**: Both parties must approve)

[//]: # (  - Freelancer marks work complete -> `freelancer_approved = TRUE`)

[//]: # (  - Client reviews -> `client_approved = TRUE`)

[//]: # (  - System sets `both_approved_at` and triggers payment release)

[//]: # (- **Retraction**: Either party can retract approval BEFORE the other approves)

[//]: # (  - Once both approved, no retraction &#40;creates `both_approved_at` timestamp&#41;)

[//]: # (- **Auto-Approval**: If client doesn't respond within X days &#40;e.g., 7 days&#41;, auto-approve)

[//]: # (  - Send reminders at day 3, 5, 6)

[//]: # (  - On day 7, if no response, auto-approve and release funds)

[//]: # (  - Prevents freelancers being held hostage by unresponsive clients)

[//]: # (- **Dispute Override**: If one party disputes after other approves, goes to dispute resolution)

[//]: # (- **Completion Evidence**: Freelancer can upload photos, documents, links as proof of work)

[//]: # (  - Useful for dispute arbitration)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### 7. disputes - Conflict Resolution)

[//]: # ()
[//]: # (```sql)

[//]: # (CREATE TABLE disputes &#40;)

[//]: # (  id BIGSERIAL PRIMARY KEY,)

[//]: # ()
[//]: # (  -- Relationships)

[//]: # (  job_id BIGINT NOT NULL REFERENCES jobs&#40;id&#41;,)

[//]: # (  milestone_id BIGINT REFERENCES milestones&#40;id&#41;, -- NULL if disputing entire job)

[//]: # ()
[//]: # (  -- Dispute Origin)

[//]: # (  raised_by_user_id BIGINT NOT NULL REFERENCES users&#40;id&#41;, -- Who raised it)

[//]: # (  dispute_type VARCHAR&#40;50&#41; NOT NULL,)

[//]: # (    -- QUALITY_ISSUE, NON_DELIVERY, PAYMENT_ISSUE, SCOPE_CREEP, OTHER)

[//]: # ()
[//]: # (  -- Details)

[//]: # (  reason TEXT NOT NULL, -- Why dispute was raised)

[//]: # (  evidence_json JSONB,)

[//]: # (    -- { "files": [...], "screenshots": [...], "messages": [...] })

[//]: # ()
[//]: # (  -- Status & Resolution)

[//]: # (  status VARCHAR&#40;50&#41; DEFAULT 'OPEN',)

[//]: # (    -- OPEN, UNDER_REVIEW, RESOLVED, ESCALATED, CLOSED)

[//]: # (  resolution VARCHAR&#40;50&#41;,)

[//]: # (    -- REFUND_CLIENT, PAY_FREELANCER, PARTIAL_REFUND_50_50,)

[//]: # (    -- PARTIAL_REFUND_CUSTOM, NO_ACTION)

[//]: # (  resolution_notes TEXT,)

[//]: # (  partial_refund_amount DECIMAL&#40;19, 4&#41;, -- If PARTIAL_REFUND_CUSTOM)

[//]: # ()
[//]: # (  -- Resolution)

[//]: # (  resolved_at TIMESTAMP,)

[//]: # (  resolved_by_user_id BIGINT REFERENCES users&#40;id&#41;, -- Admin who resolved &#40;future&#41;)

[//]: # ()
[//]: # (  -- Timestamps)

[//]: # (  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,)

[//]: # (  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

[//]: # (&#41;;)

[//]: # ()
[//]: # (-- Indexes)

[//]: # (CREATE INDEX idx_disputes_job_id ON disputes&#40;job_id&#41;;)

[//]: # (CREATE INDEX idx_disputes_raised_by_user_id ON disputes&#40;raised_by_user_id&#41;;)

[//]: # (CREATE INDEX idx_disputes_status ON disputes&#40;status&#41;;)

[//]: # (CREATE INDEX idx_disputes_created_at ON disputes&#40;created_at&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (#### Business Rules:)

[//]: # (- **Who Can Raise**: **Clients only** &#40;primary use case in MVP&#41;)

[//]: # (- **When Can Raise**: After work is submitted for approval)

[//]: # (- **Resolution**: Escrow.com handles arbitration &#40;recommended&#41;)

[//]: # (- **Evidence**: Allow file uploads &#40;screenshots, contracts, messages&#41;)

[//]: # (- **Communication Tracking**: Store all dispute-related messages in evidence_json)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Relationships & ERD)

[//]: # ()
[//]: # (### Entity Relationship Diagram &#40;Text&#41;)

[//]: # ()
[//]: # (```)

[//]: # (users &#40;1&#41; ----< &#40;N&#41; jobs [as freelancer])

[//]: # (users &#40;1&#41; ----< &#40;N&#41; jobs [as client])

[//]: # (users &#40;1&#41; ----< &#40;N&#41; disputes [raised_by])

[//]: # ()
[//]: # (jobs &#40;1&#41; ----< &#40;N&#41; milestones)

[//]: # (jobs &#40;1&#41; ----< &#40;N&#41; payment_requests)

[//]: # (jobs &#40;1&#41; ---- &#40;1&#41; job_completions)

[//]: # (jobs &#40;1&#41; ----< &#40;N&#41; disputes)

[//]: # ()
[//]: # (milestones &#40;1&#41; ----< &#40;N&#41; payment_requests)

[//]: # ()
[//]: # (payment_requests &#40;1&#41; ---- &#40;1&#41; escrow_transactions)

[//]: # (```)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Business Rules Summary)

[//]: # ()
[//]: # (### User Management)

[//]: # (1. Users can be both freelancer AND client &#40;role switching&#41;)

[//]: # (2. Clients have minimal dashboard - primarily email-based workflow)

[//]: # (3. Email verification required before first use)

[//]: # (4. Brute force protection: 5 failed logins = 1 hour lockout)

[//]: # (5. OAuth support &#40;Google, GitHub, LinkedIn&#41; alongside email/password)

[//]: # ()
[//]: # (### Job Lifecycle)

[//]: # (1. Jobs start as DRAFT -> becomes PENDING_PAYMENT when freelancer sends payment link)

[//]: # (2. Once client pays -> PAYMENT_RECEIVED -> freelancer can start work &#40;IN_PROGRESS&#41;)

[//]: # (3. When work done -> freelancer marks PENDING_COMPLETION)

[//]: # (4. Both parties approve -> COMPLETED -> funds released)

[//]: # (5. Auto-approval after 7 days if client doesn't respond)

[//]: # (6. Either party can cancel anytime -> CANCELLED -> refund issued)

[//]: # ()
[//]: # (### Milestone Payments)

[//]: # (1. Milestones are optional &#40;jobs can be full upfront payment&#41;)

[//]: # (2. Can complete milestones out of order)

[//]: # (3. Skipping milestone = no payment for that milestone)

[//]: # (4. Dual approval required: freelancer completes, client approves)

[//]: # (5. Each milestone has its own payment request/escrow transaction)

[//]: # ()
[//]: # (### Payment & Escrow)

[//]: # (1. All payments go through escrow.com &#40;MVP&#41;)

[//]: # (2. Payment links expire after 7 days &#40;configurable&#41;)

[//]: # (3. Clients can pay via embeddable widget OR email link)

[//]: # (4. Multi-currency support &#40;ISO 4217 codes&#41;)

[//]: # (5. Escrow fees tracked separately from net amount)

[//]: # (6. Funds released only after dual approval)

[//]: # ()
[//]: # (### Disputes)

[//]: # (1. Clients can raise disputes &#40;freelancers cannot in MVP&#41;)

[//]: # (2. Disputes override pending approvals)

[//]: # (3. Evidence uploaded &#40;screenshots, contracts, messages&#41;)

[//]: # (4. Resolution options: full refund, full payment, partial splits)

[//]: # (5. Escrow.com handles arbitration &#40;recommended&#41;)

[//]: # ()
[//]: # (### Security)

[//]: # (1. BCrypt password hashing)

[//]: # (2. UUID tokens for payment links &#40;not sequential IDs&#41;)

[//]: # (3. Soft deletes only &#40;never hard delete financial data&#41;)

[//]: # (4. Full API response logging for compliance)

[//]: # (5. Audit timestamps on all tables)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Future Enhancements &#40;Phase 2+&#41;)

[//]: # ()
[//]: # (### Planned Tables:)

[//]: # ()
[//]: # (1. **freelancer_profiles** - Extended freelancer data)

[//]: # (2. **client_profiles** - Extended client data)

[//]: # (3. **reviews** - Ratings & feedback system)

[//]: # (4. **notifications** - Email/SMS tracking)

[//]: # (5. **audit_logs** - Full change history)

[//]: # (6. **escrow_webhooks** - Webhook event log)

[//]: # (7. **categories** - Job categorization)

[//]: # ()
[//]: # (### Planned Features:)

[//]: # ()
[//]: # (- Hourly rate support with time tracking)

[//]: # (- Team jobs with multiple freelancers)

[//]: # (- Recurring jobs / job templates)

[//]: # (- Advanced notifications &#40;SMS, push&#41;)

[//]: # (- Reputation system)

[//]: # (- Analytics dashboard)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (**End of Database Design Document v0.1.0**)

[//]: # ()
[//]: # (*This is a living document. Update as schema evolves.*)
