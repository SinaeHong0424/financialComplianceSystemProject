package com.dfs.compliance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.dfs.compliance.model.FinancialEntity;
import com.dfs.compliance.model.FinancialEntity.ComplianceStatus;
import com.dfs.compliance.model.FinancialEntity.EntityType;
import com.dfs.compliance.model.FinancialEntity.RiskLevel;

/**
 * Service interface for FinancialEntity business operations.
 * Provides business logic layer between presentation and data access layers.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Transaction management</li>
 *   <li>Business rule validation</li>
 *   <li>Complex business operations</li>
 *   <li>Cross-cutting concerns (logging, error handling)</li>
 * </ul>
 * 
 * @author DFS Technology Bureau
 * @version 1.0
 * @since 2025-08-06
 */
public interface FinancialEntityService {
    
    // ==================== CRUD Operations ====================
    
    /**
     * Registers a new financial entity in the system.
     * Performs business validation and initializes default values.
     * 
     * @param entity the entity to register
     * @param registeredBy the user performing the registration
     * @return the registered entity with generated ID
     * @throws IllegalArgumentException if validation fails
     * @throws ServiceException if registration fails
     */
    FinancialEntity registerEntity(FinancialEntity entity, String registeredBy);
    
    /**
     * Retrieves a financial entity by ID.
     * 
     * @param entityId the entity ID
     * @return Optional containing the entity if found
     * @throws ServiceException if retrieval fails
     */
    Optional<FinancialEntity> getEntityById(Long entityId);
    
    /**
     * Updates an existing financial entity.
     * Validates business rules and tracks modifications.
     * 
     * @param entity the entity with updated information
     * @param modifiedBy the user performing the update
     * @return true if update successful
     * @throws IllegalArgumentException if validation fails
     * @throws ServiceException if update fails
     */
    boolean updateEntity(FinancialEntity entity, String modifiedBy);
    
    /**
     * Deactivates a financial entity (soft delete).
     * 
     * @param entityId the entity ID
     * @param deactivatedBy the user performing deactivation
     * @return true if deactivation successful
     * @throws ServiceException if deactivation fails
     */
    boolean deactivateEntity(Long entityId, String deactivatedBy);
    
    /**
     * Permanently deletes a financial entity (hard delete).
     * Should only be used in exceptional circumstances.
     * 
     * @param entityId the entity ID
     * @param deletedBy the user performing deletion
     * @return true if deletion successful
     * @throws ServiceException if deletion fails
     */
    boolean deleteEntity(Long entityId, String deletedBy);
    
    // ==================== Query Operations ====================
    
    /**
     * Retrieves all active financial entities.
     * 
     * @return list of active entities
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getAllActiveEntities();
    
    /**
     * Retrieves all entities of a specific type.
     * 
     * @param type the entity type
     * @return list of matching entities
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getEntitiesByType(EntityType type);
    
    /**
     * Retrieves entities by compliance status.
     * 
     * @param status the compliance status
     * @return list of matching entities
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getEntitiesByComplianceStatus(ComplianceStatus status);
    
    /**
     * Retrieves entities by risk level.
     * 
     * @param riskLevel the risk level
     * @return list of matching entities
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getEntitiesByRiskLevel(RiskLevel riskLevel);
    
    /**
     * Searches entities by name (partial, case-insensitive).
     * 
     * @param searchTerm the search term
     * @return list of matching entities
     * @throws ServiceException if search fails
     */
    List<FinancialEntity> searchEntitiesByName(String searchTerm);
    
    // ==================== Compliance Operations ====================
    
    /**
     * Updates compliance status with business rule validation.
     * Automatically updates review dates and sends notifications.
     * 
     * @param entityId the entity ID
     * @param newStatus the new compliance status
     * @param updatedBy the user performing update
     * @param reason reason for status change
     * @return true if update successful
     * @throws IllegalArgumentException if status transition invalid
     * @throws ServiceException if update fails
     */
    boolean updateComplianceStatus(Long entityId, ComplianceStatus newStatus, 
                                   String updatedBy, String reason);
    
    /**
     * Updates risk level with validation and escalation rules.
     * 
     * @param entityId the entity ID
     * @param newRiskLevel the new risk level
     * @param updatedBy the user performing update
     * @param reason reason for risk level change
     * @return true if update successful
     * @throws IllegalArgumentException if risk level invalid
     * @throws ServiceException if update fails
     */
    boolean updateRiskLevel(Long entityId, RiskLevel newRiskLevel, 
                           String updatedBy, String reason);
    
    /**
     * Performs a compliance review for an entity.
     * Updates last review date and schedules next review.
     * 
     * @param entityId the entity ID
     * @param reviewedBy the user performing review
     * @param complianceStatus the compliance status after review
     * @param riskLevel the risk level after review
     * @param notes review notes
     * @return true if review successful
     * @throws ServiceException if review fails
     */
    boolean conductComplianceReview(Long entityId, String reviewedBy,
                                   ComplianceStatus complianceStatus,
                                   RiskLevel riskLevel, String notes);
    
    // ==================== Alert & Monitoring Operations ====================
    
