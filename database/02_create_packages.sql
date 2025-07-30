-- ============================================================================
-- PL/SQL Package Specifications and Bodies
-- ============================================================================
-- Description: Business logic packages for compliance management
-- Version: 1.0
-- Database: Oracle 26c Free
-- ============================================================================

SET SERVEROUTPUT ON

PROMPT ============================================================================
PROMPT Creating PL/SQL Packages...
PROMPT ============================================================================

-- ============================================================================
-- PACKAGE 1: PKG_COMPLIANCE_MGMT
-- Description: Core compliance management functions
-- ============================================================================

PROMPT Creating package PKG_COMPLIANCE_MGMT specification...

CREATE OR REPLACE PACKAGE pkg_compliance_mgmt AS
    
    -- Register new financial entity
    FUNCTION register_entity(
        p_entity_name       VARCHAR2,
        p_entity_type       VARCHAR2,
        p_nmls_id           VARCHAR2 DEFAULT NULL,
        p_contact_email     VARCHAR2 DEFAULT NULL,
        p_contact_phone     VARCHAR2 DEFAULT NULL
    ) RETURN NUMBER;
    
    -- Update compliance status
    PROCEDURE update_compliance_status(
        p_entity_id         NUMBER,
        p_new_status        VARCHAR2,
        p_notes             VARCHAR2 DEFAULT NULL
    );
    
    -- Record new violation
    FUNCTION record_violation(
        p_entity_id         NUMBER,
        p_violation_type    VARCHAR2,
        p_description       VARCHAR2,
        p_severity          VARCHAR2,
        p_fine_amount       NUMBER DEFAULT 0
    ) RETURN NUMBER;
    
    -- Calculate compliance score
    FUNCTION calculate_compliance_score(
        p_entity_id         NUMBER,
        p_months_back       NUMBER DEFAULT 12
    ) RETURN NUMBER;
    
    -- Get entity risk level
    FUNCTION get_risk_level(
        p_entity_id         NUMBER
    ) RETURN VARCHAR2;
    
    -- Update entity risk level
    PROCEDURE update_risk_level(
        p_entity_id         NUMBER,
        p_new_risk_level    VARCHAR2,
        p_reason            VARCHAR2 DEFAULT NULL
    );
    
END pkg_compliance_mgmt;
/

PROMPT Package specification PKG_COMPLIANCE_MGMT created successfully.

-- ============================================================================
-- PACKAGE BODY 1: PKG_COMPLIANCE_MGMT
-- ============================================================================

PROMPT Creating package PKG_COMPLIANCE_MGMT body...

