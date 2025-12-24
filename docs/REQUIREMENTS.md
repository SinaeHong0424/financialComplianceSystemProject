System Requirements Document

Project: DFS Financial Compliance Management System
Version: 1.0


Business Requirements

1: Entity Management
Priority: HIGH
Description: The system shall maintain a comprehensive registry of all financial entities regulated by DFS.

Acceptance Criteria:

Store entity name, legal type, and contact information

Support 6 entity types: Bank, Insurance, MSB, FinTech, Credit Union, Broker-Dealer

Track NMLS ID for applicable entities

Record registration date automatically

Support entity search by name, type, or status

2: Compliance Status Tracking
Priority: HIGH
Description: The system shall track and manage compliance status for each entity.

Status Values:

COMPLIANT - Meeting all regulatory requirements

NON_COMPLIANT - Violations identified

PENDING_REVIEW - Awaiting compliance review

UNDER_INVESTIGATION - Active investigation in progress

PROBATION - Conditional operating status

SUSPENDED - Operations suspended

3: Violation Management
Priority: HIGH
Description: The system shall record, track, and manage regulatory violations.

Required Information:

Violation type and detailed description

Severity level (Low, Medium, High, Critical)

Fine amount and payment status

Discovery date and reporting source

Resolution status and corrective actions

Follow-up requirements

4: Risk Assessment
Priority: HIGH
Description: The system shall assess and track entity risk levels.

Risk Levels:

LOW - Minimal regulatory concerns

MEDIUM - Some compliance issues, monitoring required

HIGH - Significant violations, elevated scrutiny

CRITICAL - Severe violations, potential suspension

Automatic Escalation:

High/Critical violations automatically escalate risk level

Risk level affects review frequency

5: Audit Trail
Priority: CRITICAL
Description: The system shall maintain a complete, immutable audit trail.

Audit Requirements:

Log ALL system actions (create, update, delete)

Capture timestamp with millisecond precision

Record user performing action

Store before/after values for changes

Support audit queries by entity, user, date, action type

Retention: 7 years minimum (regulatory requirement)

6: Automated Alerts
Priority: MEDIUM
Description: The system shall generate automated alerts for compliance officers.

Alert Types:

New entity registration requiring initial review

High-risk entities needing attention

Violations unresolved for 60+ days

License expiration within 30 days

Required compliance review dates

Alert Priorities:

URGENT - Immediate action required (Critical violations)

HIGH - Action needed within 24 hours

MEDIUM - Action needed within 1 week

LOW - Informational, no immediate action




Technical Requirements

1: Database Platform
Requirement: Oracle Database 19c or higher

Specifications:

Support for PL/SQL packages and procedures

Transaction management with ACID properties

Referential integrity enforcement

Sequence-based ID generation

TIMESTAMP precision for audit logs

2: Backend Platform
Requirement: Java 11+ with Jakarta EE

Specifications:

RESTful API design principles

Servlet-based HTTP handling

JDBC for database connectivity

Prepared statements (SQL injection prevention)

Connection pooling for performance

JSON request/response format (Gson)

3: REST API Endpoints

Entity Management:

GET /api/entities - List all entities (with filters)

GET /api/entities/{id} - Get entity details

POST /api/entities - Register new entity

PUT /api/entities/{id}/status - Update compliance status

Violation Management:

GET /api/violations - List violations (with filters)

POST /api/violations - Record new violation

PUT /api/violations/{id} - Update violation status

Dashboard:

GET /api/dashboard - Get dashboard statistics

GET /api/high-priority - Get entities requiring attention

4: Security Requirements

Input Validation:

Validate all user inputs

Sanitize data before database insertion

Use parameterized queries (no string concatenation)

Authentication & Authorization:

User authentication (Phase 2)

Role-based access control

Session management with timeout

Data Protection:

HTTPS for all connections

Sensitive data encryption (future)

SQL injection prevention

5: Performance Requirements

Response Time:

API endpoints: < 2 seconds (95th percentile)

Dashboard queries: < 3 seconds

Simple lookups: < 500ms

Concurrency:

Support 100+ concurrent users

Database connection pooling (min 10, max 50)

Scalability:

Handle 10,000+ entities

Support 100,000+ audit records

Efficient indexing for large datasets

6: User Interface (Oracle APEX)

Dashboard Requirements:

Real-time statistics (entity counts, violation trends)

Interactive charts (risk distribution, compliance status)

Drill-down capability to entity details

Entity Management Pages:

Interactive Grid for CRUD operations

Search and filter functionality

Inline editing where appropriate

Export to Excel capability

Responsive Design:

Support desktop browsers (Chrome, Firefox, Safari, Edge)

Tablet compatibility

Print-friendly reports




Non-Functional Requirements

1: Availability
Target: 99.5% uptime during business hours (8 AM - 6 PM ET, Mon-Fri)

2: Data Retention

Data Type	Retention Period
Audit logs	7 years
Violation records	Permanent
Entity records	Until deactivation + 10 years
Alert notifications	2 years after resolution

3: Backup & Recovery

Daily backups at 2 AM ET

Point-in-time recovery capability

Recovery Time Objective (RTO): 4 hours

Recovery Point Objective (RPO): 24 hours

4: Documentation

Technical documentation for developers

User guide for compliance officers

API documentation with examples

Database schema documentation

Installation and deployment guide

5: Testing

Unit test coverage: 80%+ for Java code

Integration testing for API endpoints

Database testing for PL/SQL packages

User acceptance testing (UAT) before deployment

Out of Scope (Phase 1)

The following features are explicitly excluded from Phase 1:

- Email notification system
- Document upload/attachment management
- Advanced analytics and ML predictions
- Mobile application
- Integration with external credit bureaus
- Workflow automation engine

These may be considered for future phases.

Assumptions & Dependencies

Assumptions

DFS compliance officers have basic computer literacy

Oracle Database is already installed and configured

Network connectivity is reliable

User training will be provided separately

Dependencies

Oracle Database 19c+ availability

Java 11+ runtime environment

Maven build tool for compilation

Oracle APEX workspace provisioned

GitHub for version control

Success Criteria

The project is considered successful when:

-All High-priority business requirements implemented
-REST API functional with documented endpoints
-Oracle APEX dashboard operational
-80%+ test coverage achieved
-Complete technical documentation
-Successfully demonstrates DFS alignment
-Zero critical bugs in production
-Performance requirements met