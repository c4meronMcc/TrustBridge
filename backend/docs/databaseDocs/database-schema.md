// TrustBridge Database Schema
// Version: 0.1.0-MVP
// Last Updated: 2025-01-17
//
// Paste this into https://dbdiagram.io to generate visual ER diagram

Project TrustBridge {
  database_type: 'PostgreSQL'
  Note: 'Escrow payment platform for freelancers - MVP Schema'
}

// ========================================
// CORE TABLES
// ========================================

Table users {
  id bigserial [pk, increment]

  // Authentication
  email varchar(255) [unique, not null]
  password_hash varchar(255) [not null]
  oauth_provider varchar(50) [null, note: 'GOOGLE, GITHUB, LINKEDIN']
  oauth_provider_id varchar(255) [null]

  // Email Verification
  email_verified boolean [default: false]
  email_verification_token varchar(255)
  email_verification_expires_at timestamp

  // Password Reset
  reset_token varchar(255)
  reset_token_expires_at timestamp

  // User Type & Status
  user_type varchar(20) [not null, default: 'CLIENT', note: 'FREELANCER, CLIENT, BOTH']
  status varchar(50) [default: 'PENDING_VERIFICATION', note: 'PENDING_VERIFICATION, ACTIVE, SUSPENDED, BANNED, BRUTE_FORCE_LOCKED']

  // Profile
  first_name varchar(100) [not null]
  last_name varchar(100) [not null]
  business_name varchar(255) [null]
  phone_number varchar(20)
  country_code varchar(3)
  timezone varchar(50)

  // Security
  failed_login_attempts integer [default: 0]
  last_failed_login_at timestamp
  last_login_at timestamp
  brute_force_locked_until timestamp

  // Timestamps
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [default: `CURRENT_TIMESTAMP`]
  deleted_at timestamp [null, note: 'Soft delete']

  indexes {
    email
    status
    user_type
    deleted_at
  }
}

Table jobs {
  id bigserial [pk, increment]

  // Relationships
  freelancer_id bigint [not null, ref: > users.id]
  client_id bigint [not null, ref: > users.id]
  parent_job_id bigint [null, ref: > jobs.id, note: 'For job templates']

  // Job Details
  title varchar(255) [not null]
  description text [not null]

  // Pricing
  total_amount decimal(19,4) [not null]
  currency varchar(3) [not null, default: 'GBP']
  payment_type varchar(20) [default: 'MILESTONE', note: 'FULL_UPFRONT, MILESTONE, HOURLY, PARTIAL_UPFRONT']
  partial_upfront_amount decimal(19,4) [null]

  // Status
  status varchar(50) [default: 'DRAFT', note: 'DRAFT, PENDING_PAYMENT, PAYMENT_RECEIVED, IN_PROGRESS, PENDING_COMPLETION, COMPLETED, CANCELLED, DISPUTED, REFUNDED']

  // Dates
  estimated_start_date date
  estimated_completion_date date
  actual_start_date date
  actual_completion_date date

  // Additional
  contract_terms text
  attachments_json jsonb [note: 'File URLs, names, types']

  // Timestamps
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [default: `CURRENT_TIMESTAMP`]
  deleted_at timestamp [null]

  indexes {
    freelancer_id
    client_id
    status
    created_at
    deleted_at
  }
}

Table milestones {
  id bigserial [pk, increment]

  // Relationships
  job_id bigint [not null, ref: > jobs.id, note: 'Cascade delete']
  parent_milestone_id bigint [null, ref: > milestones.id, note: 'Future: sub-milestones']

  // Details
  title varchar(255) [not null]
  description text
  sequence_order integer [not null]

  // Pricing
  amount decimal(19,4) [not null]
  currency varchar(3) [not null, default: 'GBP']

  // Status
  status varchar(50) [default: 'PENDING', note: 'PENDING, IN_PROGRESS, COMPLETED_BY_FREELANCER, APPROVED_BY_CLIENT, PAID, SKIPPED, DISPUTED']

  // Deliverables
  deliverables_json jsonb [note: 'URLs, descriptions, upload timestamps']

  // Dates
  due_date date
  freelancer_completed_at timestamp
  client_approved_at timestamp
  paid_at timestamp

  // Dependencies (future)
  depends_on_milestone_id bigint [null, ref: > milestones.id]

  // Timestamps
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [default: `CURRENT_TIMESTAMP`]
  deleted_at timestamp [null]

  indexes {
    job_id
    status
    (job_id, sequence_order)
  }
}

Table payment_requests {
  id bigserial [pk, increment]

  // Relationships
  job_id bigint [not null, ref: > jobs.id]
  milestone_id bigint [null, ref: > milestones.id, note: 'NULL = full job payment']

  // Payment Link
  payment_link_token uuid [unique, not null, note: 'UUID for secure payment links']

  // Amount
  amount decimal(19,4) [not null]
  currency varchar(3) [not null, default: 'GBP']

  // Status
  status varchar(50) [default: 'PENDING', note: 'PENDING, PAID, EXPIRED, CANCELLED']

  // Payment Method
  payment_method varchar(50) [default: 'ESCROW_COM', note: 'ESCROW_COM, STRIPE (future), PAYPAL (future)']

  // Tracking
  view_count integer [default: 0]
  last_viewed_at timestamp
  reminder_sent_count integer [default: 0]
  last_reminder_sent_at timestamp

  // Dates
  expires_at timestamp [not null, note: 'Default: 7 days from creation']
  paid_at timestamp

  // Timestamps
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [default: `CURRENT_TIMESTAMP`]

  indexes {
    job_id
    milestone_id
    payment_link_token
    status
    expires_at
  }
}

