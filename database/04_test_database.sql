SET SERVEROUTPUT ON SIZE UNLIMITED;
SET LINESIZE 200;
SET PAGESIZE 100;
SET FEEDBACK OFF;

DECLARE
    v_test_count NUMBER := 0;
    v_pass_count NUMBER := 0;
    v_fail_count NUMBER := 0;
    v_result VARCHAR2(10);
    v_count NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Running schema validation tests...');

    v_test_count := v_test_count + 1;
    SELECT COUNT(*) INTO v_count
    FROM user_tables
    WHERE table_name IN ('FINANCIAL_ENTITIES','COMPLIANCE_VIOLATIONS','AUDIT_TRAIL','ALERT_NOTIFICATIONS');
    IF v_count = 4 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 1.1: Tables exist: ' || v_result);

    v_test_count := v_test_count + 1;
    SELECT COUNT(*) INTO v_count FROM user_sequences
    WHERE sequence_name IN ('ENTITY_SEQ','VIOLATION_SEQ','AUDIT_SEQ','ALERT_SEQ');
    IF v_count = 4 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 1.2: Sequences exist: ' || v_result);

    v_test_count := v_test_count + 1;
    SELECT COUNT(*) INTO v_count FROM user_indexes
    WHERE table_name IN ('FINANCIAL_ENTITIES','COMPLIANCE_VIOLATIONS','AUDIT_TRAIL','ALERT_NOTIFICATIONS');
    IF v_count >= 18 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 1.3: Index count: ' || v_result);

    v_test_count := v_test_count + 1;
    SELECT COUNT(*) INTO v_count FROM user_triggers
    WHERE trigger_name IN ('TRG_AUDIT_IMMUTABLE','TRG_ENTITY_MODIFIED','TRG_VIOLATION_MODIFIED')
      AND status = 'ENABLED';
    IF v_count = 3 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 1.4: Triggers enabled: ' || v_result);

    v_test_count := v_test_count + 1;
    SELECT COUNT(*) INTO v_count FROM user_objects
    WHERE object_type = 'PACKAGE'
      AND object_name IN ('PKG_COMPLIANCE_MGMT','PKG_ALERT_ENGINE','PKG_REPORTING')
      AND status = 'VALID';
    IF v_count = 3 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 1.5: Packages valid: ' || v_result);

    DBMS_OUTPUT.PUT_LINE('Schema validation complete.');
END;
/
 
DECLARE
    v_test_count NUMBER := 0;
    v_pass_count NUMBER := 0;
    v_fail_count NUMBER := 0;
    v_result VARCHAR2(10);
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM financial_entities;
    IF v_count >= 8 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 2.1: Entities loaded: ' || v_result);

    SELECT COUNT(*) INTO v_count FROM compliance_violations;
    IF v_count >= 10 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 2.2: Violations loaded: ' || v_result);

    SELECT COUNT(*) INTO v_count FROM audit_trail;
    IF v_count >= 20 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 2.3: Audit entries: ' || v_result);

    SELECT COUNT(*) INTO v_count FROM alert_notifications;
    IF v_count >= 10 THEN
        v_result := 'PASS';
        v_pass_count := v_pass_count + 1;
    ELSE
        v_result := 'FAIL';
        v_fail_count := v_fail_count + 1;
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 2.4: Alerts generated: ' || v_result);

    SELECT COUNT(*) INTO v_count FROM compliance_violations v
    WHERE NOT EXISTS (SELECT 1 FROM financial_entities e WHERE e.entity_id = v.entity_id);
    IF v_count = 0 THEN
        v_result := 'PASS';
    ELSE
        v_result := 'FAIL';
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 2.5: Orphaned violations: ' || v_result);

    SELECT COUNT(*) INTO v_count FROM alert_notifications a
    WHERE NOT EXISTS (SELECT 1 FROM financial_entities e WHERE e.entity_id = a.entity_id);
    IF v_count = 0 THEN
        v_result := 'PASS';
    ELSE
        v_result := 'FAIL';
    END IF;
    DBMS_OUTPUT.PUT_LINE('Test 2.6: Orphaned alerts: ' || v_result);

    DBMS_OUTPUT.PUT_LINE('Data integrity validation complete.');
END;
/

DECLARE
    v_entity_id NUMBER;
    v_violation_id NUMBER;
    v_score NUMBER;
    v_risk VARCHAR2(20);
BEGIN
    v_entity_id := pkg_compliance_mgmt.register_entity(
        p_entity_name => 'Test Entity',
        p_entity_type => 'BANK',
        p_nmls_id => 'NMLS-TEST'
    );

    v_violation_id := pkg_compliance_mgmt.record_violation(
        p_entity_id => v_entity_id,
        p_violation_type => 'Sample Violation',
        p_description => 'Description',
        p_severity => 'LOW',
        p_fine_amount => 0
    );

    v_score := pkg_compliance_mgmt.calculate_compliance_score(v_entity_id, 12);

    v_risk := pkg_compliance_mgmt.get_risk_level(v_entity_id);

    pkg_compliance_mgmt.update_compliance_status(
        p_entity_id => v_entity_id,
        p_new_status => 'COMPLIANT'
    );

    DELETE FROM alert_notifications WHERE entity_id = v_entity_id;
    DELETE FROM audit_trail WHERE entity_id = v_entity_id;
    DELETE FROM compliance_violations WHERE entity_id = v_entity_id;
    DELETE FROM financial_entities WHERE entity_id = v_entity_id;
    COMMIT;
END;
/

SET FEEDBACK ON;
SET SERVEROUTPUT OFF;
