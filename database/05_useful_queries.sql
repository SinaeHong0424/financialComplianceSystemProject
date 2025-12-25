-- ============================================================================
-- Useful SQL Queries Collection (Pure SQL / PL-SQL)
-- ============================================================================

-- SECTION 1: ENTITY QUERIES

SELECT 
    entity_id,
    entity_name,
    entity_type,
    compliance_status,
    risk_level,
    registration_date,
    TO_CHAR(total_assets, '$999,999,999,999') AS total_assets
FROM financial_entities
WHERE is_active = 'Y'
ORDER BY entity_name;

SELECT 
    entity_type,
    COUNT(*) AS entity_count,
    SUM(CASE WHEN compliance_status = 'COMPLIANT' THEN 1 ELSE 0 END) AS compliant,
    SUM(CASE WHEN compliance_status = 'NON_COMPLIANT' THEN 1 ELSE 0 END) AS non_compliant,
    SUM(CASE WHEN risk_level IN ('HIGH', 'CRITICAL') THEN 1 ELSE 0 END) AS high_risk
FROM financial_entities
WHERE is_active = 'Y'
GROUP BY entity_type
ORDER BY entity_count DESC;

SELECT 
    entity_name,
    entity_type,
    compliance_status,
    next_review_date,
    TRUNC(next_review_date - SYSDATE) AS days_until_review
FROM financial_entities
WHERE next_review_date BETWEEN SYSDATE AND SYSDATE + 30
AND is_active = 'Y'
ORDER BY next_review_date;

SELECT 
    entity_name,
    entity_type,
    license_number,
    license_expiry,
    TRUNC(license_expiry - SYSDATE) AS days_until_expiry,
    CASE 
        WHEN license_expiry <= SYSDATE + 7 THEN 'URGENT'
        WHEN license_expiry <= SYSDATE + 30 THEN 'HIGH'
        ELSE 'MEDIUM'
    END AS priority
FROM financial_entities
WHERE license_expiry BETWEEN SYSDATE AND SYSDATE + 60
AND is_active = 'Y'
ORDER BY license_expiry;

SELECT 
    e.entity_name,
    e.entity_type,
    e.risk_level,
    e.compliance_status,
    (SELECT COUNT(*) FROM compliance_violations v 
     WHERE v.entity_id = e.entity_id AND v.status != 'RESOLVED') AS active_violations,
    (SELECT COUNT(*) FROM compliance_violations v 
     WHERE v.entity_id = e.entity_id AND v.severity = 'CRITICAL') AS critical_violations,
    TO_CHAR((SELECT NVL(SUM(fine_amount), 0) FROM compliance_violations v 
             WHERE v.entity_id = e.entity_id AND v.fine_paid = 'N'), '$999,999,999') AS unpaid_fines
FROM financial_entities e
WHERE e.risk_level IN ('HIGH', 'CRITICAL')
AND e.is_active = 'Y'
ORDER BY CASE e.risk_level WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 END,
         active_violations DESC;

-- SECTION 2: VIOLATION QUERIES

SELECT 
    v.violation_id,
    e.entity_name,
    v.violation_type,
    v.severity,
    TO_CHAR(v.violation_date, 'YYYY-MM-DD') AS violation_date,
    TRUNC(SYSDATE - v.violation_date) AS days_open,
    TO_CHAR(v.fine_amount, '$999,999,999') AS fine_amount,
    v.fine_paid,
    v.status
FROM compliance_violations v
JOIN financial_entities e ON v.entity_id = e.entity_id
WHERE v.status != 'RESOLVED'
ORDER BY v.severity DESC, v.violation_date;

SELECT 
    severity,
    COUNT(*) AS total_count,
    SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved,
    SUM(CASE WHEN status != 'RESOLVED' THEN 1 ELSE 0 END) AS active,
    TO_CHAR(NVL(SUM(fine_amount), 0), '$999,999,999') AS total_fines,
    TO_CHAR(NVL(SUM(CASE WHEN fine_paid = 'N' THEN fine_amount ELSE 0 END), 0), '$999,999,999') AS unpaid_fines
FROM compliance_violations
GROUP BY severity
ORDER BY CASE severity 
    WHEN 'CRITICAL' THEN 1 
    WHEN 'HIGH' THEN 2 
    WHEN 'MEDIUM' THEN 3 
    WHEN 'LOW' THEN 4 
