-- TrustBridge Database Schema v0.1.0-MVP




CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ENUM Types
CREATE TYPE user_status_enum AS ENUM (
    'PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'BANNED', 'BRUTE_FORCE_LOCKED'
    );

CREATE TYPE job_status_enum AS ENUM (
    'DRAFT', 'PENDING_PAYMENT', 'PAYMENT_RECEIVED', 'IN_PROGRESS',
    'PENDING_COMPLETION', 'COMPLETED', 'CANCELLED', 'DISPUTED', 'REFUNDED'
    );

CREATE TYPE payment_type_enum AS ENUM (
    'FULL_UPFRONT', 'MILESTONE', 'HOURLY', 'PARTIAL_UPFRONT'
    );

CREATE TYPE milestone_status_enum AS ENUM (
    'PENDING', 'IN_PROGRESS', 'COMPLETED_BY_FREELANCER', 'APPROVED_BY_CLIENT',
    'PAID', 'SKIPPED', 'DISPUTED'
    );

CREATE TYPE payment_request_status_enum AS ENUM (
    'PENDING', 'PAID', 'EXPIRED', 'CANCELLED'
    );

CREATE TYPE payment_method_enum AS ENUM (
    'ESCROW_COM', 'STRIPE', 'PAYPAL'
    );

CREATE TYPE escrow_status_enum AS ENUM (
    'PENDING', 'HELD', 'RELEASED', 'REFUNDED', 'DISPUTED', 'CANCELLED'
    );

CREATE TYPE dispute_type_enum AS ENUM (
    'QUALITY_ISSUE', 'NON_DELIVERY', 'PAYMENT_ISSUE', 'SCOPE_CREEP', 'OTHER'
    );

CREATE TYPE dispute_status_enum AS ENUM (
    'OPEN', 'UNDER_REVIEW', 'RESOLVED', 'ESCALATED', 'CLOSED'
    );

CREATE TYPE dispute_resolution_enum AS ENUM (
    'REFUND_CLIENT', 'PAY_FREELANCER', 'PARTIAL_REFUND_50_50',
    'PARTIAL_REFUND_CUSTOM', 'NO_ACTION'
    );

-- Tables

-- users: Core identity and authentication
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       oauth_provider VARCHAR(50),
                       oauth_provider_id VARCHAR(255),
                       email_verified BOOLEAN DEFAULT FALSE,
                       email_verification_token VARCHAR(255),
                       email_verification_expires_at TIMESTAMPTZ,
                       reset_token VARCHAR(255),
                       reset_token_expires_at TIMESTAMPTZ,
                       status user_status_enum DEFAULT 'PENDING_VERIFICATION',
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       business_name VARCHAR(255),
                       phone_number VARCHAR(20),
                       country_code VARCHAR(3),
                       timezone VARCHAR(50),
                       failed_login_attempts INTEGER DEFAULT 0,
                       last_failed_login_at TIMESTAMPTZ,
                       last_login_at TIMESTAMPTZ,
                       brute_force_locked_until TIMESTAMPTZ,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_users_email_unique ON users(email) WHERE deleted_at IS NULL; -- Soft delete aware
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- roles: User role definitions
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL,
                       description TEXT,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (name, description) VALUES
                                          ('FREELANCER', 'User who provides services'),
                                          ('CLIENT', 'User who hires services');

