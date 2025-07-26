Database Design Documentation

Project: DFS Financial Compliance Management System
Version: 1.0
Database: Oracle 26c Free

1.Overview

The database is designed to support comprehensive compliance tracking with emphasis on:

Data Integrity - Enforced through constraints and foreign keys

Auditability - Complete history of all system actions

Performance - Strategic indexing for common query patterns

Scalability - Designed to handle growing data volumes




2.Entity-Relationship Model

2.1 Core Entities

FINANCIAL_ENTITIES (Main Registry)
Purpose: Central registry of all regulated financial institutions
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| entity_id | NUMBER | PK | Unique identifier (sequence-generated) |
| entity_name | VARCHAR2(255) | NOT NULL | Legal name of institution |
| entity_type | VARCHAR2(50) | NOT NULL, CHECK | Type: BANK, INSURANCE, MSB, etc. |
| nmls_id | VARCHAR2(50) | | National Mortgage Licensing System ID |
| dba_name | VARCHAR2(255) | | Doing Business As name |
| contact_email | VARCHAR2(255) | | Primary contact email |
| contact_phone | VARCHAR2(50) | | Primary contact phone |
| address_line1 | VARCHAR2(255) | | Street address |
| city | VARCHAR2(100) | | City |
| state | VARCHAR2(2) | DEFAULT 'NY' | State (NY for DFS) |
| zip_code | VARCHAR2(10) | | ZIP code |
| registration_date | DATE | NOT NULL | Date registered in system |
| license_number | VARCHAR2(100) | | Operating license number |
| license_expiry | DATE | | License expiration date |
| compliance_status | VARCHAR2(50) | NOT NULL, CHECK | Current compliance state |
| risk_level | VARCHAR2(20) | NOT NULL, CHECK | LOW, MEDIUM, HIGH, CRITICAL |
| total_assets | NUMBER(18,2) | | Total assets in USD |
| employee_count | NUMBER | | Number of employees |
| is_active | CHAR(1) | DEFAULT 'Y' | Active flag (Y/N) |
Check Constraints:
'''sql
entity_type IN ('BANK', 'INSURANCE', 'MSB', 'FINTECH', 'CREDIT_UNION', 'BROKER_DEALER')
compliance_status IN ('COMPLIANT', 'NON_COMPLIANT', 'PENDING_REVIEW', 'UNDER_INVESTIGATION', 'PROBATION', 'SUSPENDED')
risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')


COMPLIANCE_VIOLATIONS (Violation Tracking)
Purpose: Record and track all regulatory violations
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| violation_id | NUMBER | PK | Unique identifier |
| entity_id | NUMBER | FK, NOT NULL | Reference to entity |
| violation_type | VARCHAR2(100) | NOT NULL | Classification of violation |
| violation_code | VARCHAR2(50) | | Regulatory code reference |
| description | CLOB | NOT NULL | Detailed description |
| severity | VARCHAR2(20) | NOT NULL, CHECK | LOW, MEDIUM, HIGH, CRITICAL |
| violation_date | DATE | NOT NULL | Date violation occurred |
| discovery_date | DATE | DEFAULT SYSDATE | Date violation discovered |
| reported_by | VARCHAR2(100) | | Who reported the violation |
| fine_amount | NUMBER(18,2) | DEFAULT 0 | Monetary penalty |
| fine_paid | CHAR(1) | DEFAULT 'N' | Payment status (Y/N) |
| payment_due_date | DATE | | Fine payment deadline |
| payment_date | DATE | | Actual payment date |
| status | VARCHAR2(50) | NOT NULL, CHECK | Resolution status |
| resolution_date | DATE | | Date resolved |
| resolution_notes | CLOB | | Resolution details |
| corrective_action | CLOB | | Actions taken to remedy |
| follow_up_required | CHAR(1) | DEFAULT 'Y' | Follow-up needed (Y/N) |
| follow_up_date | DATE | | Scheduled follow-up date |
Check Constraints:
```sql
severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
status IN ('UNDER_REVIEW', 'CONFIRMED', 'RESOLVED', 'APPEALED', 'DISMISSED')
```

AUDIT_TRAIL (Complete Activity Log)
Purpose: Maintain immutable record of all system actions
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| audit_id | NUMBER | PK | Unique identifier |
| entity_id | NUMBER | FK | Related entity (nullable) |
| action_type | VARCHAR2(100) | NOT NULL | Type of action performed |
| action_details | CLOB | | Detailed description |
| action_timestamp | TIMESTAMP | DEFAULT SYSTIMESTAMP | When action occurred |
| performed_by | VARCHAR2(100) | DEFAULT USER | Who performed action |
| ip_address | VARCHAR2(50) | | Source IP address |
| session_id | VARCHAR2(100) | | Session identifier |
| before_value | CLOB | | State before change |
| after_value | CLOB | | State after change |
Immutability: No UPDATE or DELETE allowed on audit records

ALERT_NOTIFICATIONS (Automated Alerts)
Purpose: Generate and track compliance alerts

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| alert_id | NUMBER | PK | Unique identifier |
| entity_id | NUMBER | FK, NOT NULL | Related entity |
| alert_type | VARCHAR2(50) | NOT NULL | Classification of alert |
| alert_priority | VARCHAR2(20) | NOT NULL, CHECK | LOW, MEDIUM, HIGH, URGENT |
| alert_message | VARCHAR2(1000) | NOT NULL | Alert message text |
| alert_timestamp | TIMESTAMP | DEFAULT SYSTIMESTAMP | When alert generated |
| acknowledged | CHAR(1) | DEFAULT 'N' | Acknowledgment status |
| acknowledged_by | VARCHAR2(100) | | Who acknowledged |
| acknowledged_date | TIMESTAMP | | When acknowledged |
| resolved | CHAR(1) | DEFAULT 'N' | Resolution status |
| resolved_date | TIMESTAMP | | When resolved |
| notes | CLOB | | Additional notes |


3.Relationships
```
FINANCIAL_ENTITIES (1) ──< (N) COMPLIANCE_VIOLATIONS
FINANCIAL_ENTITIES (1) ──< (N) AUDIT_TRAIL
FINANCIAL_ENTITIES (1) ──< (N) ALERT_NOTIFICATIONS
Foreign Key Behavior:

ON DELETE: RESTRICT (prevent deletion of entity with violations)

ON UPDATE: CASCADE (propagate ID changes if needed)
```

4.Normalization Analysis

Third Normal Form (3NF) Compliance


5.Indexing Strategy
5.1 Primary Indexes (Automatic)

PK_FINANCIAL_ENTITIES on entity_id

PK_COMPLIANCE_VIOLATIONS on violation_id

PK_AUDIT_TRAIL on audit_id

PK_ALERT_NOTIFICATIONS on alert_id

5.2 Foreign Key Indexes
```sql
CREATE INDEX idx_violation_entity ON compliance_violations(entity_id);
CREATE INDEX idx_audit_entity ON audit_trail(entity_id);
CREATE INDEX idx_alert_entity ON alert_notifications(entity_id);
```
5.3 Query Optimization Indexes
High-Frequency Queries:
```sql
-- Filter by entity characteristics
CREATE INDEX idx_entity_type ON financial_entities(entity_type);
CREATE INDEX idx_entity_status ON financial_entities(compliance_status);
CREATE INDEX idx_entity_risk ON financial_entities(risk_level);
CREATE INDEX idx_entity_active ON financial_entities(is_active);

-- Time-based queries
CREATE INDEX idx_entity_review ON financial_entities(next_review_date);
CREATE INDEX idx_violation_date ON compliance_violations(violation_date);
CREATE INDEX idx_audit_timestamp ON audit_trail(action_timestamp);

-- Severity filtering
CREATE INDEX idx_violation_severity ON compliance_violations(severity);
CREATE INDEX idx_violation_status ON compliance_violations(status);

-- Alert management
CREATE INDEX idx_alert_unack ON alert_notifications(acknowledged, alert_priority);

-- Text search
CREATE INDEX idx_entity_name ON financial_entities(UPPER(entity_name));
```

### 5.4 Composite Indexes (Future Optimization)
```sql
-- For complex WHERE clauses
CREATE INDEX idx_violation_entity_date 
ON compliance_violations(entity_id, violation_date);

CREATE INDEX idx_entity_type_risk 
ON financial_entities(entity_type, risk_level);
```


6. Performance Considerations
6.1 Expected Query Patterns

Pattern 1: Find High-Risk Entities
```sql
SELECT entity_name, risk_level, compliance_status
FROM financial_entities
WHERE risk_level IN ('HIGH', 'CRITICAL')
  AND is_active = 'Y'
ORDER BY risk_level DESC;
```
Optimization: Uses idx_entity_risk and idx_entity_active

Pattern 2: Violations by Date Range
```sql
SELECT entity_id, violation_type, severity, fine_amount
FROM compliance_violations
WHERE violation_date BETWEEN :start_date AND :end_date
  AND severity = 'CRITICAL'
ORDER BY violation_date DESC;
Optimization: Uses idx_violation_date and idx_violation_severity

Pattern 3: Entity Compliance Report
```sql
SELECT e.entity_name, 
       COUNT(v.violation_id) as violation_count,
       SUM(v.fine_amount) as total_fines
FROM financial_entities e
LEFT JOIN compliance_violations v ON e.entity_id = v.entity_id
WHERE e.entity_id = :entity_id
GROUP BY e.entity_name;
```
Optimization: Uses primary key and idx_violation_entity

6.2 Query Performance Targets
| Query Type | Target Response Time |
|------------|---------------------|
| Simple lookup (by ID) | < 10ms |
| Filtered list (with indexes) | < 100ms |
| Complex JOIN (2-3 tables) | < 500ms |
| Aggregation query | < 1 second |
| Full report generation | < 3 seconds |



7.Data Integrity Constraints

7.1 Referential Integrity
```sql
-- Foreign keys ensure valid references
ALTER TABLE compliance_violations
ADD CONSTRAINT fk_violation_entity
FOREIGN KEY (entity_id) REFERENCES financial_entities(entity_id);
```
7.2 Check Constraints
```sql
-- Validate enumerated values
ALTER TABLE financial_entities
ADD CONSTRAINT chk_entity_type
CHECK (entity_type IN ('BANK', 'INSURANCE', 'MSB', 'FINTECH', 'CREDIT_UNION', 'BROKER_DEALER'));

-- Validate date logic
ALTER TABLE compliance_violations
ADD CONSTRAINT chk_violation_dates
CHECK (resolution_date IS NULL OR resolution_date >= violation_date);
```
7.3 NOT NULL Constraints
- Critical fields cannot be empty
- Examples: entity_name, violation_type, action_timestamp


8.Security Design

8.1 Row-Level Security (Future)
```sql
-- Planned: Restrict access based on user role
CREATE POLICY entity_access_policy
ON financial_entities
FOR SELECT
USING (created_by = USER OR USER IN (SELECT username FROM dfs_admins));
```

8.2 Audit Trail Immutability
```sql
-- Prevent modification of audit records
CREATE TRIGGER prevent_audit_modification
BEFORE UPDATE OR DELETE ON audit_trail
FOR EACH ROW
BEGIN
    RAISE_APPLICATION_ERROR(-20001, 'Audit trail records are immutable');
END;
```

8.3 Sensitive Data
- Passwords: NOT stored (authentication external)
- PII: Minimal storage, encrypted in production
- Financial data: Access logged in audit trail


9. Scalability Planning
9.1 Current Scale (Phase 1)
- **Entities:** 100-1,000
- **Violations:** 10-100 per entity
- **Audit records:** 1,000s
- **Alerts:** 100s active at any time

9.2 Future Scale (5 years)
-Entities: 10,000+
- Violations: 100,000+
- Audit records: 1,000,000+

9.3 Scalability Strategie

Partitioning:
```sql
-- Partition audit_trail by year
CREATE TABLE audit_trail (
    ...
)
PARTITION BY RANGE (action_timestamp) (
    PARTITION audit_2025 VALUES LESS THAN (TO_DATE('2026-01-01', 'YYYY-MM-DD')),
    PARTITION audit_2026 VALUES LESS THAN (TO_DATE('2027-01-01', 'YYYY-MM-DD'))
);
```

Archiving:
- Resolved violations older than 5 years → archive table
- Audit logs older than 7 years → cold storage

---

10. Data Retention Policy

| Table | Retention Period | Archive Strategy |
|-------|-----------------|------------------|
| financial_entities | Until deactivation + 10 years | Soft delete (is_active='N') |
| compliance_violations | Permanent | No deletion, archive old resolved |
| audit_trail | 7 years (regulatory) | Partition by year, drop old partitions |
| alert_notifications | 2 years after resolution | DELETE resolved alerts > 2 years old |


11. Backup Strategy

Frequency:
- Full backup: Daily at 2:00 AM ET
- Incremental backup: Every 6 hours
- Archive logs: Continuous

Retention:
- Daily backups: 30 days
- Weekly backups: 1 year
- Monthly backups: 7 years (compliance requirement)

Recovery Point Objective (RPO): 1 hour  
Recovery Time Objective (RTO): 4 hours


12. Future Enhancements
Phase 2 Features
- [ ] Document attachment table (store PDFs, images)
- [ ] Entity relationship table (parent/subsidiary)
- [ ] User management table (authentication)
- [ ] Scheduled review table (automated review calendar)
- [ ] Communication log table (emails, calls, meetings)

Performance Enhancements
- [ ] Materialized views for dashboard queries
- [ ] Function-based indexes for complex queries
- [ ] Database statistics collection job
- [ ] Query result caching