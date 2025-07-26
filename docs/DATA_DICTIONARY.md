Table: FINANCIAL_ENTITIES

Description: Central registry of all financial institutions regulated by DFS

| # | Column Name | Data Type | Null? | Default | Description |
|---|-------------|-----------|-------|---------|-------------|
| 1 | entity_id | NUMBER | NOT NULL | entity_seq.NEXTVAL | Primary key, unique identifier |
| 2 | entity_name | VARCHAR2(255) | NOT NULL | | Legal name of institution |
| 3 | entity_type | VARCHAR2(50) | NOT NULL | | Type of institution (see domain values) |
| 4 | nmls_id | VARCHAR2(50) | NULL | | National Mortgage Licensing System ID |
| 5 | dba_name | VARCHAR2(255) | NULL | | Doing Business As name (if different from legal name) |
| 6 | primary_contact | VARCHAR2(255) | NULL | | Name of primary contact person |
| 7 | contact_email | VARCHAR2(255) | NULL | | Primary contact email address |
| 8 | contact_phone | VARCHAR2(50) | NULL | | Primary contact phone number |
| 9 | address_line1 | VARCHAR2(255) | NULL | | Street address line 1 |
| 10 | address_line2 | VARCHAR2(255) | NULL | | Street address line 2 (suite, floor, etc.) |
| 11 | city | VARCHAR2(100) | NULL | | City |
| 12 | state | VARCHAR2(2) | NULL | 'NY' | State code (2-letter) |
| 13 | zip_code | VARCHAR2(10) | NULL | | ZIP or ZIP+4 code |
| 14 | registration_date | DATE | NOT NULL | SYSDATE | Date entity was registered in system |
| 15 | license_number | VARCHAR2(100) | NULL | | DFS-issued license number |
| 16 | license_expiry | DATE | NULL | | License expiration date |
| 17 | compliance_status | VARCHAR2(50) | NOT NULL | 'PENDING_REVIEW' | Current compliance status (see domain values) |
| 18 | risk_level | VARCHAR2(20) | NOT NULL | 'MEDIUM' | Risk assessment level (see domain values) |
| 19 | last_review_date | DATE | NULL | | Date of last compliance review |
| 20 | next_review_date | DATE | NULL | | Scheduled date for next review |
| 21 | total_assets | NUMBER(18,2) | NULL | | Total assets in USD |
| 22 | employee_count | NUMBER | NULL | | Number of employees |
| 23 | is_active | CHAR(1) | NOT NULL | 'Y' | Active status flag (Y/N) |
| 24 | notes | CLOB | NULL | | Additional notes or comments |
| 25 | created_date | DATE | NOT NULL | SYSDATE | Record creation timestamp |
| 26 | created_by | VARCHAR2(100) | NOT NULL | USER | User who created record |
| 27 | modified_date | DATE | NULL | | Last modification timestamp |
| 28 | modified_by | VARCHAR2(100) | NULL | | User who last modified record |

Primary Key: entity_id  
Sequences: entity_seq (starts at 1000)  
Indexes: idx_entity_type, idx_entity_status, idx_entity_risk, idx_entity_active, idx_entity_review, idx_entity_name

Domain Values:

entity_type:
- `BANK` - Commercial or community bank
- `INSURANCE` - Insurance company (property, casualty, life)
- `MSB` - Money Service Business (money transmitters, currency exchange)
- `FINTECH` - Financial technology company (digital payments, crypto)
- `CREDIT_UNION` - Member-owned credit union
- `BROKER_DEALER` - Securities broker or dealer

compliance_status:
- `COMPLIANT` - Meeting all regulatory requirements
- `NON_COMPLIANT` - Active violations identified
- `PENDING_REVIEW` - Awaiting compliance review
- `UNDER_INVESTIGATION` - Active investigation in progress
- `PROBATION` - Conditional operating status with restrictions
- `SUSPENDED` - Operations temporarily suspended

risk_level:
- `LOW` - Minimal regulatory concerns, good compliance history
- `MEDIUM` - Some issues noted, standard monitoring
- `HIGH` - Significant violations or concerns, elevated scrutiny
- `CRITICAL` - Severe violations, potential license revocation


 Table: COMPLIANCE_VIOLATIONS

Description: Record of all regulatory violations and enforcement actions

