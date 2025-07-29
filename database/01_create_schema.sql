-- ============================================================================
-- Database Schema Creation Script
-- ============================================================================
-- Description: Creates all tables, sequences, and constraints
-- Version: 1.0
-- Database: Oracle 26c Free
-- ============================================================================

-- ============================================================================
-- SECTION 1: DROP EXISTING OBJECTS (if exists)
-- ============================================================================

PROMPT Dropping existing objects...

-- Drop tables (in reverse order of dependencies)
DROP TABLE alert_notifications CASCADE CONSTRAINTS;
DROP TABLE audit_trail CASCADE CONSTRAINTS;
DROP TABLE compliance_violations CASCADE CONSTRAINTS;
DROP TABLE financial_entities CASCADE CONSTRAINTS;

-- Drop sequences
DROP SEQUENCE entity_seq;
DROP SEQUENCE violation_seq;
DROP SEQUENCE audit_seq;
DROP SEQUENCE alert_seq;

PROMPT Existing objects dropped successfully.

-- ============================================================================
-- SECTION 2: CREATE SEQUENCES
-- ============================================================================

PROMPT Creating sequences...

-- Entity ID sequence (starts at 1000)
CREATE SEQUENCE entity_seq
    START WITH 1000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Violation ID sequence (starts at 5000)
CREATE SEQUENCE violation_seq
    START WITH 5000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Audit ID sequence (starts at 10000)
CREATE SEQUENCE audit_seq
    START WITH 10000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Alert ID sequence (starts at 20000)
CREATE SEQUENCE alert_seq
    START WITH 20000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

PROMPT Sequences created successfully.

-- ============================================================================
-- SECTION 3: CREATE TABLES
-- ============================================================================

PROMPT Creating tables...

-- ----------------------------------------------------------------------------
-- Table: FINANCIAL_ENTITIES
-- Description: Central registry of all financial institutions
-- ----------------------------------------------------------------------------