CREATE OR REPLACE PACKAGE BODY pkg_compliance_mgmt AS
    
    -- --------------------------------------------------------------------
    -- Function: register_entity
    -- Description: Register new financial entity in the system
    -- --------------------------------------------------------------------
    FUNCTION register_entity(
        p_entity_name       VARCHAR2,
        p_entity_type       VARCHAR2,
        p_nmls_id           VARCHAR2 DEFAULT NULL,
        p_contact_email     VARCHAR2 DEFAULT NULL,
        p_contact_phone     VARCHAR2 DEFAULT NULL
    ) RETURN NUMBER IS
        v_entity_id         NUMBER;
        v_audit_id          NUMBER;
    BEGIN
        -- Validate inputs
        IF p_entity_name IS NULL OR TRIM(p_entity_name) = '' THEN
            RAISE_APPLICATION_ERROR(-20001, 'Entity name cannot be empty');
        END IF;
        
        IF p_entity_type NOT IN ('BANK', 'INSURANCE', 'MSB', 'FINTECH', 'CREDIT_UNION', 'BROKER_DEALER') THEN
            RAISE_APPLICATION_ERROR(-20002, 'Invalid entity type');
        END IF;
        
        -- Generate entity ID
        SELECT entity_seq.NEXTVAL INTO v_entity_id FROM dual;
        
        -- Insert entity
        INSERT INTO financial_entities (
            entity_id,
            entity_name,
            entity_type,
            nmls_id,
            contact_email,
            contact_phone,
            registration_date,
            compliance_status,
            risk_level,
            is_active
        ) VALUES (
            v_entity_id,
            TRIM(p_entity_name),
            p_entity_type,
            p_nmls_id,
            p_contact_email,
            p_contact_phone,
            SYSDATE,
            'PENDING_REVIEW',
            'MEDIUM',
            'Y'
        );
        
        -- Create audit trail entry
        SELECT audit_seq.NEXTVAL INTO v_audit_id FROM dual;
        INSERT INTO audit_trail (
            audit_id,
            entity_id,
            action_type,
            action_details,
            performed_by
        ) VALUES (
            v_audit_id,
            v_entity_id,
            'ENTITY_REGISTERED',
            'New entity registered: ' || p_entity_name || ' (Type: ' || p_entity_type || ')',
            USER
        );
        
        -- Create alert for review
        INSERT INTO alert_notifications (
            alert_id,
            entity_id,
            alert_type,
            alert_priority,
            alert_message
        ) VALUES (
            alert_seq.NEXTVAL,
            v_entity_id,
            'NEW_REGISTRATION',
            'MEDIUM',
            'New ' || p_entity_type || ' entity registered: ' || p_entity_name
        );
        
        COMMIT;
        RETURN v_entity_id;
        
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END register_entity;
    
    -- --------------------------------------------------------------------
    -- Procedure: update_compliance_status
    -- Description: Update entity compliance status with audit trail
    -- --------------------------------------------------------------------
    PROCEDURE update_compliance_status(
        p_entity_id         NUMBER,
        p_new_status        VARCHAR2,
        p_notes             VARCHAR2 DEFAULT NULL
    ) IS
        v_old_status        VARCHAR2(50);
        v_entity_name       VARCHAR2(255);
    BEGIN
        -- Validate status
        IF p_new_status NOT IN ('COMPLIANT', 'NON_COMPLIANT', 'PENDING_REVIEW', 
                                 'UNDER_INVESTIGATION', 'PROBATION', 'SUSPENDED') THEN
            RAISE_APPLICATION_ERROR(-20003, 'Invalid compliance status');
        END IF;
        
        -- Get current status
        SELECT compliance_status, entity_name 
        INTO v_old_status, v_entity_name
        FROM financial_entities 
        WHERE entity_id = p_entity_id;
        
        -- Update status
        UPDATE financial_entities
        SET compliance_status = p_new_status,
            modified_date = SYSDATE,
            modified_by = USER
        WHERE entity_id = p_entity_id;
        
        -- Create audit trail
        INSERT INTO audit_trail (
            audit_id,
            entity_id,
            action_type,
            action_details,
            before_value,
            after_value
        ) VALUES (
            audit_seq.NEXTVAL,
            p_entity_id,
            'STATUS_UPDATED',
            'Compliance status changed for: ' || v_entity_name || '. ' || 
            NVL(p_notes, 'No notes provided'),
            v_old_status,
            p_new_status
        );
        
        -- Create alert if status is critical
        IF p_new_status IN ('NON_COMPLIANT', 'SUSPENDED') THEN
            INSERT INTO alert_notifications (
                alert_id,
                entity_id,
                alert_type,
                alert_priority,
                alert_message
            ) VALUES (
                alert_seq.NEXTVAL,
                p_entity_id,
                'STATUS_CHANGE',
                'HIGH',
                'Status changed to ' || p_new_status || ' for: ' || v_entity_name
            );
        END IF;
        
        COMMIT;
        
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20004, 'Entity not found: ' || p_entity_id);
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END update_compliance_status;
    
    -- --------------------------------------------------------------------
    -- Function: record_violation
    -- Description: Record new compliance violation
    -- --------------------------------------------------------------------
    FUNCTION record_violation(
        p_entity_id         NUMBER,
        p_violation_type    VARCHAR2,
        p_description       VARCHAR2,
        p_severity          VARCHAR2,
        p_fine_amount       NUMBER DEFAULT 0
    ) RETURN NUMBER IS
        v_violation_id      NUMBER;
        v_entity_name       VARCHAR2(255);
        v_current_risk      VARCHAR2(20);
    BEGIN
        -- Validate inputs
        IF p_severity NOT IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') THEN
            RAISE_APPLICATION_ERROR(-20005, 'Invalid severity level');
        END IF;
        
        -- Get entity info
        SELECT entity_name, risk_level 
        INTO v_entity_name, v_current_risk
        FROM financial_entities 
        WHERE entity_id = p_entity_id;
        
        -- Generate violation ID
        SELECT violation_seq.NEXTVAL INTO v_violation_id FROM dual;
        
        -- Insert violation
        INSERT INTO compliance_violations (
            violation_id,
            entity_id,
            violation_type,
            description,
            severity,
            violation_date,
            fine_amount,
            status
        ) VALUES (
            v_violation_id,
            p_entity_id,
            p_violation_type,
            p_description,
            p_severity,
            SYSDATE,
            p_fine_amount,
            'UNDER_REVIEW'
        );
        
        -- Escalate risk level if HIGH or CRITICAL violation
        IF p_severity IN ('HIGH', 'CRITICAL') AND v_current_risk != 'CRITICAL' THEN
            UPDATE financial_entities
            SET risk_level = CASE 
                WHEN p_severity = 'CRITICAL' THEN 'CRITICAL'
                WHEN p_severity = 'HIGH' AND risk_level IN ('LOW', 'MEDIUM') THEN 'HIGH'
                ELSE risk_level
            END
            WHERE entity_id = p_entity_id;
        END IF;
        
        -- Update compliance status to NON_COMPLIANT
        UPDATE financial_entities
        SET compliance_status = 'NON_COMPLIANT'
        WHERE entity_id = p_entity_id
        AND compliance_status = 'COMPLIANT';
        
        -- Create audit trail
        INSERT INTO audit_trail (
            audit_id,
            entity_id,
            action_type,
            action_details
        ) VALUES (
            audit_seq.NEXTVAL,
            p_entity_id,
            'VIOLATION_RECORDED',
            'Violation recorded: ' || p_violation_type || ' (Severity: ' || p_severity || ')'
        );
        
        -- Create high-priority alert for HIGH/CRITICAL violations
        IF p_severity IN ('HIGH', 'CRITICAL') THEN
            INSERT INTO alert_notifications (
                alert_id,
                entity_id,
                alert_type,
                alert_priority,
                alert_message
            ) VALUES (
                alert_seq.NEXTVAL,
                p_entity_id,
                'VIOLATION',
                CASE WHEN p_severity = 'CRITICAL' THEN 'URGENT' ELSE 'HIGH' END,
                p_severity || ' violation: ' || p_violation_type || ' - ' || v_entity_name
            );
        END IF;
        
        COMMIT;
        RETURN v_violation_id;
        
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20006, 'Entity not found: ' || p_entity_id);
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END record_violation;
    
    -- --------------------------------------------------------------------
    -- Function: calculate_compliance_score
    -- Description: Calculate compliance score based on violation history
    -- Returns: Score from 0-100 (100 = perfect compliance)
    -- --------------------------------------------------------------------
    FUNCTION calculate_compliance_score(
        p_entity_id         NUMBER,
        p_months_back       NUMBER DEFAULT 12
    ) RETURN NUMBER IS
        v_violation_count   NUMBER;
        v_critical_count    NUMBER;
        v_high_count        NUMBER;
        v_score             NUMBER := 100;
        v_cutoff_date       DATE;
    BEGIN
        v_cutoff_date := ADD_MONTHS(SYSDATE, -p_months_back);
        
        -- Count violations by severity
        SELECT 
            COUNT(*),
            SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END),
            SUM(CASE WHEN severity = 'HIGH' THEN 1 ELSE 0 END)
        INTO v_violation_count, v_critical_count, v_high_count
        FROM compliance_violations
        WHERE entity_id = p_entity_id
        AND violation_date >= v_cutoff_date;
        
        -- Calculate score deductions
        v_score := v_score - (v_critical_count * 20);  -- -20 per CRITICAL
        v_score := v_score - (v_high_count * 10);      -- -10 per HIGH
        v_score := v_score - ((v_violation_count - v_critical_count - v_high_count) * 5);  -- -5 per other
        
        -- Ensure score is between 0 and 100
        IF v_score < 0 THEN
            v_score := 0;
        END IF;
        
        RETURN v_score;
        
    END calculate_compliance_score;
    
    -- --------------------------------------------------------------------
    -- Function: get_risk_level
    -- Description: Get current risk level for entity
    -- --------------------------------------------------------------------
    FUNCTION get_risk_level(
        p_entity_id         NUMBER
    ) RETURN VARCHAR2 IS
        v_risk_level        VARCHAR2(20);
    BEGIN
        SELECT risk_level 
        INTO v_risk_level
        FROM financial_entities 
        WHERE entity_id = p_entity_id;
        
        RETURN v_risk_level;
        
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20007, 'Entity not found: ' || p_entity_id);
    END get_risk_level;
    
    -- --------------------------------------------------------------------
    -- Procedure: update_risk_level
    -- Description: Update entity risk level
    -- --------------------------------------------------------------------
    PROCEDURE update_risk_level(
        p_entity_id         NUMBER,
        p_new_risk_level    VARCHAR2,
        p_reason            VARCHAR2 DEFAULT NULL
    ) IS
        v_old_risk          VARCHAR2(20);
        v_entity_name       VARCHAR2(255);
    BEGIN
        -- Validate risk level
        IF p_new_risk_level NOT IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') THEN
            RAISE_APPLICATION_ERROR(-20008, 'Invalid risk level');
        END IF;
        
        -- Get current risk
        SELECT risk_level, entity_name 
        INTO v_old_risk, v_entity_name
        FROM financial_entities 
        WHERE entity_id = p_entity_id;
        
        -- Update risk level
        UPDATE financial_entities
        SET risk_level = p_new_risk_level
        WHERE entity_id = p_entity_id;
        
        -- Create audit trail
        INSERT INTO audit_trail (
            audit_id,
            entity_id,
            action_type,
            action_details,
            before_value,
            after_value
        ) VALUES (
            audit_seq.NEXTVAL,
            p_entity_id,
            'RISK_ESCALATED',
            'Risk level changed from ' || v_old_risk || ' to ' || p_new_risk_level || 
            '. Reason: ' || NVL(p_reason, 'Not specified'),
            v_old_risk,
            p_new_risk_level
        );
        
        -- Create alert if escalated to HIGH or CRITICAL
        IF p_new_risk_level IN ('HIGH', 'CRITICAL') AND v_old_risk NOT IN ('HIGH', 'CRITICAL') THEN
            INSERT INTO alert_notifications (
                alert_id,
                entity_id,
                alert_type,
                alert_priority,
                alert_message
            ) VALUES (
                alert_seq.NEXTVAL,
                p_entity_id,
                'RISK_ESCALATION',
                'HIGH',
                'Risk escalated to ' || p_new_risk_level || ' for: ' || v_entity_name
            );
        END IF;
        
        COMMIT;
        
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20009, 'Entity not found: ' || p_entity_id);
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END update_risk_level;
    