Table escrow_transactions {
  id bigserial [pk, increment]

  // Relationships
  payment_request_id bigint [not null, ref: - payment_requests.id, note: 'One-to-one relationship']

  // Escrow.com Data
  escrow_transaction_id varchar(255) [unique, not null, note: 'From escrow.com API']
  escrow_status varchar(50) [default: 'PENDING', note: 'PENDING, HELD, RELEASED, REFUNDED, DISPUTED, CANCELLED']

  // Amounts
  amount decimal(19,4) [not null]
  currency varchar(3) [not null]
  escrow_fee decimal(19,4) [note: 'Escrow.com fee']
  net_amount decimal(19,4) [note: 'Amount after fees']

  // API Response
  escrow_api_response jsonb [note: 'Full API response for debugging']

  // Dates
  escrow_created_at timestamp
  escrow_updated_at timestamp
  escrow_held_at timestamp
  escrow_released_at timestamp
  escrow_refunded_at timestamp

  // Timestamps
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [default: `CURRENT_TIMESTAMP`]

  indexes {
    payment_request_id
    escrow_transaction_id
    escrow_status
  }
}

Table job_completions {
  id bigserial [pk, increment]

  // Relationships
  job_id bigint [unique, not null, ref: - jobs.id, note: 'One completion per job']

  // Freelancer Approval
  freelancer_approved boolean [default: false]
  freelancer_approved_at timestamp
  freelancer_notes text

  // Client Approval
  client_approved boolean [default: false]
  client_approved_at timestamp
  client_notes text

  // Evidence
  completion_evidence_json jsonb [note: 'Photos, documents, links']

  // Feedback
  client_rating integer [note: 'CHECK: 1-5']
  client_feedback text

  // Both Approved
  both_approved_at timestamp [note: 'Populated when both = TRUE']

  // Auto-Approval
  auto_approval_date timestamp [note: 'Auto-approve if no response by this date']
  auto_approved boolean [default: false]

  // Timestamps
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [default: `CURRENT_TIMESTAMP`]

  indexes {
    job_id
  }
}

Table disputes {
  id bigserial [pk, increment]

  // Relationships
  job_id bigint [not null, ref: > jobs.id]
  milestone_id bigint [null, ref: > milestones.id, note: 'NULL if disputing entire job']
  raised_by_user_id bigint [not null, ref: > users.id]

  // Details
  dispute_type varchar(50) [not null, note: 'QUALITY_ISSUE, NON_DELIVERY, PAYMENT_ISSUE, SCOPE_CREEP, OTHER']
  reason text [not null]
  evidence_json jsonb [note: 'Files, screenshots, messages']

  // Status & Resolution
  status varchar(50) [default: 'OPEN', note: 'OPEN, UNDER_REVIEW, RESOLVED, ESCALATED, CLOSED']
  resolution varchar(50) [note: 'REFUND_CLIENT, PAY_FREELANCER, PARTIAL_REFUND_50_50, PARTIAL_REFUND_CUSTOM, NO_ACTION']
  resolution_notes text
  partial_refund_amount decimal(19,4)

  // Resolution
  resolved_at timestamp
  resolved_by_user_id bigint [null, ref: > users.id, note: 'Admin who resolved']

  // Timestamps
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [default: `CURRENT_TIMESTAMP`]

  indexes {
    job_id
    raised_by_user_id
    status
    created_at
  }
}

// ========================================
// FUTURE TABLES (Phase 2+)
// ========================================

// Table freelancer_profiles {
//   id bigserial [pk]
//   user_id bigint [unique, not null, ref: - users.id]
//   bio text
//   portfolio_url varchar(255)
//   skills_json jsonb
//   hourly_rate decimal(19,4)
//   created_at timestamp
//   updated_at timestamp
// }

// Table client_profiles {
//   id bigserial [pk]
//   user_id bigint [unique, not null, ref: - users.id]
//   company_name varchar(255)
//   industry varchar(100)
//   created_at timestamp
//   updated_at timestamp
// }

// Table reviews {
//   id bigserial [pk]
//   job_id bigint [not null, ref: > jobs.id]
//   reviewer_user_id bigint [not null, ref: > users.id]
//   reviewed_user_id bigint [not null, ref: > users.id]
//   rating integer [note: '1-5']
//   comment text
//   created_at timestamp
// }

// Table notifications {
//   id bigserial [pk]
//   user_id bigint [not null, ref: > users.id]
//   type varchar(50) [note: 'EMAIL, SMS, PUSH']
//   subject varchar(255)
//   content text
//   status varchar(50) [note: 'PENDING, SENT, FAILED']
//   sent_at timestamp
//   created_at timestamp
// }

// Table audit_logs {
//   id bigserial [pk]
//   table_name varchar(100)
//   record_id bigint
//   action varchar(20) [note: 'INSERT, UPDATE, DELETE']
//   old_values jsonb
//   new_values jsonb
//   changed_by_user_id bigint [ref: > users.id]
//   changed_at timestamp
// }

// Table escrow_webhooks {
//   id bigserial [pk]
//   escrow_transaction_id varchar(255)
//   event_type varchar(100)
//   payload jsonb
//   processed boolean [default: false]
//   received_at timestamp
// }

// Table categories {
//   id bigserial [pk]
//   name varchar(100) [unique, not null]
//   description text
//   parent_category_id bigint [null, ref: > categories.id]
//   created_at timestamp
// }