END;

SELECT 
    violation_type,
    COUNT(*) AS occurrence_count,
    SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_count,
    SUM(CASE WHEN severity = 'HIGH' THEN 1 ELSE 0 END) AS high_count,
    TO_CHAR(NVL(AVG(fine_amount), 0), '$999,999,999') AS avg_fine,
    TO_CHAR(NVL(SUM(fine_amount), 0), '$999,999,999') AS total_fines
FROM compliance_violations
GROUP BY violation_type
ORDER BY occurrence_count DESC
FETCH FIRST 10 ROWS ONLY;

SELECT 
    v.violation_id,
    e.entity_name,
    v.violation_type,
    v.severity,
    TO_CHAR(v.violation_date, 'YYYY-MM-DD') AS violation_date,
    TRUNC(SYSDATE - v.violation_date) AS days_overdue,
    v.status,
    TO_CHAR(v.fine_amount, '$999,999,999') AS fine_amount
FROM compliance_violations v
JOIN financial_entities e ON v.entity_id = e.entity_id
WHERE v.status IN ('UNDER_REVIEW', 'CONFIRMED')
AND v.violation_date < SYSDATE - 60
ORDER BY (SYSDATE - v.violation_date) DESC;

SELECT 
    e.entity_name,
    e.entity_type,
    COUNT(v.violation_id) AS unpaid_violation_count,
    TO_CHAR(NVL(SUM(v.fine_amount), 0), '$999,999,999') AS total_unpaid_fines,
    TO_CHAR(MIN(v.payment_due_date), 'YYYY-MM-DD') AS earliest_due_date,
    CASE 
        WHEN MIN(v.payment_due_date) < SYSDATE THEN 'OVERDUE'
        WHEN MIN(v.payment_due_date) <= SYSDATE + 30 THEN 'DUE SOON'
        ELSE 'OK'
    END AS payment_status
FROM financial_entities e
JOIN compliance_violations v ON e.entity_id = v.entity_id
WHERE v.fine_paid = 'N'
GROUP BY e.entity_name, e.entity_type
HAVING SUM(v.fine_amount) > 0
ORDER BY SUM(v.fine_amount) DESC;

SELECT 
    TO_CHAR(violation_date, 'YYYY-MM') AS month,
    COUNT(*) AS total_violations,
    SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END) AS critical,
    SUM(CASE WHEN severity = 'HIGH' THEN 1 ELSE 0 END) AS high,
    TO_CHAR(NVL(SUM(fine_amount), 0), '$999,999,999') AS total_fines
FROM compliance_violations
WHERE violation_date >= ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -12)
GROUP BY TO_CHAR(violation_date, 'YYYY-MM')
ORDER BY month DESC;

-- SECTION 3: ALERT QUERIES

SELECT 
    alert_priority,
    COUNT(*) AS alert_count,
    SUM(CASE WHEN acknowledged = 'Y' THEN 1 ELSE 0 END) AS acknowledged,
    SUM(CASE WHEN acknowledged = 'N' THEN 1 ELSE 0 END) AS unacknowledged
FROM alert_notifications
WHERE resolved = 'N'
GROUP BY alert_priority
ORDER BY CASE alert_priority 
    WHEN 'URGENT' THEN 1 
    WHEN 'HIGH' THEN 2 
    WHEN 'MEDIUM' THEN 3 
    WHEN 'LOW' THEN 4 
END;

SELECT 
    a.alert_id,
    e.entity_name,
    a.alert_type,
    a.alert_priority,
    a.alert_message,
    TO_CHAR(a.alert_timestamp, 'YYYY-MM-DD HH24:MI:SS') AS created_at
FROM alert_notifications a
JOIN financial_entities e ON a.entity_id = e.entity_id
WHERE a.acknowledged = 'N'
AND a.resolved = 'N'
AND a.alert_timestamp >= SYSDATE - 7
ORDER BY a.alert_priority, a.alert_timestamp DESC;

SELECT 
    alert_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN resolved = 'N' THEN 1 ELSE 0 END) AS unresolved,
    SUM(CASE WHEN acknowledged = 'N' AND resolved = 'N' THEN 1 ELSE 0 END) AS needs_attention
FROM alert_notifications
GROUP BY alert_type
ORDER BY unresolved DESC, total_count DESC;

