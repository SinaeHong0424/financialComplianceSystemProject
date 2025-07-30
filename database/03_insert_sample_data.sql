-- DFS Financial Compliance Management System
-- Sample Data Insertion Script
-- Inserts realistic sample data for testing

-- SECTION 1: FINANCIAL ENTITIES

DECLARE
    v_entity_id NUMBER;
BEGIN
    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'Empire State Bank',
        p_entity_type => 'BANK',
        p_nmls_id => 'NMLS-100001',
        p_contact_email => 'compliance@empirestatebank.com',
        p_contact_phone => '212-555-0101'
    );

    UPDATE financial_entities
    SET address_line1 = '123 Wall Street',
        city = 'New York',
        state = 'NY',
        zip_code = '10005',
        license_number = 'NYB-2020-001',
        license_expiry = ADD_MONTHS(SYSDATE, 18),
        total_assets = 5000000000,
        employee_count = 850,
        compliance_status = 'COMPLIANT',
        risk_level = 'LOW',
        last_review_date = ADD_MONTHS(SYSDATE, -3),
        next_review_date = ADD_MONTHS(SYSDATE, 9)
    WHERE entity_id = v_entity_id;

    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'Metro Community Bank',
        p_entity_type => 'BANK',
        p_nmls_id => 'NMLS-100002',
        p_contact_email => 'info@metrocommunity.com',
        p_contact_phone => '718-555-0202'
    );

    UPDATE financial_entities
    SET address_line1 = '456 Brooklyn Avenue',
        city = 'Brooklyn',
        state = 'NY',
        zip_code = '11201',
        license_number = 'NYB-2019-045',
        license_expiry = ADD_MONTHS(SYSDATE, 6),
        total_assets = 1200000000,
        employee_count = 320,
        last_review_date = ADD_MONTHS(SYSDATE, -6),
        next_review_date = ADD_MONTHS(SYSDATE, 1)
    WHERE entity_id = v_entity_id;

    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'Liberty Insurance Group',
        p_entity_type => 'INSURANCE',
        p_contact_email => 'compliance@libertyinsurance.com',
        p_contact_phone => '212-555-0303'
    );

    UPDATE financial_entities
    SET address_line1 = '789 Madison Avenue',
        city = 'New York',
        state = 'NY',
        zip_code = '10022',
        license_number = 'NYI-2018-123',
        license_expiry = ADD_MONTHS(SYSDATE, 24),
        total_assets = 8500000000,
        employee_count = 1200,
        compliance_status = 'COMPLIANT',
        risk_level = 'LOW',
        last_review_date = ADD_MONTHS(SYSDATE, -2),
        next_review_date = ADD_MONTHS(SYSDATE, 10)
    WHERE entity_id = v_entity_id;

    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'QuickCash Remittance Services',
        p_entity_type => 'MSB',
        p_nmls_id => 'NMLS-200001',
        p_contact_email => 'legal@quickcash.com',
        p_contact_phone => '718-555-0404'
    );

    UPDATE financial_entities
    SET address_line1 = '321 Queens Boulevard',
        city = 'Queens',
        state = 'NY',
        zip_code = '11372',
        license_number = 'NYM-2021-067',
        license_expiry = ADD_MONTHS(SYSDATE, 12),
        total_assets = 45000000,
        employee_count = 85,
        compliance_status = 'UNDER_INVESTIGATION',
        risk_level = 'HIGH',
        last_review_date = ADD_MONTHS(SYSDATE, -1),
        next_review_date = ADD_MONTHS(SYSDATE, 2)
    WHERE entity_id = v_entity_id;

    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'PayStream Digital Payments',
        p_entity_type => 'FINTECH',
        p_contact_email => 'compliance@paystream.io',
        p_contact_phone => '646-555-0505'
    );

    UPDATE financial_entities
    SET address_line1 = '100 Broadway, Suite 2000',
        city = 'New York',
        state = 'NY',
        zip_code = '10005',
        license_number = 'NYF-2022-015',
        license_expiry = ADD_MONTHS(SYSDATE, 8),
        total_assets = 120000000,
        employee_count = 150,
        compliance_status = 'PROBATION',
        risk_level = 'CRITICAL',
        last_review_date = SYSDATE - 15,
        next_review_date = SYSDATE + 30
    WHERE entity_id = v_entity_id;

    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'New York Teachers Credit Union',
        p_entity_type => 'CREDIT_UNION',
        p_nmls_id => 'NMLS-300001',
        p_contact_email => 'info@nyteacherscu.org',
        p_contact_phone => '212-555-0606'
    );

    UPDATE financial_entities
    SET address_line1 = '250 Park Avenue',
        city = 'New York',
        state = 'NY',
        zip_code = '10177',
        license_number = 'NYC-2017-089',
        license_expiry = ADD_MONTHS(SYSDATE, 30),
        total_assets = 2800000000,
        employee_count = 520,
        compliance_status = 'COMPLIANT',
        risk_level = 'LOW',
        last_review_date = ADD_MONTHS(SYSDATE, -4),
        next_review_date = ADD_MONTHS(SYSDATE, 8)
    WHERE entity_id = v_entity_id;

    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'Manhattan Securities LLC',
        p_entity_type => 'BROKER_DEALER',
        p_contact_email => 'compliance@manhattansec.com',
        p_contact_phone => '212-555-0707'
    );

    UPDATE financial_entities
    SET address_line1 = '555 Fifth Avenue, 30th Floor',
        city = 'New York',
        state = 'NY',
        zip_code = '10017',
        license_number = 'NYD-2020-234',
        license_expiry = ADD_MONTHS(SYSDATE, 15),
        total_assets = 950000000,
        employee_count = 280,
        compliance_status = 'NON_COMPLIANT',
        risk_level = 'MEDIUM',
        last_review_date = ADD_MONTHS(SYSDATE, -5),
        next_review_date = ADD_MONTHS(SYSDATE, 1)
    WHERE entity_id = v_entity_id;

    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'SafeGuard Health Insurance',
        p_entity_type => 'INSURANCE',
        p_contact_email => 'regulatory@safeguardhealth.com',
        p_contact_phone => '516-555-0808'
    );

    UPDATE financial_entities
    SET address_line1 = '1000 Long Island Expressway',
        city = 'Garden City',
        state = 'NY',
        zip_code = '11530',
        license_number = 'NYI-2023-045',
        license_expiry = ADD_MONTHS(SYSDATE, 20),
        total_assets = 3200000000,
        employee_count = 680,
        compliance_status = 'PENDING_REVIEW',
        risk_level = 'MEDIUM',
        registration_date = SYSDATE - 45
    WHERE entity_id = v_entity_id;

    COMMIT;