CREATE TABLE financial_entities (
    -- Primary Key
    entity_id           NUMBER          NOT NULL,
    
    -- Basic Information
    entity_name         VARCHAR2(255)   NOT NULL,
    entity_type         VARCHAR2(50)    NOT NULL,
    nmls_id             VARCHAR2(50),
    dba_name            VARCHAR2(255),
    
    -- Contact Information
    primary_contact     VARCHAR2(255),
    contact_email       VARCHAR2(255),
    contact_phone       VARCHAR2(50),
    
    -- Address Information
    address_line1       VARCHAR2(255),
    address_line2       VARCHAR2(255),
    city                VARCHAR2(100),
    state               VARCHAR2(2)     DEFAULT 'NY',
    zip_code            VARCHAR2(10),
    
    -- Licensing Information
    license_number      VARCHAR2(100),
    license_expiry      DATE,
    registration_date   DATE            NOT NULL,
    
    -- Compliance Information
    compliance_status   VARCHAR2(50)    DEFAULT 'PENDING_REVIEW' NOT NULL,
    risk_level          VARCHAR2(20)    DEFAULT 'MEDIUM' NOT NULL,
    last_review_date    DATE,
    next_review_date    DATE,
    
    -- Financial Information
    total_assets        NUMBER(18,2),
    employee_count      NUMBER,
    
    -- Status
    is_active           CHAR(1)         DEFAULT 'Y' NOT NULL,
    
    -- Additional Information
    notes               CLOB,
    
    -- Audit Columns
    created_date        DATE            DEFAULT SYSDATE NOT NULL,
    created_by          VARCHAR2(100)   DEFAULT USER NOT NULL,
    modified_date       DATE,
    modified_by         VARCHAR2(100),
    
    -- Constraints
    CONSTRAINT pk_financial_entities PRIMARY KEY (entity_id),
    CONSTRAINT chk_entity_type CHECK (entity_type IN (
        'BANK', 'INSURANCE', 'MSB', 'FINTECH', 'CREDIT_UNION', 'BROKER_DEALER'
    )),
    CONSTRAINT chk_compliance_status CHECK (compliance_status IN (
        'COMPLIANT', 'NON_COMPLIANT', 'PENDING_REVIEW', 
        'UNDER_INVESTIGATION', 'PROBATION', 'SUSPENDED'
    )),
    CONSTRAINT chk_risk_level CHECK (risk_level IN (
        'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    )),
    CONSTRAINT chk_is_active CHECK (is_active IN ('Y', 'N'))
);

-- Add table comment
COMMENT ON TABLE financial_entities IS 'Central registry of all financial institutions regulated by DFS';

-- Add column comments
COMMENT ON COLUMN financial_entities.entity_id IS 'Unique identifier for entity';
COMMENT ON COLUMN financial_entities.entity_name IS 'Legal name of institution';
COMMENT ON COLUMN financial_entities.entity_type IS 'Type of institution (BANK, INSURANCE, etc.)';
COMMENT ON COLUMN financial_entities.nmls_id IS 'National Mortgage Licensing System ID';
COMMENT ON COLUMN financial_entities.compliance_status IS 'Current compliance status';
COMMENT ON COLUMN financial_entities.risk_level IS 'Risk assessment level';

PROMPT Table FINANCIAL_ENTITIES created successfully.

-- ----------------------------------------------------------------------------
-- Table: COMPLIANCE_VIOLATIONS
-- Description: Record of all regulatory violations
-- ----------------------------------------------------------------------------

CREATE TABLE compliance_violations (
    -- Primary Key
    violation_id        NUMBER          NOT NULL,
    
    -- Foreign Key
    entity_id           NUMBER          NOT NULL,
    
    -- Violation Information
    violation_type      VARCHAR2(100)   NOT NULL,
    violation_code      VARCHAR2(50),
    description         CLOB            NOT NULL,
    severity            VARCHAR2(20)    NOT NULL,
    
    -- Dates
    violation_date      DATE            NOT NULL,
    discovery_date      DATE            DEFAULT SYSDATE NOT NULL,
    reported_by         VARCHAR2(100),
    
    -- Financial Information
    fine_amount         NUMBER(18,2)    DEFAULT 0 NOT NULL,
    fine_paid           CHAR(1)         DEFAULT 'N' NOT NULL,
    payment_due_date    DATE,
    payment_date        DATE,
    
    -- Status Information
    status              VARCHAR2(50)    DEFAULT 'UNDER_REVIEW' NOT NULL,
    resolution_date     DATE,
    resolution_notes    CLOB,
    corrective_action   CLOB,
    
    -- Follow-up
    follow_up_required  CHAR(1)         DEFAULT 'Y' NOT NULL,
    follow_up_date      DATE,
    
    -- Audit Columns
    created_date        DATE            DEFAULT SYSDATE NOT NULL,
    created_by          VARCHAR2(100)   DEFAULT USER NOT NULL,
    modified_date       DATE,
    modified_by         VARCHAR2(100),
    
    -- Constraints
    CONSTRAINT pk_compliance_violations PRIMARY KEY (violation_id),
    CONSTRAINT fk_violation_entity FOREIGN KEY (entity_id) 
        REFERENCES financial_entities(entity_id),
    CONSTRAINT chk_violation_severity CHECK (severity IN (
        'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    )),
    CONSTRAINT chk_violation_status CHECK (status IN (
        'UNDER_REVIEW', 'CONFIRMED', 'RESOLVED', 'APPEALED', 'DISMISSED'
    )),
    CONSTRAINT chk_fine_paid CHECK (fine_paid IN ('Y', 'N')),
    CONSTRAINT chk_follow_up_required CHECK (follow_up_required IN ('Y', 'N')),
    CONSTRAINT chk_violation_dates CHECK (
        resolution_date IS NULL OR resolution_date >= violation_date
    )
);

-- Add table comment
COMMENT ON TABLE compliance_violations IS 'Record of all regulatory violations and enforcement actions';

-- Add column comments
COMMENT ON COLUMN compliance_violations.violation_id IS 'Unique identifier for violation';
COMMENT ON COLUMN compliance_violations.entity_id IS 'Reference to financial entity';
COMMENT ON COLUMN compliance_violations.severity IS 'Severity level (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN compliance_violations.fine_amount IS 'Monetary penalty in USD';

PROMPT Table COMPLIANCE_VIOLATIONS created successfully.

-- ----------------------------------------------------------------------------
-- Table: AUDIT_TRAIL
-- Description: Complete immutable log of all system activities
-- ----------------------------------------------------------------------------

CREATE TABLE audit_trail (
    -- Primary Key
    audit_id            NUMBER          NOT NULL,
    
    -- Foreign Key (nullable for system-level actions)
    entity_id           NUMBER,
    
    -- Action Information
    action_type         VARCHAR2(100)   NOT NULL,
    action_details      CLOB,
    action_timestamp    TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    
    -- User Information
    performed_by        VARCHAR2(100)   DEFAULT USER NOT NULL,
    ip_address          VARCHAR2(50),
    session_id          VARCHAR2(100),
    
    -- Change Tracking
    before_value        CLOB,
    after_value         CLOB,
    
    -- Constraints
    CONSTRAINT pk_audit_trail PRIMARY KEY (audit_id),
    CONSTRAINT fk_audit_entity FOREIGN KEY (entity_id) 
        REFERENCES financial_entities(entity_id)
);

-- Add table comment
COMMENT ON TABLE audit_trail IS 'Complete immutable log of all system activities';

-- Add column comments
COMMENT ON COLUMN audit_trail.audit_id IS 'Unique identifier for audit entry';
COMMENT ON COLUMN audit_trail.action_type IS 'Type of action performed';
COMMENT ON COLUMN audit_trail.performed_by IS 'User who performed action';
COMMENT ON COLUMN audit_trail.before_value IS 'State before change';
COMMENT ON COLUMN audit_trail.after_value IS 'State after change';

PROMPT Table AUDIT_TRAIL created successfully.

-- ----------------------------------------------------------------------------
-- Table: ALERT_NOTIFICATIONS
-- Description: Automated compliance alerts
-- ----------------------------------------------------------------------------

CREATE TABLE alert_notifications (
    -- Primary Key
    alert_id            NUMBER          NOT NULL,
    
    -- Foreign Key
    entity_id           NUMBER          NOT NULL,
    
    -- Alert Information
    alert_type          VARCHAR2(50)    NOT NULL,
    alert_priority      VARCHAR2(20)    NOT NULL,
    alert_message       VARCHAR2(1000)  NOT NULL,
    alert_timestamp     TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    
    -- Acknowledgment
    acknowledged        CHAR(1)         DEFAULT 'N' NOT NULL,
    acknowledged_by     VARCHAR2(100),
    acknowledged_date   TIMESTAMP,
    
    -- Resolution
    resolved            CHAR(1)         DEFAULT 'N' NOT NULL,
    resolved_date       TIMESTAMP,
    notes               CLOB,
    
    -- Constraints
    CONSTRAINT pk_alert_notifications PRIMARY KEY (alert_id),
    CONSTRAINT fk_alert_entity FOREIGN KEY (entity_id) 
        REFERENCES financial_entities(entity_id),
    CONSTRAINT chk_alert_type CHECK (alert_type IN (
        'NEW_REGISTRATION', 'VIOLATION', 'OVERDUE_VIOLATION',
        'LICENSE_EXPIRING', 'REVIEW_DUE', 'RISK_ESCALATION', 'STATUS_CHANGE'
    )),
    CONSTRAINT chk_alert_priority CHECK (alert_priority IN (
        'LOW', 'MEDIUM', 'HIGH', 'URGENT'
    )),
    CONSTRAINT chk_acknowledged CHECK (acknowledged IN ('Y', 'N')),
    CONSTRAINT chk_resolved CHECK (resolved IN ('Y', 'N'))
);

-- Add table comment
COMMENT ON TABLE alert_notifications IS 'Automated compliance alerts for monitoring officers';

-- Add column comments
COMMENT ON COLUMN alert_notifications.alert_id IS 'Unique identifier for alert';
COMMENT ON COLUMN alert_notifications.alert_type IS 'Classification of alert';
COMMENT ON COLUMN alert_notifications.alert_priority IS 'Priority level (LOW, MEDIUM, HIGH, URGENT)';

PROMPT Table ALERT_NOTIFICATIONS created successfully.

PROMPT All tables created successfully.

-- ============================================================================
-- SECTION 4: CREATE INDEXES
-- ============================================================================

PROMPT Creating indexes...

-- Financial Entities Indexes
CREATE INDEX idx_entity_type ON financial_entities(entity_type);
CREATE INDEX idx_entity_status ON financial_entities(compliance_status);
CREATE INDEX idx_entity_risk ON financial_entities(risk_level);
CREATE INDEX idx_entity_active ON financial_entities(is_active);
CREATE INDEX idx_entity_review ON financial_entities(next_review_date);
CREATE INDEX idx_entity_name_upper ON financial_entities(UPPER(entity_name));
CREATE INDEX idx_entity_license_expiry ON financial_entities(license_expiry);

-- Compliance Violations Indexes
CREATE INDEX idx_violation_entity ON compliance_violations(entity_id);
CREATE INDEX idx_violation_type ON compliance_violations(violation_type);
CREATE INDEX idx_violation_severity ON compliance_violations(severity);
CREATE INDEX idx_violation_status ON compliance_violations(status);
CREATE INDEX idx_violation_date ON compliance_violations(violation_date);
CREATE INDEX idx_violation_unpaid ON compliance_violations(fine_paid, payment_due_date);

-- Audit Trail Indexes
CREATE INDEX idx_audit_entity ON audit_trail(entity_id);
CREATE INDEX idx_audit_timestamp ON audit_trail(action_timestamp);
CREATE INDEX idx_audit_type ON audit_trail(action_type);
CREATE INDEX idx_audit_user ON audit_trail(performed_by);

-- Alert Notifications Indexes
CREATE INDEX idx_alert_entity ON alert_notifications(entity_id);
CREATE INDEX idx_alert_unack ON alert_notifications(acknowledged, alert_priority);
CREATE INDEX idx_alert_timestamp ON alert_notifications(alert_timestamp);
CREATE INDEX idx_alert_type ON alert_notifications(alert_type);

PROMPT Indexes created successfully.

-- ============================================================================
-- SECTION 5: CREATE TRIGGERS
-- ============================================================================

PROMPT Creating triggers...

-- ----------------------------------------------------------------------------
-- Trigger: trg_audit_immutable
-- Description: Prevent modification of audit trail records
-- ----------------------------------------------------------------------------

CREATE OR REPLACE TRIGGER trg_audit_immutable
BEFORE UPDATE OR DELETE ON audit_trail
FOR EACH ROW
BEGIN
    RAISE_APPLICATION_ERROR(
        -20001, 
        'Audit trail records are immutable and cannot be modified or deleted'
    );
END;
/

PROMPT Trigger TRG_AUDIT_IMMUTABLE created successfully.

-- ----------------------------------------------------------------------------
-- Trigger: trg_entity_modified
-- Description: Automatically update modified_date and modified_by
-- ----------------------------------------------------------------------------

CREATE OR REPLACE TRIGGER trg_entity_modified
BEFORE UPDATE ON financial_entities
FOR EACH ROW
BEGIN
    :NEW.modified_date := SYSDATE;
    :NEW.modified_by := USER;
END;
/

PROMPT Trigger TRG_ENTITY_MODIFIED created successfully.

-- ----------------------------------------------------------------------------
-- Trigger: trg_violation_modified
-- Description: Automatically update modified_date and modified_by
-- ----------------------------------------------------------------------------

CREATE OR REPLACE TRIGGER trg_violation_modified
BEFORE UPDATE ON compliance_violations
FOR EACH ROW
BEGIN
    :NEW.modified_date := SYSDATE;
    :NEW.modified_by := USER;
END;
/

PROMPT Trigger TRG_VIOLATION_MODIFIED created successfully.

PROMPT All triggers created successfully.

-- ============================================================================
-- SECTION 6: GRANT PERMISSIONS (Optional - for multi-user setup)
-- ============================================================================

-- Uncomment these if setting up for multiple users
/*
PROMPT Granting permissions...

GRANT SELECT, INSERT, UPDATE, DELETE ON financial_entities TO dfs_compliance_officer;
GRANT SELECT, INSERT, UPDATE, DELETE ON compliance_violations TO dfs_compliance_officer;
GRANT SELECT ON audit_trail TO dfs_compliance_officer;
GRANT SELECT, INSERT, UPDATE ON alert_notifications TO dfs_compliance_officer;

GRANT SELECT ON financial_entities TO dfs_readonly;
GRANT SELECT ON compliance_violations TO dfs_readonly;
GRANT SELECT ON audit_trail TO dfs_readonly;
GRANT SELECT ON alert_notifications TO dfs_readonly;

PROMPT Permissions granted successfully.
*/

-- ============================================================================
-- SECTION 7: VERIFICATION
-- ============================================================================

PROMPT Verifying schema creation...

-- Count tables
SELECT COUNT(*) AS table_count 
FROM user_tables 
WHERE table_name IN (
    'FINANCIAL_ENTITIES', 
    'COMPLIANCE_VIOLATIONS', 
    'AUDIT_TRAIL', 
    'ALERT_NOTIFICATIONS'
);

-- Count sequences
SELECT COUNT(*) AS sequence_count 
FROM user_sequences 
WHERE sequence_name IN (
    'ENTITY_SEQ', 
    'VIOLATION_SEQ', 
    'AUDIT_SEQ', 
    'ALERT_SEQ'
);

-- Count indexes
SELECT COUNT(*) AS index_count 
FROM user_indexes 
WHERE table_name IN (
    'FINANCIAL_ENTITIES', 
    'COMPLIANCE_VIOLATIONS', 
    'AUDIT_TRAIL', 
    'ALERT_NOTIFICATIONS'
);

-- Count triggers
SELECT COUNT(*) AS trigger_count 
FROM user_triggers 
WHERE table_name IN (
    'FINANCIAL_ENTITIES', 
    'COMPLIANCE_VIOLATIONS', 
    'AUDIT_TRAIL'
);

-- Display table structures
PROMPT 
PROMPT Table Structures:
PROMPT 

DESC financial_entities;
DESC compliance_violations;
DESC audit_trail;
DESC alert_notifications;

-- ============================================================================
-- COMPLETION MESSAGE
-- ============================================================================

PROMPT 
PROMPT ============================================================================
PROMPT Schema creation completed successfully!
PROMPT ============================================================================
PROMPT 
PROMPT Summary:
PROMPT - 4 tables created
PROMPT - 4 sequences created
PROMPT - 18 indexes created
PROMPT - 3 triggers created
PROMPT 
PROMPT Next steps:
PROMPT 1. Run 02_create_packages.sql to create PL/SQL packages
PROMPT 2. Run 03_insert_sample_data.sql to populate with test data
PROMPT 
PROMPT ============================================================================