-- user_roles: Junction table for user-role relationships
CREATE TABLE user_roles (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE(user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- jobs: Work projects between freelancers and clients
CREATE TABLE jobs (
                      id BIGSERIAL PRIMARY KEY,
                      freelancer_id BIGINT NOT NULL REFERENCES users(id),
                      client_id BIGINT NOT NULL REFERENCES users(id),
                      parent_job_id BIGINT REFERENCES jobs(id),
                      title VARCHAR(255) NOT NULL,
                      description TEXT NOT NULL,
                      total_amount DECIMAL(19, 4) NOT NULL,
                      currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
                      payment_type payment_type_enum DEFAULT 'MILESTONE',
                      partial_upfront_amount DECIMAL(19, 4),
                      status job_status_enum DEFAULT 'DRAFT',
                      estimated_start_date DATE,
                      estimated_completion_date DATE,
                      actual_start_date DATE,
                      actual_completion_date DATE,
                      contract_terms TEXT,
                      created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                      deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_jobs_freelancer_id ON jobs(freelancer_id);
CREATE INDEX idx_jobs_client_id ON jobs(client_id);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_created_at ON jobs(created_at);
CREATE INDEX idx_jobs_deleted_at ON jobs(deleted_at);

-- attachments: File attachments (polymorphic - jobs, milestones, disputes, completions)
CREATE TABLE attachments (
                             id BIGSERIAL PRIMARY KEY,
                             entity_type VARCHAR(50) NOT NULL CHECK (entity_type IN ('JOB', 'MILESTONE', 'DISPUTE', 'JOB_COMPLETION')),
                             entity_id BIGINT NOT NULL,
                             file_url VARCHAR(500) NOT NULL,
                             file_name VARCHAR(255) NOT NULL,
                             file_type VARCHAR(100),
                             file_size_bytes BIGINT,
                             description TEXT,
                             uploaded_by_user_id BIGINT REFERENCES users(id),
                             uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                             created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                             deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_attachments_entity ON attachments(entity_type, entity_id);
CREATE INDEX idx_attachments_uploaded_by ON attachments(uploaded_by_user_id);
CREATE INDEX idx_attachments_deleted_at ON attachments(deleted_at);

-- milestones: Payment checkpoints within jobs
CREATE TABLE milestones (
                            id BIGSERIAL PRIMARY KEY,
                            job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
                            parent_milestone_id BIGINT REFERENCES milestones(id),
                            title VARCHAR(255) NOT NULL,
                            description TEXT,
                            sequence_order INTEGER NOT NULL,
                            amount DECIMAL(19, 4) NOT NULL,
                            currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
                            status milestone_status_enum DEFAULT 'PENDING',
                            due_date DATE,
                            freelancer_completed_at TIMESTAMPTZ,
                            client_approved_at TIMESTAMPTZ,
                            paid_at TIMESTAMPTZ,
                            depends_on_milestone_id BIGINT REFERENCES milestones(id),
                            created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                            deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_milestones_job_id ON milestones(job_id);
CREATE INDEX idx_milestones_status ON milestones(status);
CREATE UNIQUE INDEX idx_milestones_job_sequence_unique ON milestones(job_id, sequence_order);

-- payment_requests: Payment links sent to clients
CREATE TABLE payment_requests (
                                  id BIGSERIAL PRIMARY KEY,
                                  job_id BIGINT NOT NULL REFERENCES jobs(id),
                                  milestone_id BIGINT REFERENCES milestones(id), -- NULL = full job payment
                                  payment_link_token UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
                                  amount DECIMAL(19, 4) NOT NULL,
                                  currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
                                  status payment_request_status_enum DEFAULT 'PENDING',
                                  payment_method payment_method_enum DEFAULT 'ESCROW_COM',
                                  view_count INTEGER DEFAULT 0,
                                  last_viewed_at TIMESTAMPTZ,
                                  reminder_sent_count INTEGER DEFAULT 0,
                                  last_reminder_sent_at TIMESTAMPTZ,
                                  expires_at TIMESTAMPTZ NOT NULL,
                                  paid_at TIMESTAMPTZ,
                                  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_requests_job_id ON payment_requests(job_id);
CREATE INDEX idx_payment_requests_milestone_id ON payment_requests(milestone_id);
CREATE INDEX idx_payment_requests_token ON payment_requests(payment_link_token);
CREATE INDEX idx_payment_requests_status ON payment_requests(status);
CREATE INDEX idx_payment_requests_expires_at ON payment_requests(expires_at);

-- escrow_transactions: Escrow.com API transaction tracking
CREATE TABLE escrow_transactions (
                                     id BIGSERIAL PRIMARY KEY,
                                     payment_request_id BIGINT NOT NULL REFERENCES payment_requests(id),
                                     escrow_transaction_id VARCHAR(255) UNIQUE NOT NULL,
                                     escrow_status escrow_status_enum DEFAULT 'PENDING',
                                     amount DECIMAL(19, 4) NOT NULL,
                                     currency VARCHAR(3) NOT NULL,
                                     escrow_fee DECIMAL(19, 4),
                                     net_amount DECIMAL(19, 4),
                                     escrow_api_response JSONB,
                                     escrow_created_at TIMESTAMPTZ,
                                     escrow_updated_at TIMESTAMPTZ,
                                     escrow_held_at TIMESTAMPTZ,
                                     escrow_released_at TIMESTAMPTZ,
                                     escrow_refunded_at TIMESTAMPTZ,
                                     created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_escrow_transactions_payment_request_id ON escrow_transactions(payment_request_id);
CREATE INDEX idx_escrow_transactions_escrow_transaction_id ON escrow_transactions(escrow_transaction_id);
CREATE INDEX idx_escrow_transactions_status ON escrow_transactions(escrow_status);

-- financial_ledger: Immutable audit trail for financial state changes
CREATE TABLE financial_ledger (
                                  id BIGSERIAL PRIMARY KEY,
                                  entity_type VARCHAR(50) NOT NULL CHECK (entity_type IN ('MILESTONE', 'PAYMENT_REQUEST', 'ESCROW_TRANSACTION')),
                                  entity_id BIGINT NOT NULL,
                                  field_name VARCHAR(100) NOT NULL,
                                  old_value TEXT,
                                  new_value TEXT NOT NULL,
                                  change_reason TEXT,
                                  changed_by_user_id BIGINT REFERENCES users(id),
                                  changed_via VARCHAR(50) CHECK (changed_via IN ('USER_ACTION', 'API_WEBHOOK', 'SYSTEM_AUTO', 'TRIGGER')),
                                  metadata_json JSONB,
                                  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_financial_ledger_entity ON financial_ledger(entity_type, entity_id);
CREATE INDEX idx_financial_ledger_changed_by ON financial_ledger(changed_by_user_id);
CREATE INDEX idx_financial_ledger_created_at ON financial_ledger(created_at);
CREATE INDEX idx_financial_ledger_field_name ON financial_ledger(field_name);

-- job_completions: Dual approval workflow (allows resubmission after rejection)
CREATE TABLE job_completions (
                                 id BIGSERIAL PRIMARY KEY,
                                 job_id BIGINT NOT NULL REFERENCES jobs(id),
                                 status VARCHAR(50) DEFAULT 'PENDING',
                                 freelancer_approved BOOLEAN DEFAULT FALSE,
                                 freelancer_approved_at TIMESTAMPTZ,
                                 freelancer_notes TEXT,
                                 client_approved BOOLEAN DEFAULT FALSE,
                                 client_approved_at TIMESTAMPTZ,
                                 client_notes TEXT,
                                 client_rating INTEGER CHECK (client_rating >= 1 AND client_rating <= 5),
                                 client_feedback TEXT,
                                 both_approved_at TIMESTAMPTZ,
                                 auto_approval_date TIMESTAMPTZ,
                                 auto_approved BOOLEAN DEFAULT FALSE,
                                 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_job_completions_job_id_active ON job_completions(job_id) WHERE status != 'REJECTED'; -- Allows resubmission after rejection
CREATE INDEX idx_job_completions_job_id ON job_completions(job_id);
CREATE INDEX idx_job_completions_status ON job_completions(status);

-- disputes: Conflict resolution and arbitration
CREATE TABLE disputes (
                          id BIGSERIAL PRIMARY KEY,
                          job_id BIGINT NOT NULL REFERENCES jobs(id),
                          milestone_id BIGINT REFERENCES milestones(id), -- NULL = disputing entire job
                          raised_by_user_id BIGINT NOT NULL REFERENCES users(id),
                          dispute_type dispute_type_enum NOT NULL,
                          reason TEXT NOT NULL,
                          evidence_json JSONB,
                          status dispute_status_enum DEFAULT 'OPEN',
                          resolution dispute_resolution_enum,
                          resolution_notes TEXT,
                          partial_refund_amount DECIMAL(19, 4),
                          resolved_at TIMESTAMPTZ,
                          resolved_by_user_id BIGINT REFERENCES users(id),
                          created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_disputes_job_id ON disputes(job_id);
CREATE INDEX idx_disputes_raised_by_user_id ON disputes(raised_by_user_id);
CREATE INDEX idx_disputes_status ON disputes(status);
CREATE INDEX idx_disputes_created_at ON disputes(created_at);

-- Triggers

-- Auto-update updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_jobs_updated_at BEFORE UPDATE ON jobs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_milestones_updated_at BEFORE UPDATE ON milestones
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payment_requests_updated_at BEFORE UPDATE ON payment_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_escrow_transactions_updated_at BEFORE UPDATE ON escrow_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_job_completions_updated_at BEFORE UPDATE ON job_completions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_disputes_updated_at BEFORE UPDATE ON disputes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Currency consistency: Fail-fast triggers prevent currency mismatches
CREATE OR REPLACE FUNCTION enforce_milestone_currency()
    RETURNS TRIGGER AS $$
DECLARE
    job_currency VARCHAR(3);
BEGIN
    SELECT currency INTO job_currency FROM jobs WHERE id = NEW.job_id;
    IF NEW.currency != job_currency THEN
        RAISE EXCEPTION 'Currency mismatch: Milestone currency (%) does not match job currency (%). Milestone must use job currency: %',
            NEW.currency, job_currency, job_currency
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER enforce_milestone_currency_trigger
    BEFORE INSERT OR UPDATE ON milestones
    FOR EACH ROW EXECUTE FUNCTION enforce_milestone_currency();

CREATE OR REPLACE FUNCTION enforce_payment_request_currency()
    RETURNS TRIGGER AS $$
DECLARE
    job_currency VARCHAR(3);
BEGIN
    SELECT currency INTO job_currency FROM jobs WHERE id = NEW.job_id;
    IF NEW.currency != job_currency THEN
        RAISE EXCEPTION 'Currency mismatch: Payment request currency (%) does not match job currency (%). Payment request must use job currency: %',
            NEW.currency, job_currency, job_currency
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER enforce_payment_request_currency_trigger
    BEFORE INSERT OR UPDATE ON payment_requests
    FOR EACH ROW EXECUTE FUNCTION enforce_payment_request_currency();

CREATE OR REPLACE FUNCTION enforce_escrow_currency()
    RETURNS TRIGGER AS $$
DECLARE
    payment_request_currency VARCHAR(3);
BEGIN
    SELECT currency INTO payment_request_currency FROM payment_requests WHERE id = NEW.payment_request_id;
    IF NEW.currency != payment_request_currency THEN
        RAISE EXCEPTION 'Currency mismatch: Escrow transaction currency (%) does not match payment request currency (%). Escrow transaction must use payment request currency: %',
            NEW.currency, payment_request_currency, payment_request_currency
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER enforce_escrow_currency_trigger
    BEFORE INSERT OR UPDATE ON escrow_transactions
    FOR EACH ROW EXECUTE FUNCTION enforce_escrow_currency();

-- Financial ledger: Fallback logging for automated changes (webhooks, triggers)
-- Note: Application should manually INSERT with user context for user-initiated changes
CREATE OR REPLACE FUNCTION log_financial_change()
    RETURNS TRIGGER AS $$
DECLARE
    entity_type_val VARCHAR(50);
BEGIN
    IF TG_TABLE_NAME = 'milestones' THEN
        entity_type_val := 'MILESTONE';
    ELSIF TG_TABLE_NAME = 'payment_requests' THEN
        entity_type_val := 'PAYMENT_REQUEST';
    ELSIF TG_TABLE_NAME = 'escrow_transactions' THEN
        entity_type_val := 'ESCROW_TRANSACTION';
    ELSE
        entity_type_val := 'UNKNOWN';
    END IF;

    IF TG_OP = 'UPDATE' AND OLD.escrow_status IS DISTINCT FROM NEW.escrow_status THEN
        INSERT INTO financial_ledger (
            entity_type, entity_id, field_name, old_value, new_value,
            changed_by_user_id, changed_via, created_at
        ) VALUES (
                     entity_type_val, NEW.id, 'status',
                     OLD.escrow_status::TEXT, NEW.escrow_status::TEXT,
                     NULL, 'TRIGGER', CURRENT_TIMESTAMP
                 );
    END IF;

    IF TG_OP = 'UPDATE' AND OLD.amount IS DISTINCT FROM NEW.amount THEN
        INSERT INTO financial_ledger (
            entity_type, entity_id, field_name, old_value, new_value,
            changed_by_user_id, changed_via, created_at
        ) VALUES (
                     entity_type_val, NEW.id, 'amount',
                     OLD.amount::TEXT, NEW.amount::TEXT,
                     NULL, 'TRIGGER', CURRENT_TIMESTAMP
                 );
    END IF;

    IF TG_OP = 'UPDATE' AND OLD.currency IS DISTINCT FROM NEW.currency THEN
        INSERT INTO financial_ledger (
            entity_type, entity_id, field_name, old_value, new_value,
            changed_by_user_id, changed_via, created_at
        ) VALUES (
                     entity_type_val, NEW.id, 'currency',
                     OLD.currency, NEW.currency,
                     NULL, 'TRIGGER', CURRENT_TIMESTAMP
                 );
    END IF;

    IF TG_TABLE_NAME = 'escrow_transactions' AND TG_OP = 'UPDATE' AND OLD.escrow_status IS DISTINCT FROM NEW.escrow_status THEN
        INSERT INTO financial_ledger (
            entity_type, entity_id, field_name, old_value, new_value,
            changed_by_user_id, changed_via, created_at
        ) VALUES (
                     'ESCROW_TRANSACTION', NEW.id, 'escrow_status',
                     OLD.escrow_status::TEXT, NEW.escrow_status::TEXT,
                     NULL, 'TRIGGER', CURRENT_TIMESTAMP
                 );
    END IF;

    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER log_milestone_financial_changes
    AFTER UPDATE ON milestones
    FOR EACH ROW EXECUTE FUNCTION log_financial_change();
CREATE TRIGGER log_payment_request_financial_changes
    AFTER UPDATE ON payment_requests
    FOR EACH ROW EXECUTE FUNCTION log_financial_change();
CREATE TRIGGER log_escrow_financial_changes
    AFTER UPDATE ON escrow_transactions
    FOR EACH ROW EXECUTE FUNCTION log_financial_change();

-- Job completion: Auto-set both_approved_at and status when both parties approve
CREATE OR REPLACE FUNCTION handle_both_approved()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.freelancer_approved = TRUE AND NEW.client_approved = TRUE THEN
        IF NEW.both_approved_at IS NULL THEN
            NEW.both_approved_at := CURRENT_TIMESTAMP;
        END IF;
        IF NEW.status != 'APPROVED' THEN
            NEW.status := 'APPROVED';
        END IF;
    ELSIF (OLD.freelancer_approved = TRUE AND NEW.freelancer_approved = FALSE) OR
          (OLD.client_approved = TRUE AND NEW.client_approved = FALSE) THEN
        NEW.both_approved_at := NULL;
        IF NEW.status = 'APPROVED' THEN
            NEW.status := 'PENDING';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER handle_job_completion_both_approved
    BEFORE INSERT OR UPDATE ON job_completions
    FOR EACH ROW EXECUTE FUNCTION handle_both_approved();