END pkg_compliance_mgmt;
/

PROMPT Package body PKG_COMPLIANCE_MGMT created successfully.

-- ============================================================================
-- PACKAGE 2: PKG_ALERT_ENGINE
-- Description: Automated alert generation and management
-- ============================================================================

PROMPT Creating package PKG_ALERT_ENGINE specification...

CREATE OR REPLACE PACKAGE pkg_alert_engine AS
    
    -- Generate alerts for entities requiring review
    PROCEDURE generate_review_alerts;
    
    -- Generate alerts for expiring licenses
    PROCEDURE generate_license_expiry_alerts(
        p_days_before       NUMBER DEFAULT 30
    );
    
    -- Generate alerts for overdue violations
    PROCEDURE generate_overdue_violation_alerts(
        p_days_overdue      NUMBER DEFAULT 60
    );
    
    -- Acknowledge alert
    PROCEDURE acknowledge_alert(
        p_alert_id          NUMBER,
        p_acknowledged_by   VARCHAR2
    );
    
    -- Resolve alert
    PROCEDURE resolve_alert(
        p_alert_id          NUMBER,
        p_notes             VARCHAR2 DEFAULT NULL
    );
    
    -- Get unacknowledged alerts count
    FUNCTION get_unacknowledged_count RETURN NUMBER;
    
    -- Get high priority alerts
    FUNCTION get_high_priority_alerts RETURN SYS_REFCURSOR;
    
