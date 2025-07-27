System Architecture Overview

1. High-Level Architecture
┌────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │        Oracle APEX Dashboard (Port 443/HTTPS)        │  │
│  │  • Entity Management Pages                           │  │
│  │  • Violation Tracking Interface                      │  │
│  │  • Compliance Reports & Charts                       │  │
│  │  • Alert Notification Center                         │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
                         ↕ HTTPS/REST API
┌────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │      Java REST API (Servlet Container)               │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  ComplianceAPIController                       │  │  │
│  │  │  • GET  /api/entities                          │  │  │
│  │  │  • POST /api/entities                          │  │  │
│  │  │  • GET  /api/violations                        │  │  │
│  │  │  • POST /api/violations                        │  │  │
│  │  │  • GET  /api/dashboard                         │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  FinancialEntityService                        │  │  │
│  │  │  • registerEntity()                            │  │  │
│  │  │  • updateComplianceStatus()                    │  │  │
│  │  │  • recordViolation()                           │  │  │
│  │  │  • generateReport()                            │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
                         ↕ JDBC (Oracle UCP)
┌────────────────────────────────────────────────────────────┐
│                      Database Layer                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │        Oracle Database 23c Free                      │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  PL/SQL Packages                               │  │  │
│  │  │  • pkg_compliance_mgmt                         │  │  │
│  │  │  • pkg_alert_engine                            │  │  │
│  │  │  • pkg_reporting                               │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Tables                                        │  │  │
│  │  │  • financial_entities                          │  │  │
│  │  │  • compliance_violations                       │  │  │
│  │  │  • audit_trail                                 │  │  │
│  │  │  • alert_notifications                         │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
2. Layer Responsibilities

2.1 Presentation Layer (Oracle APEX)
Purpose: User interface for DFS compliance officers

Responsibilities:
- Display dashboards and reports
- Provide forms for data entry
- Navigate between pages
- Export data to Excel/PDF
- Display charts and visualizations

Technology: Oracle APEX 24.1

Key Pages:
1. Dashboard (Page 1) - Overview statistics
2. Entity Management (Page 2) - CRUD operations
3. Violation Tracking (Page 5) - Search and filter
4. Alert Center (Page 8) - Notifications



2.2 Application Layer (Java)
Purpose: Business logic and REST API

Responsibilities:
- Validate input data
- Enforce business rules
- Coordinate database operations
- Handle errors and exceptions
- Log activities
- Format responses

Technology: Java 11 + Servlet 4.0 + Gson

Components:
1. Controllers: HTTP request handling
2. Services: Business logic implementation
3. Utilities: Connection management, logging



2.3 Database Layer (Oracle)
Purpose: Data persistence and business logic

Responsibilities:
- Store all system data
- Enforce data integrity (constraints, FKs)
- Execute complex business logic (PL/SQL)
- Generate automated alerts
- Maintain audit trail
- Optimize query performance

Technology: Oracle Database 23c Free

Components:
1. Tables: 4 core tables
2. Packages: 3 PL/SQL packages
3. Sequences: Auto-increment IDs
4. Indexes:Query optimization
5. Triggers: Audit immutability



3. Design Patterns Used

3.1 MVC (Model-View-Controller)
View:       APEX Pages
Controller: ComplianceAPIController.java
Model:      FinancialEntityService.java + Database Tables


3.2 Service Layer Pattern
Controller → Service → Database
- Controller handles HTTP
- Service implements business logic
- Database persists data


3.3 DAO Pattern (Implicit)
Service methods abstract database operations
- registerEntity() hides INSERT complexity
- searchEntities() hides query details


 3.4 Singleton (Connection Pool)
One shared connection pool across application
- Managed by Oracle UCP
- Efficient resource utilization




4. Data Flow Examples

Example 1: Register New Entity
┌─────────┐         ┌──────────┐         ┌─────────┐         ┌──────────┐
│  APEX   │         │   REST   │         │ Service │         │ Database │
│  Page   │         │   API    │         │  Layer  │         │          │
└────┬────┘         └─────┬────┘         └────┬────┘         └────┬─────┘
     │                    │                   │                    │
     │ 1. Submit Form     │                   │                    │
     │───────────────────>│                   │                    │
     │                    │ 2. Validate Input │                    │
     │                    │──────────────────>│                    │
     │                    │                   │ 3. INSERT entity   │
     │                    │                   │───────────────────>│
     │                    │                   │ 4. entity_id=1001  │
     │                    │                   │<───────────────────│
     │                    │                   │ 5. INSERT audit    │
     │                    │                   │───────────────────>│
     │                    │                   │ 6. INSERT alert    │
     │                    │                   │───────────────────>│
     │                    │ 7. Return Success │                    │
     │                    │<──────────────────│                    │
     │ 8. Show Success    │                   │                    │
     │<───────────────────│                   │                    │

     Example 2: Record Violation (with Escalation)