    /**
     * Gets entities with licenses expiring within specified days.
     * 
     * @param daysThreshold days to look ahead
     * @return list of entities with expiring licenses
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getEntitiesWithExpiringLicenses(int daysThreshold);
    
    /**
     * Gets entities with overdue compliance reviews.
     * 
     * @return list of entities with overdue reviews
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getEntitiesWithOverdueReviews();
    
    /**
     * Gets all non-compliant entities requiring attention.
     * 
     * @return list of non-compliant entities
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getNonCompliantEntities();
    
    /**
     * Gets all high-risk and critical entities.
     * 
     * @return list of high-risk entities
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getHighRiskEntities();
    
    // ==================== License Management ====================
    
    /**
     * Renews entity license with validation.
     * 
     * @param entityId the entity ID
     * @param newExpiryDate the new expiry date
     * @param renewedBy the user performing renewal
     * @return true if renewal successful
     * @throws IllegalArgumentException if date invalid
     * @throws ServiceException if renewal fails
     */
    boolean renewLicense(Long entityId, LocalDate newExpiryDate, String renewedBy);
    
    /**
     * Suspends entity license.
     * 
     * @param entityId the entity ID
     * @param suspendedBy the user performing suspension
     * @param reason suspension reason
     * @return true if suspension successful
     * @throws ServiceException if suspension fails
     */
    boolean suspendLicense(Long entityId, String suspendedBy, String reason);
    
    /**
     * Reinstates suspended license.
     * 
     * @param entityId the entity ID
     * @param reinstatedBy the user performing reinstatement
     * @param reason reinstatement reason
     * @return true if reinstatement successful
     * @throws ServiceException if reinstatement fails
     */
    boolean reinstateLicense(Long entityId, String reinstatedBy, String reason);
    
    // ==================== Reporting & Analytics ====================
    
    /**
     * Gets compliance summary statistics.
     * 
     * @return summary with counts by status, risk level, type
     * @throws ServiceException if calculation fails
     */
    ComplianceSummary getComplianceSummary();
    
    /**
     * Gets entities requiring review within specified days.
     * 
     * @param daysThreshold days to look ahead
     * @return list of entities requiring review
     * @throws ServiceException if retrieval fails
     */
    List<FinancialEntity> getEntitiesRequiringReview(int daysThreshold);
    
    /**
     * Validates if entity meets registration requirements.
     * 
     * @param entity the entity to validate
     * @return validation result with errors if any
     */
    ValidationResult validateEntityRegistration(FinancialEntity entity);
    
    /**
     * Gets total entity count.
     * 
     * @return total count
     * @throws ServiceException if count fails
     */
    long getTotalEntityCount();
    
    /**
     * Gets entity count by type.
     * 
     * @param type the entity type
     * @return count for specified type
     * @throws ServiceException if count fails
     */
    long getEntityCountByType(EntityType type);
    
    // ==================== Utility Classes ====================
    
    /**
     * Compliance summary statistics.
     */
    class ComplianceSummary {
        private long totalEntities;
        private long compliantCount;
        private long nonCompliantCount;
        private long pendingReviewCount;
        private long underInvestigationCount;
        private long suspendedCount;
        private long lowRiskCount;
        private long mediumRiskCount;
        private long highRiskCount;
        private long criticalRiskCount;
        private long expiringSoon;
        private long overdueReviews;
        
        // Getters and setters
        public long getTotalEntities() { return totalEntities; }
        public void setTotalEntities(long totalEntities) { this.totalEntities = totalEntities; }
        
        public long getCompliantCount() { return compliantCount; }
        public void setCompliantCount(long compliantCount) { this.compliantCount = compliantCount; }
        
        public long getNonCompliantCount() { return nonCompliantCount; }
        public void setNonCompliantCount(long nonCompliantCount) { this.nonCompliantCount = nonCompliantCount; }
        
        public long getPendingReviewCount() { return pendingReviewCount; }
        public void setPendingReviewCount(long pendingReviewCount) { this.pendingReviewCount = pendingReviewCount; }
        
        public long getUnderInvestigationCount() { return underInvestigationCount; }
        public void setUnderInvestigationCount(long underInvestigationCount) { 
            this.underInvestigationCount = underInvestigationCount; 
        }
        
        public long getSuspendedCount() { return suspendedCount; }
        public void setSuspendedCount(long suspendedCount) { this.suspendedCount = suspendedCount; }
        
        public long getLowRiskCount() { return lowRiskCount; }
        public void setLowRiskCount(long lowRiskCount) { this.lowRiskCount = lowRiskCount; }
        
        public long getMediumRiskCount() { return mediumRiskCount; }
        public void setMediumRiskCount(long mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }
        
        public long getHighRiskCount() { return highRiskCount; }
        public void setHighRiskCount(long highRiskCount) { this.highRiskCount = highRiskCount; }
        
        public long getCriticalRiskCount() { return criticalRiskCount; }
        public void setCriticalRiskCount(long criticalRiskCount) { this.criticalRiskCount = criticalRiskCount; }
        
        public long getExpiringSoon() { return expiringSoon; }
        public void setExpiringSoon(long expiringSoon) { this.expiringSoon = expiringSoon; }
        
        public long getOverdueReviews() { return overdueReviews; }
        public void setOverdueReviews(long overdueReviews) { this.overdueReviews = overdueReviews; }
    }
    
    /**
     * Validation result.
     */
    class ValidationResult {
        private boolean valid;
        private List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
    }
}