SELECT 
    alert_priority,
    COUNT(*) AS acknowledged_count,
    ROUND(AVG(EXTRACT(HOUR FROM (acknowledged_date - alert_timestamp)) * 60 + 
              EXTRACT(MINUTE FROM (acknowledged_date - alert_timestamp))), 2) AS avg_response_minutes,
    MIN(EXTRACT(HOUR FROM (acknowledged_date - alert_timestamp)) * 60 + 
        EXTRACT(MINUTE FROM (acknowledged_date - alert_timestamp))) AS min_response_minutes,
    MAX(EXTRACT(HOUR FROM (acknowledged_date - alert_timestamp)) * 60 + 
        EXTRACT(MINUTE FROM (acknowledged_date - alert_timestamp))) AS max_response_minutes
FROM alert_notifications
WHERE acknowledged = 'Y'
AND acknowledged_date IS NOT NULL
GROUP BY alert_priority
ORDER BY CASE alert_priority 
    WHEN 'URGENT' THEN 1 
    WHEN 'HIGH' THEN 2 
    WHEN 'MEDIUM' THEN 3 
    WHEN 'LOW' THEN 4 
END;

-- SECTION 4: AUDIT TRAIL QUERIES

SELECT 
    TO_CHAR(action_timestamp, 'YYYY-MM-DD HH24:MI:SS') AS timestamp,
    action_type,
    performed_by,
    SUBSTR(action_details, 1, 80) AS action_summary
FROM audit_trail
WHERE action_timestamp >= SYSDATE - 1
ORDER BY action_timestamp DESC;

SELECT 
    action_type,
    COUNT(*) AS occurrence_count,
    COUNT(DISTINCT performed_by) AS unique_users,
    TO_CHAR(MIN(action_timestamp), 'YYYY-MM-DD') AS first_occurrence,
    TO_CHAR(MAX(action_timestamp), 'YYYY-MM-DD') AS last_occurrence
FROM audit_trail
GROUP BY action_type
ORDER BY occurrence_count DESC;

SELECT 
    performed_by,
    COUNT(*) AS total_actions,
    COUNT(DISTINCT action_type) AS distinct_action_types,
    TO_CHAR(MIN(action_timestamp), 'YYYY-MM-DD HH24:MI:SS') AS first_action,
    TO_CHAR(MAX(action_timestamp), 'YYYY-MM-DD HH24:MI:SS') AS last_action
FROM audit_trail
GROUP BY performed_by
ORDER BY total_actions DESC;

SELECT 
    TO_CHAR(a.action_timestamp, 'YYYY-MM-DD HH24:MI:SS') AS timestamp,
    a.action_type,
    a.action_details,
    a.performed_by,
    a.before_value,
    a.after_value
FROM audit_trail a
WHERE a.entity_id = 1004
ORDER BY a.action_timestamp DESC;

-- SECTION 5: ANALYTICS & REPORTING

SELECT 
    entity_name,
    entity_type,
    pkg_compliance_mgmt.calculate_compliance_score(entity_id, 12) AS compliance_score,
    CASE 
        WHEN pkg_compliance_mgmt.calculate_compliance_score(entity_id, 12) >= 90 THEN 'Excellent'
        WHEN pkg_compliance_mgmt.calculate_compliance_score(entity_id, 12) >= 75 THEN 'Good'
        WHEN pkg_compliance_mgmt.calculate_compliance_score(entity_id, 12) >= 50 THEN 'Fair'
        ELSE 'Poor'
    END AS score_rating,
    compliance_status,
    risk_level
FROM financial_entities
WHERE is_active = 'Y'
ORDER BY compliance_score DESC;

SELECT 
    e.entity_name,
    e.entity_type,
    COUNT(v.violation_id) AS total_violations,
    SUM(CASE WHEN v.severity = 'CRITICAL' THEN 1 ELSE 0 END) AS critical,
    SUM(CASE WHEN v.severity = 'HIGH' THEN 1 ELSE 0 END) AS high,
    SUM(CASE WHEN v.status != 'RESOLVED' THEN 1 ELSE 0 END) AS active,
    TO_CHAR(NVL(SUM(v.fine_amount), 0), '$999,999,999') AS total_fines
FROM financial_entities e
LEFT JOIN compliance_violations v ON e.entity_id = v.entity_id
WHERE e.is_active = 'Y'
GROUP BY e.entity_name, e.entity_type
ORDER BY COUNT(v.violation_id) DESC;
