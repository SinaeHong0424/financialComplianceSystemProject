-- DFS Financial Compliance Management System
-- Database Schema Creation Script
-- Creates all tables, sequences, constraints

-- DROP EXISTING OBJECTS

DROP TABLE alert_notifications CASCADE CONSTRAINTS;
DROP TABLE audit_trail CASCADE CONSTRAINTS;
DROP TABLE compliance_violations CASCADE CONSTRAINTS;
DROP TABLE financial_entities CASCADE CONSTRAINTS;

DROP SEQUENCE entity_seq;
DROP SEQUENCE violation_seq;
DROP SEQUENCE audit_seq;
DROP SEQUENCE alert_seq;

-- CREATE SEQUENCES

CREATE SEQUENCE entity_seq
    START WITH 1000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE violation_seq
    START WITH 5000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE audit_seq
    START WITH 10000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE alert_seq
    START WITH 20000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- CREATE TABLES

CREATE TABLE financial_entities (
    entity_id           NUMBER          NOT NULL,
    entity_name         VARCHAR2(255)   NOT NULL,
    entity_type         VARCHAR2(50)    NOT NULL,
    nmls_id             VARCHAR2(50),
    dba_name            VARCHAR2(255),
    primary_contact     VARCHAR2(255),
    contact_email       VARCHAR2(255),
    contact_phone       VARCHAR2(50),
    address_line1       VARCHAR2(255),
    address_line2       VARCHAR2(255),
    city                VARCHAR2(100),
    state               VARCHAR2(2)     DEFAULT 'NY',
    zip_code            VARCHAR2(10),
    license_number      VARCHAR2(100),
    license_expiry      DATE,
    registration_date   DATE            NOT NULL,
    compliance_status   VARCHAR2(50)    DEFAULT 'PENDING_REVIEW' NOT NULL,
    risk_level          VARCHAR2(20)    DEFAULT 'MEDIUM' NOT NULL,
    last_review_date    DATE,
    next_review_date    DATE,
    total_assets        NUMBER(18,2),
    employee_count      NUMBER,
    is_active           CHAR(1)         DEFAULT 'Y' NOT NULL,
    notes               CLOB,
    created_date        DATE            DEFAULT SYSDATE NOT NULL,
    created_by          VARCHAR2(100)   DEFAULT USER NOT NULL,
    modified_date       DATE,
    modified_by         VARCHAR2(100),
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

COMMENT ON TABLE financial_entities IS 'Central registry of all financial institutions regulated by DFS';

CREATE TABLE compliance_violations (
    violation_id        NUMBER          NOT NULL,
    entity_id           NUMBER          NOT NULL,
    violation_type      VARCHAR2(100)   NOT NULL,
    violation_code      VARCHAR2(50),
    description         CLOB            NOT NULL,
    severity            VARCHAR2(20)    NOT NULL,
    violation_date      DATE            NOT NULL,
    discovery_date      DATE            DEFAULT SYSDATE NOT NULL,
    reported_by         VARCHAR2(100),
    fine_amount         NUMBER(18,2)    DEFAULT 0 NOT NULL,
    fine_paid           CHAR(1)         DEFAULT 'N' NOT NULL,
    payment_due_date    DATE,
    payment_date        DATE,
    status              VARCHAR2(50)    DEFAULT 'UNDER_REVIEW' NOT NULL,
    resolution_date     DATE,
    resolution_notes    CLOB,
    corrective_action   CLOB,
    follow_up_required  CHAR(1)         DEFAULT 'Y' NOT NULL,
    follow_up_date      DATE,
    created_date        DATE            DEFAULT SYSDATE NOT NULL,
    created_by          VARCHAR2(100)   DEFAULT USER NOT NULL,
    modified_date       DATE,
    modified_by         VARCHAR2(100),
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

COMMENT ON TABLE compliance_violations IS 'Record of all regulatory violations and enforcement actions';

CREATE TABLE audit_trail (
    audit_id            NUMBER          NOT NULL,
    entity_id           NUMBER,
    action_type         VARCHAR2(100)   NOT NULL,
    action_details      CLOB,
    action_timestamp    TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    performed_by        VARCHAR2(100)   DEFAULT USER NOT NULL,
    ip_address          VARCHAR2(50),
    session_id          VARCHAR2(100),
    before_value        CLOB,
    after_value         CLOB,
    CONSTRAINT pk_audit_trail PRIMARY KEY (audit_id),
    CONSTRAINT fk_audit_entity FOREIGN KEY (entity_id) 
        REFERENCES financial_entities(entity_id)
);

COMMENT ON TABLE audit_trail IS 'Complete immutable log of all system activities';

CREATE TABLE alert_notifications (
    alert_id            NUMBER          NOT NULL,
    entity_id           NUMBER          NOT NULL,
    alert_type          VARCHAR2(50)    NOT NULL,
    alert_priority      VARCHAR2(20)    NOT NULL,
    alert_message       VARCHAR2(1000)  NOT NULL,
    alert_timestamp     TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    acknowledged        CHAR(1)         DEFAULT 'N' NOT NULL,
    acknowledged_by     VARCHAR2(100),
    acknowledged_date   TIMESTAMP,
    resolved            CHAR(1)         DEFAULT 'N' NOT NULL,
    resolved_date       TIMESTAMP,
    notes               CLOB,
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

COMMENT ON TABLE alert_notifications IS 'Automated compliance alerts';

-- CREATE INDEXES

CREATE INDEX idx_entity_type ON financial_entities(entity_type);
CREATE INDEX idx_entity_status ON financial_entities(compliance_status);
CREATE INDEX idx_entity_risk ON financial_entities(risk_level);
CREATE INDEX idx_entity_active ON financial_entities(is_active);
CREATE INDEX idx_entity_review ON financial_entities(next_review_date);
CREATE INDEX idx_entity_name_upper ON financial_entities(UPPER(entity_name));
CREATE INDEX idx_entity_license_expiry ON financial_entities(license_expiry);

CREATE INDEX idx_violation_entity ON compliance_violations(entity_id);
CREATE INDEX idx_violation_type ON compliance_violations(violation_type);
CREATE INDEX idx_violation_severity ON compliance_violations(severity);
CREATE INDEX idx_violation_status ON compliance_violations(status);
CREATE INDEX idx_violation_date ON compliance_violations(violation_date);
CREATE INDEX idx_violation_unpaid ON compliance_violations(fine_paid, payment_due_date);

CREATE INDEX idx_audit_entity ON audit_trail(entity_id);
CREATE INDEX idx_audit_timestamp ON audit_trail(action_timestamp);
CREATE INDEX idx_audit_type ON audit_trail(action_type);
CREATE INDEX idx_audit_user ON audit_trail(performed_by);

CREATE INDEX idx_alert_entity ON alert_notifications(entity_id);
CREATE INDEX idx_alert_unack ON alert_notifications(acknowledged, alert_priority);
CREATE INDEX idx_alert_timestamp ON alert_notifications(alert_timestamp);
CREATE INDEX idx_alert_type ON alert_notifications(alert_type);

-- CREATE TRIGGERS

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

CREATE OR REPLACE TRIGGER trg_entity_modified
BEFORE UPDATE ON financial_entities
FOR EACH ROW
BEGIN
    :NEW.modified_date := SYSDATE;
    :NEW.modified_by := USER;
END;
/

CREATE OR REPLACE TRIGGER trg_violation_modified
BEFORE UPDATE ON compliance_violations
FOR EACH ROW
BEGIN
    :NEW.modified_date := SYSDATE;
    :NEW.modified_by := USER;
END;
/
