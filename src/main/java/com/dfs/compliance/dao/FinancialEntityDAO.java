package com.dfs.compliance.dao;

import com.dfs.compliance.model.FinancialEntity;
import com.dfs.compliance.model.FinancialEntity.ComplianceStatus;
import com.dfs.compliance.model.FinancialEntity.EntityType;
import com.dfs.compliance.model.FinancialEntity.RiskLevel;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for FinancialEntity operations.
 * 
 * <p>Defines all database operations for financial entities.
 * Implementations should handle SQL exceptions and provide
 * appropriate error handling.
 */
public interface FinancialEntityDAO {
    
    /**
     * Creates a new financial entity in the database.
     * 
     * @param entity the entity to create
     * @return the created entity with generated ID
     * @throws SQLException if database error occurs
     */
    FinancialEntity create(FinancialEntity entity) throws SQLException;
    
    /**
     * Finds an entity by its ID.
     * 
     * @param entityId the entity ID
     * @return Optional containing the entity if found
     * @throws SQLException if database error occurs
     */
    Optional<FinancialEntity> findById(Long entityId) throws SQLException;
    
    /**
     * Finds all active entities.
     * 
     * @return list of active entities
     * @throws SQLException if database error occurs
     */
    List<FinancialEntity> findAll() throws SQLException;
    
    /**
     * Finds entities by type.
     * 
     * @param entityType the entity type
     * @return list of entities of specified type
     * @throws SQLException if database error occurs
     */
    List<FinancialEntity> findByType(EntityType entityType) throws SQLException;
    
    /**
     * Finds entities by compliance status.
     * 
     * @param status the compliance status
     * @return list of entities with specified status
     * @throws SQLException if database error occurs
     */
    List<FinancialEntity> findByComplianceStatus(ComplianceStatus status) throws SQLException;
    
    /**
     * Finds entities by risk level.
     * 
     * @param riskLevel the risk level
     * @return list of entities with specified risk level
     * @throws SQLException if database error occurs
     */
    List<FinancialEntity> findByRiskLevel(RiskLevel riskLevel) throws SQLException;
    
    /**
     * Finds entities with expiring licenses.
     * 
     * @param daysAhead number of days to look ahead
     * @return list of entities with expiring licenses
     * @throws SQLException if database error occurs
     */
    List<FinancialEntity> findEntitiesWithExpiringLicenses(int daysAhead) throws SQLException;
    
    /**
     * Finds entities with overdue reviews.
     * 
     * @return list of entities with overdue reviews
     * @throws SQLException if database error occurs
     */
    List<FinancialEntity> findEntitiesWithOverdueReviews() throws SQLException;
    
    /**
     * Searches entities by name (case-insensitive).
     * 
     * @param name the name to search for
     * @return list of matching entities
     * @throws SQLException if database error occurs
     */
    List<FinancialEntity> searchByName(String name) throws SQLException;
    
    /**
     * Updates an existing entity.
     * 
     * @param entity the entity to update
     * @return true if update successful
     * @throws SQLException if database error occurs
     */
    boolean update(FinancialEntity entity) throws SQLException;
    
    /**
     * Updates entity compliance status.
     * 
     * @param entityId the entity ID
     * @param status new compliance status
     * @param notes optional notes
     * @return true if update successful
     * @throws SQLException if database error occurs
     */
    boolean updateComplianceStatus(Long entityId, ComplianceStatus status, String notes) 
            throws SQLException;
    
    /**
     * Updates entity risk level.
     * 
     * @param entityId the entity ID
     * @param riskLevel new risk level
     * @param reason optional reason
     * @return true if update successful
     * @throws SQLException if database error occurs
     */
    boolean updateRiskLevel(Long entityId, RiskLevel riskLevel, String reason) 
            throws SQLException;
    
    /**
     * Soft deletes an entity (sets is_active to false).
     * 
     * @param entityId the entity ID
     * @return true if delete successful
     * @throws SQLException if database error occurs
     */
    boolean delete(Long entityId) throws SQLException;
    
    /**
     * Counts total active entities.
     * 
     * @return count of active entities
     * @throws SQLException if database error occurs
     */
    long count() throws SQLException;
    
    /**
     * Counts entities by type.
     * 
     * @param entityType the entity type
     * @return count of entities of specified type
     * @throws SQLException if database error occurs
     */
    long countByType(EntityType entityType) throws SQLException;
}