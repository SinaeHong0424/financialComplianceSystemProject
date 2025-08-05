package com.dfs.compliance.service.impl;

import com.dfs.compliance.dao.FinancialEntityDAO;
import com.dfs.compliance.model.FinancialEntity;
import com.dfs.compliance.model.FinancialEntity.ComplianceStatus;
import com.dfs.compliance.model.FinancialEntity.EntityType;
import com.dfs.compliance.model.FinancialEntity.RiskLevel;
import com.dfs.compliance.service.FinancialEntityService;
import com.dfs.compliance.service.ServiceException;
import com.dfs.compliance.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of FinancialEntityService interface.
 * Provides business logic and transaction management for financial entity operations.
 * 
 * @author DFS Technology Bureau
 * @version 1.0
 * @since 2025-08-06
 */
public class FinancialEntityServiceImpl implements FinancialEntityService {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialEntityServiceImpl.class);
    
    private final FinancialEntityDAO entityDAO;
    
    // Business rule constants
    private static final int DEFAULT_REVIEW_INTERVAL_MONTHS = 12;
    private static final int HIGH_RISK_REVIEW_INTERVAL_MONTHS = 6;
    private static final int CRITICAL_RISK_REVIEW_INTERVAL_MONTHS = 3;
    private static final int LICENSE_EXPIRY_WARNING_DAYS = 60;
    
    /**
     * Constructs service with DAO dependency.
     * 
     * @param entityDAO the entity DAO
     */
    public FinancialEntityServiceImpl(FinancialEntityDAO entityDAO) {
        if (entityDAO == null) {
            throw new IllegalArgumentException("FinancialEntityDAO cannot be null");
        }
        this.entityDAO = entityDAO;
    }
    
    // ==================== CRUD Operations ====================
    
    @Override
    public FinancialEntity registerEntity(FinancialEntity entity, String registeredBy) {
        logger.info("Registering new entity: {} by {}", entity.getEntityName(), registeredBy);
        
        try {
            // Validate entity
            ValidationResult validation = validateEntityRegistration(entity);
            if (!validation.isValid()) {
                String errors = String.join(", ", validation.getErrors());
                throw new IllegalArgumentException("Entity validation failed: " + errors);
            }
            
            // Set registration metadata
            entity.setRegistrationDate(LocalDate.now());
            entity.setCreatedDate(LocalDateTime.now());
            entity.setCreatedBy(registeredBy);
            entity.setActive(true);
            
            // Set initial review date based on risk level
            entity.setNextReviewDate(calculateNextReviewDate(entity.getRiskLevel()));
            
            // Initialize default values
            if (entity.getState() == null || entity.getState().isEmpty()) {
                entity.setState("NY");
            }
            
            // Create entity
            FinancialEntity registered = executeInTransaction(conn -> {
                return entityDAO.create(entity);
            });
            
            logger.info("Successfully registered entity ID: {} ({})", 
                       registered.getEntityId(), registered.getEntityName());
            
            return registered;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Entity registration validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error registering entity: {}", e.getMessage(), e);
            throw new ServiceException("Failed to register entity", e);
        }
    }
    
    @Override
    public Optional<FinancialEntity> getEntityById(Long entityId) {
        logger.debug("Retrieving entity by ID: {}", entityId);
        
        try {
            return executeInTransaction(conn -> {
                return entityDAO.findById(entityId);
            });
        } catch (Exception e) {
            logger.error("Error retrieving entity ID {}: {}", entityId, e.getMessage(), e);
            throw new ServiceException("Failed to retrieve entity", e);
        }
    }
    
    @Override
    public boolean updateEntity(FinancialEntity entity, String modifiedBy) {
        logger.info("Updating entity ID: {} by {}", entity.getEntityId(), modifiedBy);
        
        try {
            // Validate entity exists
            Optional<FinancialEntity> existing = getEntityById(entity.getEntityId());
            if (!existing.isPresent()) {
                throw new IllegalArgumentException("Entity not found: " + entity.getEntityId());
            }
            
            // Validate update
            ValidationResult validation = validateEntityRegistration(entity);
            if (!validation.isValid()) {
                String errors = String.join(", ", validation.getErrors());
                throw new IllegalArgumentException("Entity validation failed: " + errors);
            }
            
            // Set modification metadata
            entity.setModifiedDate(LocalDateTime.now());
            entity.setModifiedBy(modifiedBy);
            
            // Update entity
            boolean updated = executeInTransaction(conn -> {
                return entityDAO.update(entity);
            });
            
            if (updated) {
                logger.info("Successfully updated entity ID: {}", entity.getEntityId());
            }
            
            return updated;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Entity update validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating entity ID {}: {}", entity.getEntityId(), e.getMessage(), e);
            throw new ServiceException("Failed to update entity", e);
        }
    }
    
    @Override
    public boolean deactivateEntity(Long entityId, String deactivatedBy) {
        logger.info("Deactivating entity ID: {} by {}", entityId, deactivatedBy);
        
        try {
            Optional<FinancialEntity> entity = getEntityById(entityId);
            if (!entity.isPresent()) {
                throw new IllegalArgumentException("Entity not found: " + entityId);
            }
            
            FinancialEntity entityToUpdate = entity.get();
            entityToUpdate.setActive(false);
            entityToUpdate.setModifiedDate(LocalDateTime.now());
            entityToUpdate.setModifiedBy(deactivatedBy);
            
            boolean deactivated = executeInTransaction(conn -> {
                return entityDAO.update(entityToUpdate);
            });
            
            if (deactivated) {
                logger.info("Successfully deactivated entity ID: {}", entityId);
            }
            
            return deactivated;
            
        } catch (Exception e) {
            logger.error("Error deactivating entity ID {}: {}", entityId, e.getMessage(), e);
            throw new ServiceException("Failed to deactivate entity", e);
        }
    }
    
    @Override
    public boolean deleteEntity(Long entityId, String deletedBy) {
        logger.warn("Permanently deleting entity ID: {} by {}", entityId, deletedBy);
        
        try {
            // Verify entity exists
            Optional<FinancialEntity> entity = getEntityById(entityId);
            if (!entity.isPresent()) {
                throw new IllegalArgumentException("Entity not found: " + entityId);
            }
            
            // Log the deletion for audit
            logger.warn("PERMANENT DELETE: Entity ID {} ({}) deleted by {}", 
                       entityId, entity.get().getEntityName(), deletedBy);
            
            boolean deleted = executeInTransaction(conn -> {
                return entityDAO.delete(entityId);
            });
            
            if (deleted) {
                logger.info("Successfully deleted entity ID: {}", entityId);
            }
            
            return deleted;
            
        } catch (Exception e) {
            logger.error("Error deleting entity ID {}: {}", entityId, e.getMessage(), e);
            throw new ServiceException("Failed to delete entity", e);
        }
    }
    // ==================== Query Operations ====================
    
    @Override
    public List<FinancialEntity> getAllActiveEntities() {
        logger.debug("Retrieving all active entities");
        
        try {
            List<FinancialEntity> allEntities = executeInTransaction(conn -> {
                return entityDAO.findAll();
            });
            
            // Filter for active entities only
            List<FinancialEntity> activeEntities = allEntities.stream()
                .filter(FinancialEntity::isActive)
                .collect(Collectors.toList());
            
            logger.info("Retrieved {} active entities", activeEntities.size());
            return activeEntities;
            
        } catch (Exception e) {
            logger.error("Error retrieving active entities: {}", e.getMessage(), e);
            throw new ServiceException("Failed to retrieve active entities", e);
        }
    }
    
    @Override
    public List<FinancialEntity> getEntitiesByType(EntityType type) {
        logger.debug("Retrieving entities by type: {}", type);
        
        try {
            List<FinancialEntity> entities = executeInTransaction(conn -> {
                return entityDAO.findByType(type);
            });
            
            logger.info("Retrieved {} entities of type {}", entities.size(), type);
            return entities;
            
        } catch (Exception e) {
            logger.error("Error retrieving entities by type {}: {}", type, e.getMessage(), e);
            throw new ServiceException("Failed to retrieve entities by type", e);
        }
    }
    
    @Override
    public List<FinancialEntity> getEntitiesByComplianceStatus(ComplianceStatus status) {
        logger.debug("Retrieving entities by compliance status: {}", status);
        
        try {
            List<FinancialEntity> entities = executeInTransaction(conn -> {
                return entityDAO.findByComplianceStatus(status);
            });
            
            logger.info("Retrieved {} entities with status {}", entities.size(), status);
            return entities;
            
        } catch (Exception e) {
            logger.error("Error retrieving entities by status {}: {}", status, e.getMessage(), e);
            throw new ServiceException("Failed to retrieve entities by status", e);
        }
    }
    
    @Override
    public List<FinancialEntity> getEntitiesByRiskLevel(RiskLevel riskLevel) {
        logger.debug("Retrieving entities by risk level: {}", riskLevel);
        
        try {
            List<FinancialEntity> entities = executeInTransaction(conn -> {
                return entityDAO.findByRiskLevel(riskLevel);
            });
            
            logger.info("Retrieved {} entities with risk level {}", entities.size(), riskLevel);
            return entities;
            
        } catch (Exception e) {
            logger.error("Error retrieving entities by risk level {}: {}", riskLevel, e.getMessage(), e);
            throw new ServiceException("Failed to retrieve entities by risk level", e);
        }
    }
    
    @Override
    public List<FinancialEntity> searchEntitiesByName(String searchTerm) {
        logger.debug("Searching entities by name: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }
        
        try {
            List<FinancialEntity> entities = executeInTransaction(conn -> {
                return entityDAO.searchByName(searchTerm.trim());
            });
            
            logger.info("Found {} entities matching '{}'", entities.size(), searchTerm);
            return entities;
            
        } catch (Exception e) {
            logger.error("Error searching entities by name: {}", e.getMessage(), e);
            throw new ServiceException("Failed to search entities", e);
        }
    }
    
    // ==================== Compliance Operations ====================
    
    @Override
    public boolean updateComplianceStatus(Long entityId, ComplianceStatus newStatus, 
                                         String updatedBy, String reason) {
        logger.info("Updating compliance status for entity ID {} to {} by {}", 
                   entityId, newStatus, updatedBy);
        
        try {
            // Validate entity exists
            Optional<FinancialEntity> entityOpt = getEntityById(entityId);
            if (!entityOpt.isPresent()) {
                throw new IllegalArgumentException("Entity not found: " + entityId);
            }
            
            FinancialEntity entity = entityOpt.get();
            ComplianceStatus oldStatus = entity.getComplianceStatus();
            
            // Validate status transition
            validateStatusTransition(oldStatus, newStatus);
            
            // Update entity
            boolean updated = executeInTransaction(conn -> {
                return entityDAO.updateComplianceStatus(entityId, newStatus, updatedBy);
            });
            
            if (updated) {
                logger.info("Successfully updated compliance status for entity ID {} from {} to {}", 
                           entityId, oldStatus, newStatus);
                logger.info("Reason: {} | Updated by: {}", reason, updatedBy);
                
                // Trigger any necessary alerts or workflows
                handleComplianceStatusChange(entity, oldStatus, newStatus);
            }
            
            return updated;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Status update validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating compliance status for entity ID {}: {}", 
                        entityId, e.getMessage(), e);
            throw new ServiceException("Failed to update compliance status", e);
        }
    }
    
    @Override
    public boolean updateRiskLevel(Long entityId, RiskLevel newRiskLevel, 
                                  String updatedBy, String reason) {
        logger.info("Updating risk level for entity ID {} to {} by {}", 
                   entityId, newRiskLevel, updatedBy);
        
        try {
            // Validate entity exists
            Optional<FinancialEntity> entityOpt = getEntityById(entityId);
            if (!entityOpt.isPresent()) {
                throw new IllegalArgumentException("Entity not found: " + entityId);
            }
            
            FinancialEntity entity = entityOpt.get();
            RiskLevel oldRiskLevel = entity.getRiskLevel();
            
            // Update risk level
            boolean updated = executeInTransaction(conn -> {
                return entityDAO.updateRiskLevel(entityId, newRiskLevel, updatedBy);
            });
            
            if (updated) {
                logger.info("Successfully updated risk level for entity ID {} from {} to {}", 
                           entityId, oldRiskLevel, newRiskLevel);
                logger.info("Reason: {} | Updated by: {}", reason, updatedBy);
                
                // Update review schedule based on new risk level
                updateReviewSchedule(entityId, newRiskLevel, updatedBy);
                
                // Trigger escalation if risk increased
                if (isRiskEscalation(oldRiskLevel, newRiskLevel)) {
                    handleRiskEscalation(entity, oldRiskLevel, newRiskLevel);
                }
            }
            
            return updated;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Risk level update validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating risk level for entity ID {}: {}", 
                        entityId, e.getMessage(), e);
            throw new ServiceException("Failed to update risk level", e);
        }
    }
    
    @Override
    public boolean conductComplianceReview(Long entityId, String reviewedBy,
                                          ComplianceStatus complianceStatus,
                                          RiskLevel riskLevel, String notes) {
        logger.info("Conducting compliance review for entity ID {} by {}", entityId, reviewedBy);
        
        try {
            // Get entity
            Optional<FinancialEntity> entityOpt = getEntityById(entityId);
            if (!entityOpt.isPresent()) {
                throw new IllegalArgumentException("Entity not found: " + entityId);
            }
            
            FinancialEntity entity = entityOpt.get();
            
            // Update entity with review results
            entity.setComplianceStatus(complianceStatus);
            entity.setRiskLevel(riskLevel);
            entity.setLastReviewDate(LocalDate.now());
            entity.setNextReviewDate(calculateNextReviewDate(riskLevel));
            
            if (notes != null && !notes.trim().isEmpty()) {
                String updatedNotes = entity.getNotes() == null ? notes :
                    entity.getNotes() + "\n\n[" + LocalDate.now() + "] Review by " + reviewedBy + ":\n" + notes;
                entity.setNotes(updatedNotes);
            }
            
            entity.setModifiedDate(LocalDateTime.now());
            entity.setModifiedBy(reviewedBy);
            
            // Save updated entity
            boolean reviewed = executeInTransaction(conn -> {
                return entityDAO.update(entity);
            });
            
            if (reviewed) {
                logger.info("Successfully completed compliance review for entity ID {}", entityId);
                logger.info("Results: Status={}, Risk={}, Next Review={}", 
                           complianceStatus, riskLevel, entity.getNextReviewDate());
            }
            
            return reviewed;
            
        } catch (Exception e) {
            logger.error("Error conducting compliance review for entity ID {}: {}", 
                        entityId, e.getMessage(), e);
            throw new ServiceException("Failed to conduct compliance review", e);
        }
    }
    // ==================== Alert & Monitoring Operations ====================
    
    @Override
    public List<FinancialEntity> getEntitiesWithExpiringLicenses(int daysThreshold) {
        logger.debug("Retrieving entities with licenses expiring within {} days", daysThreshold);
        
        if (daysThreshold < 0) {
            throw new IllegalArgumentException("Days threshold must be positive");
        }
        
        try {
            List<FinancialEntity> entities = executeInTransaction(conn -> {
                return entityDAO.findEntitiesWithExpiringLicenses(daysThreshold);
            });
            
            logger.info("Found {} entities with licenses expiring within {} days", 
                       entities.size(), daysThreshold);
            
            return entities;
            
        } catch (Exception e) {
            logger.error("Error retrieving entities with expiring licenses: {}", e.getMessage(), e);
            throw new ServiceException("Failed to retrieve entities with expiring licenses", e);
        }
    }
    
    @Override
    public List<FinancialEntity> getEntitiesWithOverdueReviews() {
        logger.debug("Retrieving entities with overdue reviews");
        
        try {
            List<FinancialEntity> entities = executeInTransaction(conn -> {
                return entityDAO.findEntitiesWithOverdueReviews();
            });
            
            logger.info("Found {} entities with overdue reviews", entities.size());
            
            return entities;
            
        } catch (Exception e) {
            logger.error("Error retrieving entities with overdue reviews: {}", e.getMessage(), e);
            throw new ServiceException("Failed to retrieve entities with overdue reviews", e);
        }
    }
    
    @Override
    public List<FinancialEntity> getNonCompliantEntities() {
        logger.debug("Retrieving non-compliant entities");
        
        try {
            List<FinancialEntity> entities = executeInTransaction(conn -> {
                return entityDAO.findByComplianceStatus(ComplianceStatus.NON_COMPLIANT);
            });
            
            logger.info("Found {} non-compliant entities", entities.size());
            
            return entities;
            
        } catch (Exception e) {
            logger.error("Error retrieving non-compliant entities: {}", e.getMessage(), e);
            throw new ServiceException("Failed to retrieve non-compliant entities", e);
        }
    }
    
    @Override
    public List<FinancialEntity> getHighRiskEntities() {
        logger.debug("Retrieving high-risk entities");
        
        try {
            List<FinancialEntity> highRisk = executeInTransaction(conn -> {
                return entityDAO.findByRiskLevel(RiskLevel.HIGH);
            });
            
            List<FinancialEntity> critical = executeInTransaction(conn -> {
                return entityDAO.findByRiskLevel(RiskLevel.CRITICAL);
            });
            
            List<FinancialEntity> allHighRisk = new ArrayList<>();
            allHighRisk.addAll(critical);
            allHighRisk.addAll(highRisk);
            
            logger.info("Found {} high-risk/critical entities (Critical: {}, High: {})", 
                       allHighRisk.size(), critical.size(), highRisk.size());
            
            return allHighRisk;
            
        } catch (Exception e) {
            logger.error("Error retrieving high-risk entities: {}", e.getMessage(), e);
            throw new ServiceException("Failed to retrieve high-risk entities", e);
        }
    }
    
    // ==================== License Management ====================
    
    @Override
    public boolean renewLicense(Long entityId, LocalDate newExpiryDate, String renewedBy) {
        logger.info("Renewing license for entity ID {} to {} by {}", 
                   entityId, newExpiryDate, renewedBy);
        
        try {
            // Validate entity exists
            Optional<FinancialEntity> entityOpt = getEntityById(entityId);
            if (!entityOpt.isPresent()) {
                throw new IllegalArgumentException("Entity not found: " + entityId);
            }
            
            // Validate new expiry date
            if (newExpiryDate == null || newExpiryDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("New expiry date must be in the future");
            }
            
            FinancialEntity entity = entityOpt.get();
            entity.setLicenseExpiry(newExpiryDate);
            entity.setModifiedDate(LocalDateTime.now());
            entity.setModifiedBy(renewedBy);
            
            // Add renewal note
            String renewalNote = "[" + LocalDate.now() + "] License renewed by " + renewedBy + 
                               ". New expiry: " + newExpiryDate;
            String updatedNotes = entity.getNotes() == null ? renewalNote :
                entity.getNotes() + "\n" + renewalNote;
            entity.setNotes(updatedNotes);
            
            boolean renewed = executeInTransaction(conn -> {
                return entityDAO.update(entity);
            });
            
            if (renewed) {
                logger.info("Successfully renewed license for entity ID {} until {}", 
                           entityId, newExpiryDate);
            }
            
            return renewed;
            
        } catch (IllegalArgumentException e) {
            logger.warn("License renewal validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error renewing license for entity ID {}: {}", entityId, e.getMessage(), e);
            throw new ServiceException("Failed to renew license", e);
        }
    }
    
    @Override
    public boolean suspendLicense(Long entityId, String suspendedBy, String reason) {
        logger.info("Suspending license for entity ID {} by {}", entityId, suspendedBy);
        
        try {
            return updateComplianceStatus(entityId, ComplianceStatus.SUSPENDED, 
                                        suspendedBy, reason);
        } catch (Exception e) {
            logger.error("Error suspending license for entity ID {}: {}", entityId, e.getMessage(), e);
            throw new ServiceException("Failed to suspend license", e);
        }
    }
    
    @Override
    public boolean reinstateLicense(Long entityId, String reinstatedBy, String reason) {
        logger.info("Reinstating license for entity ID {} by {}", entityId, reinstatedBy);
        
        try {
            // Get entity to check current status
            Optional<FinancialEntity> entityOpt = getEntityById(entityId);
            if (!entityOpt.isPresent()) {
                throw new IllegalArgumentException("Entity not found: " + entityId);
            }
            
            FinancialEntity entity = entityOpt.get();
            if (entity.getComplianceStatus() != ComplianceStatus.SUSPENDED) {
                throw new IllegalArgumentException("Entity is not suspended");
            }
            
            // Reinstate to pending review for re-evaluation
            return updateComplianceStatus(entityId, ComplianceStatus.PENDING_REVIEW, 
                                        reinstatedBy, reason);
            
        } catch (IllegalArgumentException e) {
            logger.warn("License reinstatement validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error reinstating license for entity ID {}: {}", 
                        entityId, e.getMessage(), e);
            throw new ServiceException("Failed to reinstate license", e);
        }
    }
    
    // ==================== Reporting & Analytics ====================
    
    @Override
    public ComplianceSummary getComplianceSummary() {
        logger.debug("Generating compliance summary");
        
        try {
            ComplianceSummary summary = new ComplianceSummary();
            
            // Get total count
            long totalCount = executeInTransaction(conn -> {
                return entityDAO.count();
            });
            summary.setTotalEntities(totalCount);
            
            // Get counts by compliance status
            summary.setCompliantCount(getEntitiesByComplianceStatus(ComplianceStatus.COMPLIANT).size());
            summary.setNonCompliantCount(getEntitiesByComplianceStatus(ComplianceStatus.NON_COMPLIANT).size());
            summary.setPendingReviewCount(getEntitiesByComplianceStatus(ComplianceStatus.PENDING_REVIEW).size());
            summary.setUnderInvestigationCount(getEntitiesByComplianceStatus(ComplianceStatus.UNDER_INVESTIGATION).size());
            summary.setSuspendedCount(getEntitiesByComplianceStatus(ComplianceStatus.SUSPENDED).size());
            
            // Get counts by risk level
            summary.setLowRiskCount(getEntitiesByRiskLevel(RiskLevel.LOW).size());
            summary.setMediumRiskCount(getEntitiesByRiskLevel(RiskLevel.MEDIUM).size());
            summary.setHighRiskCount(getEntitiesByRiskLevel(RiskLevel.HIGH).size());
            summary.setCriticalRiskCount(getEntitiesByRiskLevel(RiskLevel.CRITICAL).size());
            
            // Get alert counts
            summary.setExpiringSoon(getEntitiesWithExpiringLicenses(LICENSE_EXPIRY_WARNING_DAYS).size());
            summary.setOverdueReviews(getEntitiesWithOverdueReviews().size());
            
            logger.info("Generated compliance summary: {} total entities, {} compliant, {} non-compliant", 
                       summary.getTotalEntities(), summary.getCompliantCount(), 
                       summary.getNonCompliantCount());
            
            return summary;
            
        } catch (Exception e) {
            logger.error("Error generating compliance summary: {}", e.getMessage(), e);
            throw new ServiceException("Failed to generate compliance summary", e);
        }
    }
    
    @Override
    public List<FinancialEntity> getEntitiesRequiringReview(int daysThreshold) {
        logger.debug("Retrieving entities requiring review within {} days", daysThreshold);
        
        try {
            List<FinancialEntity> allEntities = getAllActiveEntities();
            LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
            
            List<FinancialEntity> requiresReview = allEntities.stream()
                .filter(entity -> {
                    LocalDate nextReview = entity.getNextReviewDate();
                    return nextReview != null && !nextReview.isAfter(thresholdDate);
                })
                .collect(Collectors.toList());
            
            logger.info("Found {} entities requiring review within {} days", 
                       requiresReview.size(), daysThreshold);
            
            return requiresReview;
            
        } catch (Exception e) {
            logger.error("Error retrieving entities requiring review: {}", e.getMessage(), e);
            throw new ServiceException("Failed to retrieve entities requiring review", e);
        }
    }
    @Override
    public ValidationResult validateEntityRegistration(FinancialEntity entity) {
        List<String> errors = new ArrayList<>();
        
        if (entity == null) {
            errors.add("Entity cannot be null");
            return new ValidationResult(false, errors);
        }
        
        // Required field validations
        if (entity.getEntityName() == null || entity.getEntityName().trim().isEmpty()) {
            errors.add("Entity name is required");
        }
        
        if (entity.getEntityType() == null) {
            errors.add("Entity type is required");
        }
        
        if (entity.getLicenseNumber() == null || entity.getLicenseNumber().trim().isEmpty()) {
            errors.add("License number is required");
        }
        
        if (entity.getComplianceStatus() == null) {
            errors.add("Compliance status is required");
        }
        
        if (entity.getRiskLevel() == null) {
            errors.add("Risk level is required");
        }
        
        // Contact information validation
        if (entity.getContactEmail() != null && !entity.getContactEmail().isEmpty()) {
            if (!isValidEmail(entity.getContactEmail())) {
                errors.add("Invalid email format: " + entity.getContactEmail());
            }
        }
        
        if (entity.getContactPhone() != null && !entity.getContactPhone().isEmpty()) {
            if (!isValidPhone(entity.getContactPhone())) {
                errors.add("Invalid phone format: " + entity.getContactPhone());
            }
        }
        
        // Address validation
        if (entity.getState() != null && entity.getState().length() != 2) {
            errors.add("State must be 2-character code");
        }
        
        if (entity.getZipCode() != null && !entity.getZipCode().isEmpty()) {
            if (!isValidZipCode(entity.getZipCode())) {
                errors.add("Invalid ZIP code format: " + entity.getZipCode());
            }
        }
        
        // License expiry validation
        if (entity.getLicenseExpiry() != null && entity.getLicenseExpiry().isBefore(LocalDate.now())) {
            errors.add("License expiry date cannot be in the past");
        }
        
        // Financial validation
        if (entity.getTotalAssets() != null && entity.getTotalAssets().signum() < 0) {
            errors.add("Total assets cannot be negative");
        }
        
        if (entity.getEmployeeCount() != null && entity.getEmployeeCount() < 0) {
            errors.add("Employee count cannot be negative");
        }
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors);
    }
    
    @Override
    public long getTotalEntityCount() {
        logger.debug("Getting total entity count");
        
        try {
            long count = executeInTransaction(conn -> {
                return entityDAO.count();
            });
            
            logger.debug("Total entity count: {}", count);
            return count;
            
        } catch (Exception e) {
            logger.error("Error getting total entity count: {}", e.getMessage(), e);
            throw new ServiceException("Failed to get entity count", e);
        }
    }
    
    @Override
    public long getEntityCountByType(EntityType type) {
        logger.debug("Getting entity count for type: {}", type);
        
        try {
            long count = executeInTransaction(conn -> {
                return entityDAO.countByType(type);
            });
            
            logger.debug("Entity count for type {}: {}", type, count);
            return count;
            
        } catch (Exception e) {
            logger.error("Error getting entity count by type: {}", e.getMessage(), e);
            throw new ServiceException("Failed to get entity count by type", e);
        }
    }
    
    // ==================== Private Helper Methods ====================
    
    /**
     * Executes a database operation within a transaction.
     */
    private <T> T executeInTransaction(TransactionCallback<T> callback) throws SQLException {
        Connection conn = null;
        try {
            // Get connection using static method
            conn = DatabaseConnection.getConnection();
            // Connection already has autoCommit=false from DatabaseConnection.getConnection()
            
            // Use the injected DAO instead of creating new instance
            T result = callback.execute(entityDAO);
            
            conn.commit();
            return result;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Transaction rolled back due to error: {}", e.getMessage());
                } catch (SQLException rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection: {}", closeEx.getMessage());
                }
            }
        }
    }
    
    /**
     * Functional interface for transaction callbacks.
     */
    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute(FinancialEntityDAO dao) throws SQLException;
    }
    
    /**
     * Calculates next review date based on risk level.
     */
    private LocalDate calculateNextReviewDate(RiskLevel riskLevel) {
        LocalDate today = LocalDate.now();
        
        switch (riskLevel) {
            case CRITICAL:
                return today.plusMonths(CRITICAL_RISK_REVIEW_INTERVAL_MONTHS);
            case HIGH:
                return today.plusMonths(HIGH_RISK_REVIEW_INTERVAL_MONTHS);
            case MEDIUM:
            case LOW:
            default:
                return today.plusMonths(DEFAULT_REVIEW_INTERVAL_MONTHS);
        }
    }
    
    /**
     * Updates review schedule based on risk level.
     */
    private void updateReviewSchedule(Long entityId, RiskLevel newRiskLevel, String updatedBy) {
        try {
            Optional<FinancialEntity> entityOpt = getEntityById(entityId);
            if (entityOpt.isPresent()) {
                FinancialEntity entity = entityOpt.get();
                entity.setNextReviewDate(calculateNextReviewDate(newRiskLevel));
                entity.setModifiedDate(LocalDateTime.now());
                entity.setModifiedBy(updatedBy);
                
                executeInTransaction(conn -> {
                    return entityDAO.update(entity);
                });
                
                logger.info("Updated review schedule for entity ID {}: Next review = {}", 
                           entityId, entity.getNextReviewDate());
            }
        } catch (Exception e) {
            logger.warn("Could not update review schedule: {}", e.getMessage());
        }
    }
    
    /**
     * Validates compliance status transition.
     */
    private void validateStatusTransition(ComplianceStatus from, ComplianceStatus to) {
        // Business rule: Cannot go directly from SUSPENDED to COMPLIANT
        if (from == ComplianceStatus.SUSPENDED && to == ComplianceStatus.COMPLIANT) {
            throw new IllegalArgumentException(
                "Cannot transition from SUSPENDED to COMPLIANT directly. " +
                "Must go through PENDING_REVIEW first.");
        }
        
        // Business rule: Cannot go from UNDER_INVESTIGATION to COMPLIANT without review
        if (from == ComplianceStatus.UNDER_INVESTIGATION && to == ComplianceStatus.COMPLIANT) {
            logger.warn("Transitioning from UNDER_INVESTIGATION to COMPLIANT - ensure proper review conducted");
        }
    }
    
    /**
     * Handles compliance status change events.
     */
    private void handleComplianceStatusChange(FinancialEntity entity, 
                                             ComplianceStatus oldStatus, 
                                             ComplianceStatus newStatus) {
        logger.info("Handling compliance status change for entity ID {}: {} -> {}", 
                   entity.getEntityId(), oldStatus, newStatus);
        
        // Trigger alerts for critical status changes
        if (newStatus == ComplianceStatus.NON_COMPLIANT || 
            newStatus == ComplianceStatus.UNDER_INVESTIGATION ||
            newStatus == ComplianceStatus.SUSPENDED) {
            logger.warn("ALERT: Entity {} ({}) status changed to {}", 
                       entity.getEntityId(), entity.getEntityName(), newStatus);
            // TODO: Send notification to compliance team
        }
    }
    
    /**
     * Determines if risk level change is an escalation.
     */
    private boolean isRiskEscalation(RiskLevel from, RiskLevel to) {
        int[] riskOrder = {0, 1, 2, 3}; // LOW, MEDIUM, HIGH, CRITICAL
        return riskOrder[to.ordinal()] > riskOrder[from.ordinal()];
    }
    
    /**
     * Handles risk escalation events.
     */
    private void handleRiskEscalation(FinancialEntity entity, RiskLevel from, RiskLevel to) {
        logger.warn("RISK ESCALATION: Entity {} ({}) risk level increased from {} to {}", 
                   entity.getEntityId(), entity.getEntityName(), from, to);
        
        if (to == RiskLevel.CRITICAL) {
            logger.error("CRITICAL RISK: Entity {} requires immediate attention", 
                        entity.getEntityId());
            // TODO: Send urgent notification to management
        }
    }
    
    /**
     * Validates email format.
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Validates phone format.
     */
    private boolean isValidPhone(String phone) {
        // Remove common formatting characters
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)\\.]+", "");
        // Check if it's 10-11 digits (with optional country code)
        return cleanPhone.matches("^\\d{10,11}$");
    }
    
    /**
     * Validates ZIP code format.
     */
    private boolean isValidZipCode(String zipCode) {
        // Support both 5-digit and ZIP+4 formats
        return zipCode.matches("^\\d{5}(-\\d{4})?$");
    }
}