|   | Column Name | Data Type | Null? | Default | Description |
|---|-------------|-----------|-------|---------|-------------|
| 1 | violation_id | NUMBER | NOT NULL | violation_seq.NEXTVAL | Primary key |
| 2 | entity_id | NUMBER | NOT NULL | | Foreign key to financial_entities |
| 3 | violation_type | VARCHAR2(100) | NOT NULL | | Classification/category of violation |
| 4 | violation_code | VARCHAR2(50) | NULL | | Regulatory code reference (e.g., 23 NYCRR 500.02) |
| 5 | description | CLOB | NOT NULL | | Detailed description of violation |
| 6 | severity | VARCHAR2(20) | NOT NULL | | Severity level (LOW, MEDIUM, HIGH, CRITICAL) |
| 7 | violation_date | DATE | NOT NULL | | Date violation occurred |
| 8 | discovery_date | DATE | NOT NULL | SYSDATE | Date violation was discovered |
| 9 | reported_by | VARCHAR2(100) | NULL | | Person/entity who reported violation |
| 10 | fine_amount | NUMBER(18,2) | NOT NULL | 0 | Monetary penalty assessed (USD) |
| 11 | fine_paid | CHAR(1) | NOT NULL | 'N' | Whether fine has been paid (Y/N) |
| 12 | payment_due_date | DATE | NULL | | Deadline for fine payment |
| 13 | payment_date | DATE | NULL | | Actual date fine was paid |
| 14 | status | VARCHAR2(50) | NOT NULL | 'UNDER_REVIEW' | Current status of violation |
| 15 | resolution_date | DATE | NULL | | Date violation was resolved |
| 16 | resolution_notes | CLOB | NULL | | Details of resolution |
| 17 | corrective_action | CLOB | NULL | | Actions taken by entity to remedy |
| 18 | follow_up_required | CHAR(1) | NOT NULL | 'Y' | Whether follow-up needed (Y/N) |
| 19 | follow_up_date | DATE | NULL | | Date of scheduled follow-up |
| 20 | created_date | DATE | NOT NULL | SYSDATE | Record creation timestamp |
| 21 | created_by | VARCHAR2(100) | NOT NULL | USER | User who created record |
| 22 | modified_date | DATE | NULL | | Last modification timestamp |
| 23 | modified_by | VARCHAR2(100) | NULL | | User who last modified record |

Primary Key: violation_id  
Foreign Keys: entity_id → financial_entities(entity_id)  
Sequences: violation_seq (starts at 5000)  
Indexes: idx_violation_entity, idx_violation_type, idx_violation_severity, idx_violation_status, idx_violation_date, idx_violation_unpaid



Domain Values:

severity:
- `LOW` - Minor violation, administrative in nature
- `MEDIUM` - Moderate violation requiring corrective action
- `HIGH` - Serious violation, significant regulatory concern
- `CRITICAL` - Severe violation, potential license suspension

status:
- `UNDER_REVIEW` - Violation is being investigated
- `CONFIRMED` - Violation confirmed, entity notified
- `RESOLVED` - Violation remediated and closed
- `APPEALED` - Entity has filed an appeal
- `DISMISSED` - Violation determined to be unfounded

Common violation_type examples:
- AML Deficiency (Anti-Money Laundering)
- BSA Violation (Bank Secrecy Act)
- Consumer Protection Violation
- Record Retention Violation
- Licensing Violation
- Cybersecurity Incident
- Unfair/Deceptive Practices



Table: AUDIT_TRAIL

Description: Complete immutable log of all system activities

|   | Column Name | Data Type | Null? | Default | Description |
|---|-------------|-----------|-------|---------|-------------|
| 1 | audit_id | NUMBER | NOT NULL | audit_seq.NEXTVAL | Primary key |
| 2 | entity_id | NUMBER | NULL | | Related entity (null for system-level actions) |
| 3 | action_type | VARCHAR2(100) | NOT NULL | | Type of action performed |
| 4 | action_details | CLOB | NULL | | Detailed description of action |
| 5 | action_timestamp | TIMESTAMP | NOT NULL | SYSTIMESTAMP | Precise time action occurred |
| 6 | performed_by | VARCHAR2(100) | NOT NULL | USER | User who performed action |
| 7 | ip_address | VARCHAR2(50) | NULL | | Source IP address |
| 8 | session_id | VARCHAR2(100) | NULL | | Session identifier |
| 9 | before_value | CLOB | NULL | | State before change (for updates) |
| 10 | after_value | CLOB | NULL | | State after change (for updates) |

