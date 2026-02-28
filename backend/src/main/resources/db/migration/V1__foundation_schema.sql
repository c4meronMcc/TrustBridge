-- V1__Initial_Schema.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
       id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
       email VARCHAR(255) UNIQUE NOT NULL,
       phone_number VARCHAR(20),
       password_hash VARCHAR(255),
       first_name VARCHAR(100) NOT NULL,
       last_name VARCHAR(100) NOT NULL,
       role VARCHAR(50) DEFAULT 'FREELANCER'
           CHECK (role IN ('FREELANCER', 'ADMIN', 'CLIENT', 'CLIENT_GUEST')),
       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Jobs table
CREATE TABLE jobs (
      id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
      freelancer_id uuid NOT NULL REFERENCES users(id),
      client_id uuid REFERENCES users(id),
      title VARCHAR(255) NOT NULL,
      description TEXT,
      total_amount DECIMAL(19, 4) NOT NULL,
      currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
      invite_token VARCHAR(64) UNIQUE,
      status VARCHAR(50) DEFAULT 'DRAFT'
          CHECK (status IN ('DRAFT', 'PENDING_ACCEPTANCE', 'AWAITING_PAYMENT', 'IN_PROGRESS', 'SUBMITTED', 'APPROVED', 'PAID_OUT', 'DISPUTED', 'CANCELLED')),
      created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Milestones table
CREATE TABLE milestones (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id uuid NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    sequence_order INTEGER NOT NULL,
    status VARCHAR(50) DEFAULT 'LOCKED'
        CHECK (status IN ('LOCKED', 'AWAITING_PAYMENT', 'IN_PROGRESS', 'SUBMITTED', 'APPROVED', 'PAID_OUT', 'DISPUTED_NEGOTIATION', 'DISPUTE_ARBITRATION', 'DISPUTE_RESOLVED', 'CANCELLED')),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Payment Requests table
CREATE TABLE payment_requests (
      id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
      milestone_id uuid NOT NULL REFERENCES milestones(id),
      payment_link_token UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
      stripe_session_id VARCHAR(255),
      amount DECIMAL(19, 4) NOT NULL,
      status VARCHAR(50) DEFAULT 'PENDING'
          CHECK (status IN ('PENDING', 'PROCESSING', 'PAID', 'FAILED', 'EXPIRED', 'CANCELLED', 'REFUNDED')),
      expires_at TIMESTAMPTZ NOT NULL,
      created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_payment_requests_token ON payment_requests(payment_link_token);
CREATE INDEX idx_payment_requests_stripe_session ON payment_requests(stripe_session_id);