END pkg_alert_engine;
/

PROMPT Package specification PKG_ALERT_ENGINE created successfully.

-- ============================================================================
-- PACKAGE BODY 2: PKG_ALERT_ENGINE
-- ============================================================================

PROMPT Creating package PKG_ALERT_ENGINE body...

CREATE OR REPLACE PACKAGE BODY pkg_alert_engine AS
    
    -- --------------------------------------------------------------------
    -- Procedure: generate_review_alerts
    -- Description: Generate alerts for entities with overdue reviews
    -- --------------------------------------------------------------------
    PROCEDURE generate_review_alerts IS
        v_alert_count       NUMBER := 0;
        
        CURSOR c_overdue_reviews IS
            SELECT entity_id, entity_name, next_review_date
            FROM financial_entities
            WHERE next_review_date < SYSDATE
            AND is_active = 'Y'
            AND entity_id NOT IN (
                SELECT entity_id 
                FROM alert_notifications 
                WHERE alert_type = 'REVIEW_DUE' 
                AND resolved = 'N'
            );
    BEGIN
        FOR rec IN c_overdue_reviews LOOP
            INSERT INTO alert_notifications (
                alert_id,
                entity_id,
                alert_type,
                alert_priority,
                alert_message
            ) VALUES (
                alert_seq.NEXTVAL,
                rec.entity_id,
                'REVIEW_DUE',
                'MEDIUM',
                'Compliance review overdue for: ' || rec.entity_name || 
                ' (Due: ' || TO_CHAR(rec.next_review_date, 'YYYY-MM-DD') || ')'
            );
            
            v_alert_count := v_alert_count + 1;
        END LOOP;
        
        COMMIT;
        
        DBMS_OUTPUT.PUT_LINE('Generated ' || v_alert_count || ' review alerts');
        
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END generate_review_alerts;
    
    -- --------------------------------------------------------------------
    -- Procedure: generate_license_expiry_alerts
    -- Description: Generate alerts for expiring licenses
    -- --------------------------------------------------------------------
    PROCEDURE generate_license_expiry_alerts(
        p_days_before       NUMBER DEFAULT 30
    ) IS
        v_alert_count       NUMBER := 0;
        
        CURSOR c_expiring_licenses IS
            SELECT entity_id, entity_name, license_number, license_expiry
            FROM financial_entities
            WHERE license_expiry BETWEEN SYSDATE AND SYSDATE + p_days_before
            AND is_active = 'Y'
            AND entity_id NOT IN (
                SELECT entity_id 
                FROM alert_notifications 
                WHERE alert_type = 'LICENSE_EXPIRING' 
                AND resolved = 'N'
                AND alert_timestamp > SYSDATE - p_days_before
            );
    BEGIN
        FOR rec IN c_expiring_licenses LOOP
            INSERT INTO alert_notifications (
                alert_id,
                entity_id,
                alert_type,
                alert_priority,
                alert_message
            ) VALUES (
                alert_seq.NEXTVAL,
                rec.entity_id,
                'LICENSE_EXPIRING',
                CASE 
                    WHEN rec.license_expiry <= SYSDATE + 7 THEN 'URGENT'
                    WHEN rec.license_expiry <= SYSDATE + 14 THEN 'HIGH'
                    ELSE 'MEDIUM'
                END,
                'License expiring on ' || TO_CHAR(rec.license_expiry, 'YYYY-MM-DD') || 
                ' for: ' || rec.entity_name || ' (License #' || rec.license_number || ')'
            );
            
            v_alert_count := v_alert_count + 1;
        END LOOP;
        
        COMMIT;
        
        DBMS_OUTPUT.PUT_LINE('Generated ' || v_alert_count || ' license expiry alerts');
        
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END generate_license_expiry_alerts;
    
    -- --------------------------------------------------------------------
    -- Procedure: generate_overdue_violation_alerts
    -- Description: Generate alerts for violations unresolved beyond threshold
    -- --------------------------------------------------------------------
    PROCEDURE generate_overdue_violation_alerts(
        p_days_overdue      NUMBER DEFAULT 60
    ) IS
        v_alert_count       NUMBER := 0;
        
        CURSOR c_overdue_violations IS
            SELECT 
                v.violation_id,
                v.entity_id,
                e.entity_name,
                v.violation_type,
                v.violation_date,
                v.severity
            FROM compliance_violations v
            JOIN financial_entities e ON v.entity_id = e.entity_id
            WHERE v.status IN ('UNDER_REVIEW', 'CONFIRMED')
            AND v.violation_date < SYSDATE - p_days_overdue
            AND v.violation_id NOT IN (
                SELECT TO_NUMBER(REGEXP_SUBSTR(alert_message, 'Violation #([0-9]+)', 1, 1, NULL, 1))
                FROM alert_notifications 
                WHERE alert_type = 'OVERDUE_VIOLATION' 
                AND resolved = 'N'
                AND REGEXP_LIKE(alert_message, 'Violation #[0-9]+')
            );
    BEGIN
        FOR rec IN c_overdue_violations LOOP
            INSERT INTO alert_notifications (
                alert_id,
                entity_id,
                alert_type,
                alert_priority,
                alert_message
            ) VALUES (
                alert_seq.NEXTVAL,
                rec.entity_id,
                'OVERDUE_VIOLATION',
                CASE 
                    WHEN rec.severity = 'CRITICAL' THEN 'URGENT'
                    WHEN rec.severity = 'HIGH' THEN 'HIGH'
                    ELSE 'MEDIUM'
                END,
                'Violation #' || rec.violation_id || ' unresolved for ' || 
                TRUNC(SYSDATE - rec.violation_date) || ' days: ' || 
                rec.violation_type || ' - ' || rec.entity_name
            );
            
            v_alert_count := v_alert_count + 1;
        END LOOP;
        
        COMMIT;
        
        DBMS_OUTPUT.PUT_LINE('Generated ' || v_alert_count || ' overdue violation alerts');
        
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END generate_overdue_violation_alerts;
    
    -- --------------------------------------------------------------------
    -- Procedure: acknowledge_alert
    -- Description: Mark alert as acknowledged
    -- --------------------------------------------------------------------
    PROCEDURE acknowledge_alert(
        p_alert_id          NUMBER,
        p_acknowledged_by   VARCHAR2
    ) IS
    BEGIN
        UPDATE alert_notifications
        SET acknowledged = 'Y',
            acknowledged_by = p_acknowledged_by,
            acknowledged_date = SYSTIMESTAMP
        WHERE alert_id = p_alert_id
        AND acknowledged = 'N';
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20010, 'Alert not found or already acknowledged: ' || p_alert_id);
        END IF;
        
        COMMIT;
        
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END acknowledge_alert;
    
    -- --------------------------------------------------------------------
    -- Procedure: resolve_alert
    -- Description: Mark alert as resolved
    -- --------------------------------------------------------------------
    PROCEDURE resolve_alert(
        p_alert_id          NUMBER,
        p_notes             VARCHAR2 DEFAULT NULL
    ) IS
    BEGIN
        UPDATE alert_notifications
        SET resolved = 'Y',
            resolved_date = SYSTIMESTAMP,
            notes = p_notes
        WHERE alert_id = p_alert_id
        AND resolved = 'N';
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20011, 'Alert not found or already resolved: ' || p_alert_id);
        END IF;
        
        COMMIT;
        
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END resolve_alert;
    
    -- --------------------------------------------------------------------
    -- Function: get_unacknowledged_count
    -- Description: Get count of unacknowledged alerts
    -- --------------------------------------------------------------------
    FUNCTION get_unacknowledged_count RETURN NUMBER IS
        v_count             NUMBER;
    BEGIN
        SELECT COUNT(*)
        INTO v_count
        FROM alert_notifications
        WHERE acknowledged = 'N'
        AND resolved = 'N';
        
        RETURN v_count;
        
    END get_unacknowledged_count;
    
    -- --------------------------------------------------------------------
    -- Function: get_high_priority_alerts
    -- Description: Get cursor of high priority unresolved alerts
    -- --------------------------------------------------------------------
    FUNCTION get_high_priority_alerts RETURN SYS_REFCURSOR IS
        v_cursor            SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                a.alert_id,
                a.entity_id,
                e.entity_name,
                a.alert_type,
                a.alert_priority,
                a.alert_message,
                a.alert_timestamp,
                a.acknowledged,
                a.acknowledged_by,
                a.acknowledged_date
            FROM alert_notifications a
            JOIN financial_entities e ON a.entity_id = e.entity_id
            WHERE a.alert_priority IN ('HIGH', 'URGENT')
            AND a.resolved = 'N'
            ORDER BY 
                CASE a.alert_priority 
                    WHEN 'URGENT' THEN 1 
                    WHEN 'HIGH' THEN 2 
                END,
                a.alert_timestamp DESC;
        
        RETURN v_cursor;
        
    END get_high_priority_alerts;
    
