package com.dfs.compliance.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfs.compliance.model.FinancialEntity;
import com.dfs.compliance.model.FinancialEntity.ComplianceStatus;
import com.dfs.compliance.model.FinancialEntity.EntityType;
import com.dfs.compliance.model.FinancialEntity.RiskLevel;
import com.dfs.compliance.util.DatabaseConnection;

/**
 * Unit tests for FinancialEntityDAO.
 * 
 * <p>Tests all CRUD operations and business logic methods.
 * Uses actual database connection for integration testing.
 * 
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinancialEntityDAOTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialEntityDAOTest.class);
    
    private FinancialEntityDAO entityDAO;
    private Long testEntityId;
    
    @BeforeAll
    void setUp() throws Exception {
        logger.info("========================================");
        logger.info("Setting up FinancialEntityDAO tests");
        logger.info("========================================");
        entityDAO = new FinancialEntityDAOImpl();
        
        // Test database connection
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assertTrue(conn.isValid(5), "Database connection should be valid");
            logger.info("✓ Database connection test passed");
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Test 1: Create new financial entity")
    void testCreateEntity() throws SQLException {
        logger.info("\n>>> Test 1: Creating new financial entity");
        
        // Arrange
        FinancialEntity entity = new FinancialEntity();
        entity.setEntityName("JUnit Test Bank");
        entity.setEntityType(EntityType.BANK);
        entity.setNmlsId("NMLS-TEST-999");
        entity.setContactEmail("test@junit.com");
        entity.setContactPhone("555-TEST-001");
        entity.setAddressLine1("123 Test Street");
        entity.setCity("Test City");
        entity.setState("NY");
        entity.setZipCode("10001");
        entity.setLicenseNumber("TEST-LIC-001");
        entity.setLicenseExpiry(LocalDate.now().plusYears(1));
        entity.setComplianceStatus(ComplianceStatus.PENDING_REVIEW);
        entity.setRiskLevel(RiskLevel.MEDIUM);
        entity.setTotalAssets(new BigDecimal("1000000000"));
        entity.setEmployeeCount(100);
        
        // Act
        FinancialEntity created = entityDAO.create(entity);
        testEntityId = created.getEntityId();
        
        // Assert
        assertNotNull(created.getEntityId(), "Entity ID should be generated");
        assertEquals("JUnit Test Bank", created.getEntityName());
        assertEquals(EntityType.BANK, created.getEntityType());
        assertTrue(created.isActive(), "New entity should be active");
        
        logger.info("✓ Created entity with ID: {}", testEntityId);
    }
    
    @Test
    @Order(2)
    @DisplayName("Test 2: Find entity by ID")
    void testFindById() throws SQLException {
        logger.info("\n>>> Test 2: Finding entity by ID");
        
        // Act
        Optional<FinancialEntity> result = entityDAO.findById(testEntityId);
        
        // Assert
        assertTrue(result.isPresent(), "Entity should be found");
        assertEquals("JUnit Test Bank", result.get().getEntityName());
        assertEquals(EntityType.BANK, result.get().getEntityType());
        
        logger.info("✓ Found entity: {}", result.get().getEntityName());
    }
    
    @Test
    @Order(3)
    @DisplayName("Test 3: Find entity by ID - not found")
    void testFindByIdNotFound() throws SQLException {
        logger.info("\n>>> Test 3: Finding non-existent entity");
        
        // Act
        Optional<FinancialEntity> result = entityDAO.findById(999999L);
        
        // Assert
        assertFalse(result.isPresent(), "Non-existent entity should not be found");
        
        logger.info("✓ Correctly returned empty Optional");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test 4: Find all entities")
    void testFindAll() throws SQLException {
        logger.info("\n>>> Test 4: Finding all entities");
        
        // Act
        List<FinancialEntity> entities = entityDAO.findAll();
        
        // Assert
        assertNotNull(entities, "Entity list should not be null");
        assertTrue(entities.size() > 0, "Should find at least one entity");
        assertTrue(entities.stream().anyMatch(e -> e.getEntityId().equals(testEntityId)),
                "Should include test entity");
        
        logger.info("✓ Found {} active entities", entities.size());
    }
    
    @Test
    @Order(5)
    @DisplayName("Test 5: Find entities by type")
    void testFindByType() throws SQLException {
        logger.info("\n>>> Test 5: Finding entities by type");
        
        // Act
        List<FinancialEntity> banks = entityDAO.findByType(EntityType.BANK);
        
        // Assert
        assertNotNull(banks, "Bank list should not be null");
        assertTrue(banks.size() > 0, "Should find at least one bank");
        assertTrue(banks.stream().allMatch(e -> e.getEntityType() == EntityType.BANK),
                "All entities should be banks");
        
        logger.info("✓ Found {} banks", banks.size());
    }
    
    @Test
    @Order(6)
    @DisplayName("Test 6: Find entities by compliance status")
    void testFindByComplianceStatus() throws SQLException {
        logger.info("\n>>> Test 6: Finding entities by compliance status");
        
        // Act
        List<FinancialEntity> pending = entityDAO.findByComplianceStatus(
                ComplianceStatus.PENDING_REVIEW);
        
        // Assert
        assertNotNull(pending, "Pending list should not be null");
        assertTrue(pending.stream().allMatch(
                e -> e.getComplianceStatus() == ComplianceStatus.PENDING_REVIEW),
                "All entities should have PENDING_REVIEW status");
        
        logger.info("✓ Found {} entities with PENDING_REVIEW status", pending.size());
    }
    
    @Test
    @Order(7)
    @DisplayName("Test 7: Find entities by risk level")
    void testFindByRiskLevel() throws SQLException {
        logger.info("\n>>> Test 7: Finding entities by risk level");
        
        // Act
        List<FinancialEntity> mediumRisk = entityDAO.findByRiskLevel(RiskLevel.MEDIUM);
        
        // Assert
        assertNotNull(mediumRisk, "Medium risk list should not be null");
        assertTrue(mediumRisk.stream().allMatch(e -> e.getRiskLevel() == RiskLevel.MEDIUM),
                "All entities should have MEDIUM risk level");
        
        logger.info("✓ Found {} entities with MEDIUM risk", mediumRisk.size());
    }
    
    @Test
    @Order(8)
    @DisplayName("Test 8: Find entities with expiring licenses")
    void testFindEntitiesWithExpiringLicenses() throws SQLException {
        logger.info("\n>>> Test 8: Finding entities with expiring licenses");
        
        // Act
        List<FinancialEntity> expiring = entityDAO.findEntitiesWithExpiringLicenses(365);
        
        // Assert
        assertNotNull(expiring, "Expiring list should not be null");
        assertTrue(expiring.stream().anyMatch(e -> e.getEntityId().equals(testEntityId)),
                "Should include test entity with license expiring in 1 year");
        
        logger.info("✓ Found {} entities with licenses expiring in 365 days", expiring.size());
    }
    
    @Test
    @Order(9)
    @DisplayName("Test 9: Search entities by name")
    void testSearchByName() throws SQLException {
        logger.info("\n>>> Test 9: Searching entities by name");
        
        // Act
        List<FinancialEntity> results = entityDAO.searchByName("JUnit");
        
        // Assert
        assertNotNull(results, "Search results should not be null");
        assertTrue(results.size() > 0, "Should find at least one entity");
        assertTrue(results.stream().anyMatch(e -> e.getEntityName().contains("JUnit")),
                "Results should contain 'JUnit' in name");
        
        logger.info("✓ Found {} entities matching 'JUnit'", results.size());
    }
    
    @Test
    @Order(10)
    @DisplayName("Test 10: Update entity")
    void testUpdateEntity() throws SQLException {
        logger.info("\n>>> Test 10: Updating entity");
        
        // Arrange
        Optional<FinancialEntity> entityOpt = entityDAO.findById(testEntityId);
        assertTrue(entityOpt.isPresent(), "Entity should exist");
        
        FinancialEntity entity = entityOpt.get();
        entity.setEmployeeCount(150);
        entity.setNotes("Updated by JUnit test");
        
        // Act
        boolean updated = entityDAO.update(entity);
        
        // Assert
        assertTrue(updated, "Update should succeed");
        
        // Verify
        Optional<FinancialEntity> verifyOpt = entityDAO.findById(testEntityId);
        assertTrue(verifyOpt.isPresent(), "Entity should still exist");
        assertEquals(150, verifyOpt.get().getEmployeeCount());
        assertEquals("Updated by JUnit test", verifyOpt.get().getNotes());
        
        logger.info("✓ Successfully updated entity");
    }
    
    @Test
    @Order(11)
    @DisplayName("Test 11: Update compliance status")
    void testUpdateComplianceStatus() throws SQLException {
        logger.info("\n>>> Test 11: Updating compliance status");
        
        // Act
        boolean updated = entityDAO.updateComplianceStatus(
                testEntityId, 
                ComplianceStatus.COMPLIANT, 
                "Test passed compliance review");
        
        // Assert
        assertTrue(updated, "Status update should succeed");
        
        // Verify
        Optional<FinancialEntity> verifyOpt = entityDAO.findById(testEntityId);
        assertTrue(verifyOpt.isPresent(), "Entity should exist");
        assertEquals(ComplianceStatus.COMPLIANT, verifyOpt.get().getComplianceStatus());
        
        logger.info("✓ Successfully updated compliance status to COMPLIANT");
    }
    
    @Test
    @Order(12)
    @DisplayName("Test 12: Update risk level")
    void testUpdateRiskLevel() throws SQLException {
        logger.info("\n>>> Test 12: Updating risk level");
        
        // Act
        boolean updated = entityDAO.updateRiskLevel(
                testEntityId, 
                RiskLevel.LOW, 
                "Test entity in good standing");
        
        // Assert
        assertTrue(updated, "Risk level update should succeed");
        
        // Verify
        Optional<FinancialEntity> verifyOpt = entityDAO.findById(testEntityId);
        assertTrue(verifyOpt.isPresent(), "Entity should exist");
        assertEquals(RiskLevel.LOW, verifyOpt.get().getRiskLevel());
        
        logger.info("✓ Successfully updated risk level to LOW");
    }
    
    @Test
    @Order(13)
    @DisplayName("Test 13: Count all entities")
    void testCount() throws SQLException {
        logger.info("\n>>> Test 13: Counting all entities");
        
        // Act
        long count = entityDAO.count();
        
        // Assert
        assertTrue(count > 0, "Should have at least one entity");
        
        logger.info("✓ Total active entities: {}", count);
    }
    
    @Test
    @Order(14)
    @DisplayName("Test 14: Count entities by type")
    void testCountByType() throws SQLException {
        logger.info("\n>>> Test 14: Counting entities by type");
        
        // Act
        long bankCount = entityDAO.countByType(EntityType.BANK);
        
        // Assert
        assertTrue(bankCount > 0, "Should have at least one bank");
        
        logger.info("✓ Total banks: {}", bankCount);
    }
    
    @Test
    @Order(15)
    @DisplayName("Test 15: Soft delete entity")
    void testDelete() throws SQLException {
        logger.info("\n>>> Test 15: Soft deleting entity");
        
        // Act
        boolean deleted = entityDAO.delete(testEntityId);
        
        // Assert
        assertTrue(deleted, "Delete should succeed");
        
        // Verify entity still exists but is inactive
        Optional<FinancialEntity> verifyOpt = entityDAO.findById(testEntityId);
        assertTrue(verifyOpt.isPresent(), "Entity should still exist in database");
        assertFalse(verifyOpt.get().isActive(), "Entity should be inactive");
        
        // Verify entity is not in findAll() results
        List<FinancialEntity> allActive = entityDAO.findAll();
        assertFalse(allActive.stream().anyMatch(e -> e.getEntityId().equals(testEntityId)),
                "Deleted entity should not appear in active entities list");
        
        logger.info("✓ Successfully soft deleted entity (is_active = 'N')");
    }
    
    @Test
    @Order(16)
    @DisplayName("Test 16: Update validation - null entity ID")
    void testUpdateValidation() {
        logger.info("\n>>> Test 16: Testing update validation");
        
        // Arrange
        FinancialEntity entity = new FinancialEntity();
        entity.setEntityName("Test");
        // entity ID is null
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            entityDAO.update(entity);
        }, "Update with null ID should throw IllegalArgumentException");
        
        logger.info("✓ Correctly threw IllegalArgumentException for null ID");
    }
    
    @AfterAll
    void tearDown() throws Exception {
        logger.info("\n========================================");
        logger.info("FinancialEntityDAO tests completed");
        logger.info("========================================");
        
        // Note: Test entity remains soft-deleted in database
        // This is intentional for audit trail purposes
    }
}