END;
/

-- SECTION 2: COMPLIANCE VIOLATIONS

DECLARE
    v_violation_id NUMBER;
BEGIN
    v_violation_id := pkg_compliance_mgmt.record_violation(
        p_entity_id => 1003,
        p_violation_type => 'AML Deficiency',
        p_description => 'Failed to implement adequate Anti-Money Laundering controls.',
        p_severity => 'CRITICAL',
        p_fine_amount => 500000
    );

    UPDATE compliance_violations
    SET violation_code = '23 NYCRR 504.3',
        reported_by = 'DFS Examiner Team',
        payment_due_date = SYSDATE + 60,
        follow_up_date = SYSDATE + 30
    WHERE violation_id = v_violation_id;

    v_violation_id := pkg_compliance_mgmt.record_violation(
        p_entity_id => 1004,
        p_violation_type => 'Cybersecurity Incident',
        p_description => 'Data breach affecting customer accounts.',
        p_severity => 'CRITICAL',
        p_fine_amount => 750000
    );

    UPDATE compliance_violations
    SET violation_code = '23 NYCRR 500.17',
        reported_by = 'DFS Cybersecurity Division',
        violation_date = SYSDATE - 45,
        payment_due_date = SYSDATE + 15,
        follow_up_date = SYSDATE + 7
    WHERE violation_id = v_violation_id;

    -- Additional violation inserts follow similar pattern...
    COMMIT;
END;
/

-- SECTION 3: GENERATE AUTOMATED ALERTS

BEGIN
    pkg_alert_engine.generate_review_alerts;
    pkg_alert_engine.generate_license_expiry_alerts(30);
    pkg_alert_engine.generate_overdue_violation_alerts(60);
    COMMIT;
END;
/

-- SECTION 4: SAMPLE ALERT ACKNOWLEDGMENTS

DECLARE
    CURSOR c_alerts IS
        SELECT alert_id
        FROM alert_notifications
        WHERE acknowledged = 'N'
        ORDER BY alert_timestamp
        FETCH FIRST 5 ROWS ONLY;
    v_count NUMBER := 0;
BEGIN
    FOR rec IN c_alerts LOOP
        pkg_alert_engine.acknowledge_alert(
            p_alert_id => rec.alert_id,
            p_acknowledged_by => 'compliance_officer_1'
        );
        v_count := v_count + 1;
    END LOOP;
    COMMIT;
END;
/

-- SECTION 5: DATA VERIFICATION SELECTS

SELECT entity_type, COUNT(*) AS count
FROM financial_entities
GROUP BY entity_type;

SELECT compliance_status, COUNT(*) AS count
FROM financial_entities
GROUP BY compliance_status;

SELECT risk_level, COUNT(*) AS count
FROM financial_entities
GROUP BY risk_level;

SELECT COUNT(*) AS total_violations,
       SUM(fine_amount) AS total_fines
FROM compliance_violations;

SELECT COUNT(*) AS total_alerts,
       SUM(CASE WHEN resolved = 'Y' THEN 1 ELSE 0 END) AS resolved_count
FROM alert_notifications;

SELECT COUNT(*) AS total_audit_entries
FROM audit_trail;