END pkg_alert_engine;
/

PROMPT Package body PKG_ALERT_ENGINE created successfully.

-- ============================================================================
-- PACKAGE 3: PKG_REPORTING
-- Description: Reporting and analytics functions
-- ============================================================================

PROMPT Creating package PKG_REPORTING specification...

CREATE OR REPLACE PACKAGE pkg_reporting AS
    
    -- Get dashboard statistics
    FUNCTION get_dashboard_stats RETURN SYS_REFCURSOR;
    
    -- Get entity compliance report
    FUNCTION get_entity_report(
        p_entity_id         NUMBER
    ) RETURN SYS_REFCURSOR;
    
    -- Get violations by type report
    FUNCTION get_violations_by_type(
        p_start_date        DATE DEFAULT ADD_MONTHS(SYSDATE, -12),
        p_end_date          DATE DEFAULT SYSDATE
    ) RETURN SYS_REFCURSOR;
    
    -- Get high risk entities
    FUNCTION get_high_risk_entities RETURN SYS_REFCURSOR;
    
    -- Get compliance trends
    FUNCTION get_compliance_trends(
        p_months_back       NUMBER DEFAULT 12
    ) RETURN SYS_REFCURSOR;
    
    -- Get entity violation summary
    PROCEDURE get_entity_violation_summary(
        p_entity_id         NUMBER,
        p_total_violations  OUT NUMBER,
        p_critical_count    OUT NUMBER,
        p_high_count        OUT NUMBER,
        p_total_fines       OUT NUMBER,
        p_unpaid_fines      OUT NUMBER
    );
    