User submits violation form
   ↓
REST API validates data
   ↓
Service.recordViolation()
   ↓
INSERT into compliance_violations
   ↓
If severity = HIGH or CRITICAL:
   ↓
   Service.escalateRiskLevel()
   ↓
   UPDATE financial_entities SET risk_level = ...
   ↓
INSERT into audit_trail (action: VIOLATION_RECORDED)
   ↓
INSERT into alert_notifications (priority: HIGH)
   ↓
Return success to user


5. Security Architecture

5.1 Authentication (Future Phase)
- User login via APEX authentication scheme
- Session management with timeout
- Role-based access control (RBAC)

5.2 Input Validation

Layer 1: Client-side (APEX validations)
Layer 2: REST API (Java validations)
Layer 3: Database (CHECK constraints)


5.3 SQL Injection Prevention
- Prepared statements only (no string concatenation)
- Bind variables in all queries
- Input sanitization

5.4 Audit Trail
- Every action logged with timestamp, user, before/after
- Audit records are immutable (trigger prevents modification)
- 7-year retention for regulatory compliance



6. Performance Optimization
6.1 Database Level
- Strategic indexes on high-frequency columns
- Connection pooling (Oracle UCP)
- Query optimization with EXPLAIN PLAN
- Partitioning for large tables (future)

6.2 Application Level
- Efficient SQL queries (no SELECT *)
- Batch operations where possible
- Caching frequently accessed data (future)

6.3 Network Level
- HTTPS for encryption (small overhead)
- JSON compression for large payloads (future)
- CDN for static assets (future)


7. Scalability Strategy

7.1 Current Capacity (Phase 1)
- Entities: 100-1,000
- Concurrent Users: 10-50
- Transactions/day: 1,000-10,000

7.2 Future Capacity (5 years)
-*Entities: 10,000+
- Concurrent Users: 100-200
- Transactions/day: 100,000+

7.3 Scaling Approaches

Vertical Scaling:
- Increase database server resources
- More RAM for connection pool
- Faster storage (SSD → NVMe)

Horizontal Scaling (future):
- Application server clustering
- Load balancer for REST API
- Database read replicas

Data Scaling:
- Table partitioning by year
- Archive old data to separate tablespace
- Materialized views for reporting


8. Deployment Architecture
┌─────────────────────────────────────────────┐
│         Production Environment              │
├─────────────────────────────────────────────┤
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │   Web/Application Server              │ │
│  │   • Apache Tomcat 10                  │ │
│  │   • Java 11 Runtime                   │ │
│  │   • dfs-compliance.war                │ │
│  └───────────────────────────────────────┘ │
│                     ↕                       │
│  ┌───────────────────────────────────────┐ │
│  │   Oracle Database Server              │ │
│  │   • Oracle 23c Free                   │ │
│  │   • APEX 24.1                         │ │
│  │   • Listener Port 1521                │ │
│  └───────────────────────────────────────┘ │
│                     ↕                       │
│  ┌───────────────────────────────────────┐ │
│  │   Backup Storage                      │ │
│  │   • Daily backups                     │ │
│  │   • Archive logs                      │ │
│  └───────────────────────────────────────┘ │
│                                             │
└─────────────────────────────────────────────┘
9. Disaster Recovery

Recovery Point Objective (RPO): 1 hour  
Recovery Time Objective (RTO): 4 hours

Backup Strategy:
- Full backup: Daily at 2 AM ET
- Incremental: Every 6 hours
- Archive logs: Continuous

Recovery Steps:
1. Restore database from latest backup
2. Apply archive logs for point-in-time recovery
3. Restart application server
4. Verify data integrity
5. Resume operations





10. Monitoring & Logging

10.1 Application Monitoring
- SLF4J + Logback for application logs
- Log levels: ERROR, WARN, INFO, DEBUG
- Log rotation daily, keep 30 days

10.2 Database Monitoring
- Oracle Enterprise Manager (future)
- Custom monitoring queries
- Alert on tablespace usage > 80%

10.3 Performance Monitoring
- Response time tracking for API endpoints
- Slow query log (queries > 3 seconds)
- Connection pool metrics



11. Future Enhancements

Phase 2
- [ ] Redis caching layer
- [ ] Message queue for async processing
- [ ] Elasticsearch for advanced search
- [ ] Document storage (S3-compatible)

Phase 3
- [ ] Microservices architecture
- [ ] Kubernetes orchestration
- [ ] GraphQL API
- [ ] Real-time notifications (WebSocket)



Document Status: Complete  
Last Review: July 27, 2025  
Architecture Approval: Pending