Primary Key: audit_id  
Foreign Keys: entity_id → financial_entities(entity_id) [nullable]  
Sequences: audit_seq (starts at 10000)  
Indexes: idx_audit_entity, idx_audit_timestamp, idx_audit_type, idx_audit_user

Special Notes:
- Records are IMMUTABLE - no updates or deletes allowed
- Trigger prevents modification attempts
- Retention: 7 years minimum (regulatory requirement)

Common action_type examples:
- ENTITY_REGISTERED
- STATUS_UPDATED
- VIOLATION_RECORDED
- RISK_ESCALATED
- LICENSE_RENEWED
- ENTITY_DEACTIVATED
- USER_LOGIN
- REPORT_GENERATED


Table: ALERT_NOTIFICATIONS

Description: Automated compliance alerts for monitoring officers

| # | Column Name | Data Type | Null? | Default | Description |
|---|-------------|-----------|-------|---------|-------------|
| 1 | alert_id | NUMBER | NOT NULL | alert_seq.NEXTVAL | Primary key |
| 2 | entity_id | NUMBER | NOT NULL | | Foreign key to financial_entities |
| 3 | alert_type | VARCHAR2(50) | NOT NULL | | Classification of alert |
| 4 | alert_priority | VARCHAR2(20) | NOT NULL | | Priority level (LOW, MEDIUM, HIGH, URGENT) |
| 5 | alert_message | VARCHAR2(1000) | NOT NULL | | Alert message text |
| 6 | alert_timestamp | TIMESTAMP | NOT NULL | SYSTIMESTAMP | When alert was generated |
| 7 | acknowledged | CHAR(1) | NOT NULL | 'N' | Whether alert acknowledged (Y/N) |
| 8 | acknowledged_by | VARCHAR2(100) | NULL | | User who acknowledged |
| 9 | acknowledged_date | TIMESTAMP | NULL | | When alert was acknowledged |
| 10 | resolved | CHAR(1) | NOT NULL | 'N' | Whether issue resolved (Y/N) |
| 11 | resolved_date | TIMESTAMP | NULL | | When issue was resolved |
| 12 | notes | CLOB | NULL | | Additional notes or follow-up |

Primary Key: alert_id  
Foreign Keys: entity_id → financial_entities(entity_id)  
Sequences: alert_seq (starts at 20000)  
Indexes: idx_alert_entity, idx_alert_unack, idx_alert_timestamp

Domain Values:

alert_type:
- `NEW_REGISTRATION` - New entity registered, needs review
- `VIOLATION` - New violation recorded
- `OVERDUE_VIOLATION` - Violation unresolved for 60+ days
- `LICENSE_EXPIRING` - License expiring within 30 days
- `REVIEW_DUE` - Scheduled compliance review due
- `RISK_ESCALATION` - Risk level increased
- `STATUS_CHANGE` - Compliance status changed

alert_priority:
- `LOW` - Informational, no immediate action required
- `MEDIUM` - Action needed within 1 week
- `HIGH` - Action needed within 24 hours
- `URGENT` - Immediate attention required (critical violations)


Sequences
| Sequence Name | Start Value | Increment | Description |
|--------------|-------------|-----------|-------------|
| entity_seq | 1000 | 1 | Primary key for financial_entities |
| violation_seq | 5000 | 1 | Primary key for compliance_violations |
| audit_seq | 10000 | 1 | Primary key for audit_trail |
| alert_seq | 20000 | 1 | Primary key for alert_notifications |
Rationale for Start Values:**
- Different starting points prevent ID confusion
- Easy to identify record type from ID alone
- Allows room for manual test data (if needed)

Naming Conventions

Tables: Plural nouns, lowercase with underscores  
Columns:Lowercase with underscores, descriptive names  
Primary Keys: `table_name_id` format  
Foreign Keys: References primary key name  
Indexes: `idx_table_column` format  
Sequences: `table_seq` format  
Constraints: `chk_table_column` or `fk_table_fktable`