END pkg_reporting;
/

PROMPT Package specification PKG_REPORTING created successfully.

-- ============================================================================
-- PACKAGE BODY 3: PKG_REPORTING
-- ============================================================================

PROMPT Creating package PKG_REPORTING body...

CREATE OR REPLACE PACKAGE BODY pkg_reporting AS
    
    -- --------------------------------------------------------------------
    -- Function: get_dashboard_stats
    -- Description: Return key statistics for dashboard
    -- --------------------------------------------------------------------
    FUNCTION get_dashboard_stats RETURN SYS_REFCURSOR IS
        v_cursor            SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT
                -- Entity Statistics
                (SELECT COUNT(*) FROM financial_entities WHERE is_active = 'Y') AS total_entities,
                (SELECT COUNT(*) FROM financial_entities WHERE compliance_status = 'COMPLIANT') AS compliant_entities,
                (SELECT COUNT(*) FROM financial_entities WHERE compliance_status = 'NON_COMPLIANT') AS non_compliant_entities,
                (SELECT COUNT(*) FROM financial_entities WHERE risk_level = 'CRITICAL') AS critical_risk_entities,
                (SELECT COUNT(*) FROM financial_entities WHERE risk_level = 'HIGH') AS high_risk_entities,
                
                -- Violation Statistics
                (SELECT COUNT(*) FROM compliance_violations WHERE status != 'RESOLVED') AS active_violations,
                (SELECT COUNT(*) FROM compliance_violations WHERE severity = 'CRITICAL' AND status != 'RESOLVED') AS critical_violations,
                (SELECT COUNT(*) FROM compliance_violations WHERE violation_date >= TRUNC(SYSDATE, 'MM')) AS violations_this_month,
                
                -- Financial Statistics
                (SELECT NVL(SUM(fine_amount), 0) FROM compliance_violations WHERE fine_paid = 'N') AS total_unpaid_fines,
                (SELECT NVL(SUM(fine_amount), 0) FROM compliance_violations WHERE fine_paid = 'Y') AS total_paid_fines,
                
                -- Alert Statistics
                (SELECT COUNT(*) FROM alert_notifications WHERE resolved = 'N') AS unresolved_alerts,
                (SELECT COUNT(*) FROM alert_notifications WHERE acknowledged = 'N' AND resolved = 'N') AS unacknowledged_alerts,
                (SELECT COUNT(*) FROM alert_notifications WHERE alert_priority = 'URGENT' AND resolved = 'N') AS urgent_alerts
            FROM dual;
        
        RETURN v_cursor;
        
    END get_dashboard_stats;
    
    -- --------------------------------------------------------------------
    -- Function: get_entity_report
    -- Description: Comprehensive report for a specific entity
    -- --------------------------------------------------------------------
    FUNCTION get_entity_report(
        p_entity_id         NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_cursor            SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                e.entity_id,
                e.entity_name,
                e.entity_type,
                e.nmls_id,
                e.compliance_status,
                e.risk_level,
                e.license_number,
                e.license_expiry,
                e.registration_date,
                e.last_review_date,
                e.next_review_date,
                e.total_assets,
                e.employee_count,
                -- Violation Summary
                (SELECT COUNT(*) FROM compliance_violations 
                 WHERE entity_id = e.entity_id) AS total_violations,
                (SELECT COUNT(*) FROM compliance_violations 
                 WHERE entity_id = e.entity_id AND status != 'RESOLVED') AS active_violations,
                (SELECT COUNT(*) FROM compliance_violations 
                 WHERE entity_id = e.entity_id AND severity = 'CRITICAL') AS critical_violations,
                (SELECT NVL(SUM(fine_amount), 0) FROM compliance_violations 
                 WHERE entity_id = e.entity_id) AS total_fines,
                (SELECT NVL(SUM(fine_amount), 0) FROM compliance_violations 
                 WHERE entity_id = e.entity_id AND fine_paid = 'N') AS unpaid_fines,
                -- Recent Violations (last 3)
                (SELECT LISTAGG(violation_type, ', ') WITHIN GROUP (ORDER BY violation_date DESC)
                 FROM (SELECT violation_type FROM compliance_violations 
                       WHERE entity_id = e.entity_id 
                       ORDER BY violation_date DESC FETCH FIRST 3 ROWS ONLY)
                ) AS recent_violations,
                -- Alert Summary
                (SELECT COUNT(*) FROM alert_notifications 
                 WHERE entity_id = e.entity_id AND resolved = 'N') AS active_alerts,
                -- Compliance Score
                pkg_compliance_mgmt.calculate_compliance_score(e.entity_id, 12) AS compliance_score
            FROM financial_entities e
            WHERE e.entity_id = p_entity_id;
        
        RETURN v_cursor;
        
    END get_entity_report;
    
    -- --------------------------------------------------------------------
    -- Function: get_violations_by_type
    -- Description: Violation statistics grouped by type
    -- --------------------------------------------------------------------
    FUNCTION get_violations_by_type(
        p_start_date        DATE DEFAULT ADD_MONTHS(SYSDATE, -12),
        p_end_date          DATE DEFAULT SYSDATE
    ) RETURN SYS_REFCURSOR IS
        v_cursor            SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                violation_type,
                COUNT(*) AS total_count,
                SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_count,
                SUM(CASE WHEN severity = 'HIGH' THEN 1 ELSE 0 END) AS high_count,
                SUM(CASE WHEN severity = 'MEDIUM' THEN 1 ELSE 0 END) AS medium_count,
                SUM(CASE WHEN severity = 'LOW' THEN 1 ELSE 0 END) AS low_count,
                NVL(SUM(fine_amount), 0) AS total_fines,
                NVL(AVG(fine_amount), 0) AS avg_fine,
                SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_count,
                SUM(CASE WHEN status != 'RESOLVED' THEN 1 ELSE 0 END) AS active_count
            FROM compliance_violations
            WHERE violation_date BETWEEN p_start_date AND p_end_date
            GROUP BY violation_type
            ORDER BY total_count DESC;
        
        RETURN v_cursor;
        
    END get_violations_by_type;
    
    -- --------------------------------------------------------------------
    -- Function: get_high_risk_entities
    -- Description: List of entities with HIGH or CRITICAL risk
    -- --------------------------------------------------------------------
    FUNCTION get_high_risk_entities RETURN SYS_REFCURSOR IS
        v_cursor            SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                e.entity_id,
                e.entity_name,
                e.entity_type,
                e.risk_level,
                e.compliance_status,
                (SELECT COUNT(*) FROM compliance_violations v
                 WHERE v.entity_id = e.entity_id 
                 AND v.status != 'RESOLVED') AS active_violations,
                (SELECT COUNT(*) FROM compliance_violations v
                 WHERE v.entity_id = e.entity_id 
                 AND v.severity = 'CRITICAL') AS critical_violations,
                (SELECT NVL(SUM(fine_amount), 0) FROM compliance_violations v
                 WHERE v.entity_id = e.entity_id 
                 AND v.fine_paid = 'N') AS unpaid_fines,
                pkg_compliance_mgmt.calculate_compliance_score(e.entity_id, 12) AS compliance_score,
                e.last_review_date,
                e.next_review_date
            FROM financial_entities e
            WHERE e.risk_level IN ('HIGH', 'CRITICAL')
            AND e.is_active = 'Y'
            ORDER BY 
                CASE e.risk_level WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 END,
                active_violations DESC;
        
        RETURN v_cursor;
        
    END get_high_risk_entities;
    
    -- --------------------------------------------------------------------
    -- Function: get_compliance_trends
    -- Description: Monthly compliance trends
    -- --------------------------------------------------------------------
    FUNCTION get_compliance_trends(
        p_months_back       NUMBER DEFAULT 12
    ) RETURN SYS_REFCURSOR IS
        v_cursor            SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            WITH monthly_violations AS (
                SELECT 
                    TO_CHAR(violation_date, 'YYYY-MM') AS month,
                    COUNT(*) AS violation_count,
                    SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_count,
                    NVL(SUM(fine_amount), 0) AS total_fines
                FROM compliance_violations
                WHERE violation_date >= ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -p_months_back)
                GROUP BY TO_CHAR(violation_date, 'YYYY-MM')
            ),
            monthly_registrations AS (
                SELECT 
                    TO_CHAR(registration_date, 'YYYY-MM') AS month,
                    COUNT(*) AS new_entities
                FROM financial_entities
                WHERE registration_date >= ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -p_months_back)
                GROUP BY TO_CHAR(registration_date, 'YYYY-MM')
            )
            SELECT 
                NVL(v.month, r.month) AS month,
                NVL(v.violation_count, 0) AS violation_count,
                NVL(v.critical_count, 0) AS critical_count,
                NVL(v.total_fines, 0) AS total_fines,
                NVL(r.new_entities, 0) AS new_entities
            FROM monthly_violations v
            FULL OUTER JOIN monthly_registrations r ON v.month = r.month
            ORDER BY NVL(v.month, r.month);
        
        RETURN v_cursor;
        
    END get_compliance_trends;
    
    -- --------------------------------------------------------------------
    -- Procedure: get_entity_violation_summary
    -- Description: Get violation summary statistics for entity
    -- --------------------------------------------------------------------
    PROCEDURE get_entity_violation_summary(
        p_entity_id         NUMBER,
        p_total_violations  OUT NUMBER,
        p_critical_count    OUT NUMBER,
        p_high_count        OUT NUMBER,
        p_total_fines       OUT NUMBER,
        p_unpaid_fines      OUT NUMBER
    ) IS
    BEGIN
        SELECT 
            COUNT(*),
            SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END),
            SUM(CASE WHEN severity = 'HIGH' THEN 1 ELSE 0 END),
            NVL(SUM(fine_amount), 0),
            NVL(SUM(CASE WHEN fine_paid = 'N' THEN fine_amount ELSE 0 END), 0)
        INTO 
            p_total_violations,
            p_critical_count,
            p_high_count,
            p_total_fines,
            p_unpaid_fines
        FROM compliance_violations
        WHERE entity_id = p_entity_id;
        
    EXCEPTION
        WHEN OTHERS THEN
            p_total_violations := 0;
            p_critical_count := 0;
            p_high_count := 0;
            p_total_fines := 0;
            p_unpaid_fines := 0;
    END get_entity_violation_summary;
    
