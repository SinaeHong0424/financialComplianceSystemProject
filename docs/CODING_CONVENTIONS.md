Coding Conventions and Style Guide

Languages: Java 11, PL/SQL, Oracle APEX

1.General Principles

-Readability

Use clear names and straightforward logic
Avoid clever shortcuts that reduce clarity


-Consistency

Follow existing patterns
Use agreed naming and formatting


-Simplicity

Solve only what is needed
Avoid unnecessary abstractions


-Safety

Validate input early
Fail fast with clear errors


2.Java Style

-Naming

-Classes

//public class FinancialEntityService { }


-Methods

//public int registerEntity(String name) { }


-Variables

//String entityName
//int violationCount


-Constants

//public static final int MAX_POOL_SIZE = 50


-Formatting

-Indent with four spaces

//if (active) {
    process()
}


-Limit lines to about 120 characters
-Open brace on same line

-Best Practices

-Try with resources

//try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ...
}


-Use PreparedStatement to prevent SQL injection

//String sql = "SELECT * FROM table WHERE id = ?"


-Return Optional for nullable results

//public Optional<Entity> findEntityById(int id)


-Exception handling

//if (name == null) {
    throw new ValidationException("Name required")
}


-Logging

//LOGGER.info("Registering entity {}", name)


3.PL SQL Style

-Naming

-Packages

//CREATE PACKAGE pkg_compliance_mgmt AS


-Variables

//v_entity_id NUMBER


-Parameters

//p_entity_name VARCHAR2


-Formatting

//SELECT field
//INTO v_entity
//FROM dual


-Error handling

//EXCEPTION
//WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20001, 'Not found')


4.SQL Style

-Uppercase keywords

//SELECT e.id, e.name
//FROM financial_entities e
//JOIN violations v ON e.id = v.entity_id
//WHERE e.active = 'Y'


-Meaningful aliases

//FROM financial_entities fe
//JOIN compliance_violations cv


5.Oracle APEX Naming

-Page and item names with page prefix

//P2_ENTITY_NAME
//Region: Recent Violations


6.Documentation

-Javadoc

/**
 Register entity and return generated id

 @param name entity name
 @return generated id
 @throws ValidationException invalid input
 */
//public int registerEntity(String name)


-Inline comments explain why

        // escalate high severity risk


7.Testing

-Test naming

//@Test
//void registerEntity_nullName_throwsValidationException() { }


-Arrange Act Assert pattern

 // Arrange
 // Act
 // Assert


8.Code Review Essentials

-Functionality

handles edge cases


-Quality

follows naming and formatting


-Security

validates input
prevents SQL injection


-Testing

adequate coverage


-Documentation

public methods documented


9.Tooling

Configure code formatter in IDE
Use static analysis (Checkstyle, SonarLint) and fix issues before commit

End of Guide