END pkg_reporting;
/

PROMPT Package body PKG_REPORTING created successfully.

-- ============================================================================
-- COMPLETION MESSAGE
-- ============================================================================

PROMPT 
PROMPT ============================================================================
PROMPT PL/SQL Packages creation completed successfully!
PROMPT ============================================================================
PROMPT 
PROMPT Summary:
PROMPT - Package 1: PKG_COMPLIANCE_MGMT (6 functions/procedures)
PROMPT   * register_entity
PROMPT   * update_compliance_status
PROMPT   * record_violation
PROMPT   * calculate_compliance_score
PROMPT   * get_risk_level
PROMPT   * update_risk_level
PROMPT 
PROMPT - Package 2: PKG_ALERT_ENGINE (7 functions/procedures)
PROMPT   * generate_review_alerts
PROMPT   * generate_license_expiry_alerts
PROMPT   * generate_overdue_violation_alerts
PROMPT   * acknowledge_alert
PROMPT   * resolve_alert
PROMPT   * get_unacknowledged_count
PROMPT   * get_high_priority_alerts
PROMPT 
PROMPT - Package 3: PKG_REPORTING (6 functions/procedures)
PROMPT   * get_dashboard_stats
PROMPT   * get_entity_report
PROMPT   * get_violations_by_type
PROMPT   * get_high_risk_entities
PROMPT   * get_compliance_trends
PROMPT   * get_entity_violation_summary
PROMPT 
PROMPT Total: 3 packages, 19 functions/procedures
PROMPT 
PROMPT Next step:
PROMPT Run 03_insert_sample_data.sql to populate with test data
PROMPT 
PROMPT ============================================================================