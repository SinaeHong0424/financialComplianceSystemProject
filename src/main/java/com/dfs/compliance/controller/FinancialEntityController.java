package com.dfs.compliance.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfs.compliance.dto.ApiResponse;
import com.dfs.compliance.dto.DTOMapper;
import com.dfs.compliance.dto.FinancialEntityDTO;
import com.dfs.compliance.model.FinancialEntity;
import com.dfs.compliance.model.FinancialEntity.ComplianceStatus;
import com.dfs.compliance.model.FinancialEntity.EntityType;
import com.dfs.compliance.model.FinancialEntity.RiskLevel;
import com.dfs.compliance.service.FinancialEntityService;
import com.dfs.compliance.service.FinancialEntityService.ComplianceSummary;
import com.dfs.compliance.service.FinancialEntityService.ValidationResult;
import com.dfs.compliance.service.ServiceException;

/**
 * REST Controller for Financial Entity Management
 * Provides RESTful API endpoints for entity operations
 * 
 * Base URL: /api/entities
 * 
 * Endpoints organized by category:
 * - CRUD Operations (5)
 * - Query Operations (3)
 * - Compliance Operations (3)
 * - License Management (1)
 * - Alert Endpoints (4)
 * - Reporting (2)
 * 
 * Total: 17 RESTful endpoints
 * 
 * @author Jennifer Hong
 * @version 1.0
 * @since Day 11
 */
public class FinancialEntityController {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialEntityController.class);
    
    private final FinancialEntityService entityService;
    
    /**
     * Constructor with dependency injection
     * 
     * @param entityService the service layer implementation
     */
    public FinancialEntityController(FinancialEntityService entityService) {
        this.entityService = entityService;
    }
    
    // ========================================================================
    // CRUD OPERATIONS (5 endpoints)
    // ========================================================================
    
    /**
     * Register a new financial entity
     * POST /api/entities
     * 
     * @param dto entity data from request body
     * @param request HTTP request for audit trail
     * @return ApiResponse with created entity
     */
    public ApiResponse<FinancialEntityDTO> registerEntity(
            FinancialEntityDTO dto, 
            HttpServletRequest request) {
        try {
            logger.info("Registering new entity: {}", dto.getEntityName());
            
            String registeredBy = getUserFromRequest(request);
            FinancialEntity entity = DTOMapper.toEntity(dto);
            
            // Service returns the registered entity
            FinancialEntity registered = entityService.registerEntity(entity, registeredBy);
            FinancialEntityDTO responseDto = DTOMapper.toDTO(registered);
            
            logger.info("Successfully registered entity with ID: {}", registered.getEntityId());
            return ApiResponse.success(responseDto, "Entity registered successfully")
                    .addMetadata("entityId", registered.getEntityId());
                    
        } catch (ServiceException e) {
            logger.error("Service error registering entity: {}", e.getMessage());
            return ApiResponse.error("Failed to register entity: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error registering entity", e);
            return ApiResponse.error("An unexpected error occurred");
        }
    }
    
    /**
     * Get entity by ID
     * GET /api/entities/{id}
     * 
     * @param entityId the entity ID
     * @return ApiResponse with entity data or error if not found
     */
    public ApiResponse<FinancialEntityDTO> getEntity(Long entityId) {
        try {
            logger.debug("Fetching entity with ID: {}", entityId);
            
            // Service returns Optional<FinancialEntity>
            Optional<FinancialEntity> optionalEntity = entityService.getEntityById(entityId);
            
            if (!optionalEntity.isPresent()) {
                logger.warn("Entity not found with ID: {}", entityId);
                return ApiResponse.error("Entity not found with ID: " + entityId);
            }
            
            FinancialEntity entity = optionalEntity.get();
            FinancialEntityDTO dto = DTOMapper.toDTO(entity);
            
            return ApiResponse.success(dto);
            
        } catch (Exception e) {
            logger.error("Error fetching entity with ID: {}", entityId, e);
            return ApiResponse.error("Failed to retrieve entity");
        }
    }
    
    /**
     * Get all active entities
     * GET /api/entities
     * 
     * @return ApiResponse with list of active entities
     */
    public ApiResponse<List<FinancialEntityDTO>> getAllEntities() {
        try {
            logger.debug("Fetching all active entities");
            
            List<FinancialEntity> entities = entityService.getAllActiveEntities();
            List<FinancialEntityDTO> dtos = entities.stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} active entities", dtos.size());
            return ApiResponse.success(dtos)
                    .addMetadata("count", dtos.size());
                    
        } catch (Exception e) {
            logger.error("Error fetching all entities", e);
            return ApiResponse.error("Failed to retrieve entities");
        }
    }
    
    /**
     * Update entity information
     * PUT /api/entities/{id}
     * 
     * @param entityId the entity ID to update
     * @param dto updated entity data
     * @param request HTTP request for audit trail
     * @return ApiResponse with updated entity
     */
    public ApiResponse<FinancialEntityDTO> updateEntity(
            Long entityId,
            FinancialEntityDTO dto,
            HttpServletRequest request) {
        try {
            logger.info("Updating entity with ID: {}", entityId);
            
            String modifiedBy = getUserFromRequest(request);
            
            // Ensure ID matches
            dto.setEntityId(entityId);
            FinancialEntity entity = DTOMapper.toEntity(dto);
            
            // Service returns boolean indicating success
            boolean success = entityService.updateEntity(entity, modifiedBy);
            
            if (!success) {
                logger.warn("Failed to update entity with ID: {}", entityId);
                return ApiResponse.error("Failed to update entity with ID: " + entityId);
            }
            
            // Fetch the updated entity to return
            Optional<FinancialEntity> optionalUpdated = entityService.getEntityById(entityId);
            
            if (!optionalUpdated.isPresent()) {
                logger.error("Entity not found after update: {}", entityId);
                return ApiResponse.error("Entity not found after update");
            }
            
            FinancialEntity updated = optionalUpdated.get();
            FinancialEntityDTO responseDto = DTOMapper.toDTO(updated);
            
            logger.info("Successfully updated entity ID: {}", entityId);
            return ApiResponse.success(responseDto, "Entity updated successfully");
            
        } catch (ServiceException e) {
            logger.error("Service error updating entity: {}", e.getMessage());
            return ApiResponse.error("Failed to update entity: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating entity", e);
            return ApiResponse.error("An unexpected error occurred");
        }
    }
    
    /**
     * Deactivate entity (soft delete)
     * DELETE /api/entities/{id}
     * 
     * @param entityId the entity ID to deactivate
     * @param request HTTP request for audit trail
     * @return ApiResponse with success status
     */
    public ApiResponse<Boolean> deactivateEntity(
            Long entityId,
            HttpServletRequest request) {
        try {
            String deactivatedBy = getUserFromRequest(request);
            
            logger.info("Deactivating entity with ID: {}", entityId);
            
            boolean result = entityService.deactivateEntity(entityId, deactivatedBy);
            
            if (result) {
                logger.info("Successfully deactivated entity ID: {}", entityId);
                return ApiResponse.success(result, "Entity deactivated successfully");
            } else {
                logger.warn("Failed to deactivate entity ID: {}", entityId);
                return ApiResponse.error("Failed to deactivate entity");
            }
            
        } catch (ServiceException e) {
            logger.error("Service error deactivating entity: {}", e.getMessage());
            return ApiResponse.error("Failed to deactivate entity: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deactivating entity", e);
            return ApiResponse.error("An unexpected error occurred");
        }
    }
    
    // ========================================================================
    // QUERY OPERATIONS (3 endpoints)
    // ========================================================================
    
    /**
     * Get entities by type
     * GET /api/entities/type/{type}
     * 
     * @param type the entity type (BANK, INSURANCE, MSB, FINTECH, etc.)
     * @return ApiResponse with filtered entities
     */
    public ApiResponse<List<FinancialEntityDTO>> getEntitiesByType(String type) {
        try {
            EntityType entityType = EntityType.valueOf(type.toUpperCase());
            logger.debug("Fetching entities of type: {}", entityType);
            
            List<FinancialEntity> entities = entityService.getEntitiesByType(entityType);
            List<FinancialEntityDTO> dtos = entities.stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} entities of type {}", dtos.size(), entityType);
            return ApiResponse.success(dtos)
                    .addMetadata("type", entityType)
                    .addMetadata("count", dtos.size());
                    
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid entity type requested: {}", type);
            return ApiResponse.error("Invalid entity type: " + type);
        } catch (Exception e) {
            logger.error("Error fetching entities by type: {}", type, e);
            return ApiResponse.error("Failed to retrieve entities by type");
        }
    }
    
    /**
     * Search entities by name
     * GET /api/entities/search?q={query}
     * 
     * @param query search term (partial name match)
     * @return ApiResponse with search results
     */
    public ApiResponse<List<FinancialEntityDTO>> searchEntities(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query received");
                return ApiResponse.error("Search query cannot be empty");
            }
            
            logger.debug("Searching entities with query: {}", query);
            
            List<FinancialEntity> entities = entityService.searchEntitiesByName(query.trim());
            List<FinancialEntityDTO> dtos = entities.stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} entities matching query: {}", dtos.size(), query);
            return ApiResponse.success(dtos)
                    .addMetadata("query", query)
                    .addMetadata("count", dtos.size());
                    
        } catch (Exception e) {
            logger.error("Error searching entities", e);
            return ApiResponse.error("Failed to search entities");
        }
    }
    
    /**
     * Get total entity count
     * GET /api/entities/count
     * 
     * @return ApiResponse with total count
     */
    public ApiResponse<Long> getTotalCount() {
        try {
            logger.debug("Fetching total entity count");
            
            long count = entityService.getTotalEntityCount();
            
            logger.debug("Total entity count: {}", count);
            return ApiResponse.success(count);
            
        } catch (Exception e) {
            logger.error("Error getting total count", e);
            return ApiResponse.error("Failed to retrieve entity count");
        }
    }
    
    // ========================================================================
    // COMPLIANCE OPERATIONS (3 endpoints)
    // ========================================================================
    
    /**
     * Update compliance status
     * PUT /api/entities/{id}/compliance-status
     * 
     * @param entityId the entity ID
     * @param status new compliance status
     * @param reason reason for status change
     * @param request HTTP request for audit trail
     * @return ApiResponse with success status
     */
    public ApiResponse<Boolean> updateComplianceStatus(
            Long entityId,
            String status,
            String reason,
            HttpServletRequest request) {
        try {
            ComplianceStatus complianceStatus = ComplianceStatus.valueOf(status.toUpperCase());
            String updatedBy = getUserFromRequest(request);
            
            logger.info("Updating compliance status for entity {}: {} (Reason: {})", 
                    entityId, complianceStatus, reason);
            
            boolean result = entityService.updateComplianceStatus(
                    entityId, complianceStatus, updatedBy, reason);
            
            if (result) {
                logger.info("Successfully updated compliance status for entity {}", entityId);
                return ApiResponse.success(result, "Compliance status updated successfully");
            } else {
                logger.warn("Failed to update compliance status for entity {}", entityId);
                return ApiResponse.error("Failed to update compliance status");
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid compliance status: {}", status);
            return ApiResponse.error("Invalid compliance status: " + status);
        } catch (ServiceException e) {
            logger.error("Service error updating compliance status: {}", e.getMessage());
            return ApiResponse.error("Failed to update compliance status: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating compliance status", e);
            return ApiResponse.error("An unexpected error occurred");
        }
    }
    
    /**
     * Update risk level
     * PUT /api/entities/{id}/risk-level
     * 
     * @param entityId the entity ID
     * @param level new risk level
     * @param reason reason for risk level change
     * @param request HTTP request for audit trail
     * @return ApiResponse with success status
     */
    public ApiResponse<Boolean> updateRiskLevel(
            Long entityId,
            String level,
            String reason,
            HttpServletRequest request) {
        try {
            RiskLevel riskLevel = RiskLevel.valueOf(level.toUpperCase());
            String updatedBy = getUserFromRequest(request);
            
            logger.info("Updating risk level for entity {}: {} (Reason: {})", 
                    entityId, riskLevel, reason);
            
            boolean result = entityService.updateRiskLevel(
                    entityId, riskLevel, updatedBy, reason);
            
            if (result) {
                logger.info("Successfully updated risk level for entity {}", entityId);
                return ApiResponse.success(result, "Risk level updated successfully");
            } else {
                logger.warn("Failed to update risk level for entity {}", entityId);
                return ApiResponse.error("Failed to update risk level");
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid risk level: {}", level);
            return ApiResponse.error("Invalid risk level: " + level);
        } catch (ServiceException e) {
            logger.error("Service error updating risk level: {}", e.getMessage());
            return ApiResponse.error("Failed to update risk level: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating risk level", e);
            return ApiResponse.error("An unexpected error occurred");
        }
    }
    
    /**
     * Conduct compliance review
     * POST /api/entities/{id}/review
     * 
     * @param entityId the entity ID
     * @param status new compliance status after review
     * @param level new risk level after review
     * @param notes review notes
     * @param request HTTP request for audit trail
     * @return ApiResponse with success status
     */
    public ApiResponse<Boolean> conductReview(
            Long entityId,
            String status,
            String level,
            String notes,
            HttpServletRequest request) {
        try {
            ComplianceStatus complianceStatus = ComplianceStatus.valueOf(status.toUpperCase());
            RiskLevel riskLevel = RiskLevel.valueOf(level.toUpperCase());
            String reviewedBy = getUserFromRequest(request);
            
            logger.info("Conducting review for entity {}", entityId);
            
            boolean result = entityService.conductComplianceReview(
                    entityId, reviewedBy, complianceStatus, riskLevel, notes);
            
            if (result) {
                logger.info("Successfully completed review for entity {}", entityId);
                return ApiResponse.success(result, "Compliance review completed successfully");
            } else {
                logger.warn("Failed to complete review for entity {}", entityId);
                return ApiResponse.error("Failed to conduct review");
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status or risk level parameters");
            return ApiResponse.error("Invalid status or risk level parameters");
        } catch (ServiceException e) {
            logger.error("Service error conducting review: {}", e.getMessage());
            return ApiResponse.error("Failed to conduct review: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error conducting review", e);
            return ApiResponse.error("An unexpected error occurred");
        }
    }
    
    // ========================================================================
    // LICENSE MANAGEMENT (1 endpoint)
    // ========================================================================
    
    /**
     * Renew entity license
     * POST /api/entities/{id}/renew-license
     * 
     * @param entityId the entity ID
     * @param newExpiryDate new license expiry date (ISO format: YYYY-MM-DD)
     * @param request HTTP request for audit trail
     * @return ApiResponse with success status
     */
    public ApiResponse<Boolean> renewLicense(
            Long entityId,
            String newExpiryDate,
            HttpServletRequest request) {
        try {
            LocalDate expiryDate = LocalDate.parse(newExpiryDate);
            String renewedBy = getUserFromRequest(request);
            
            logger.info("Renewing license for entity {} until {}", entityId, expiryDate);
            
            boolean result = entityService.renewLicense(entityId, expiryDate, renewedBy);
            
            if (result) {
                logger.info("Successfully renewed license for entity {}", entityId);
                return ApiResponse.success(result, "License renewed successfully")
                        .addMetadata("newExpiryDate", expiryDate);
            } else {
                logger.warn("Failed to renew license for entity {}", entityId);
                return ApiResponse.error("Failed to renew license");
            }
            
        } catch (Exception e) {
            logger.error("Error renewing license", e);
            return ApiResponse.error("Failed to renew license: " + e.getMessage());
        }
    }
    
    // ========================================================================
    // ALERT ENDPOINTS (4 endpoints)
    // ========================================================================
    
    /**
     * Get entities with expiring licenses
     * GET /api/entities/alerts/expiring-licenses?days={days}
     * 
     * @param days number of days to look ahead (default: 60)
     * @return ApiResponse with list of entities
     */
    public ApiResponse<List<FinancialEntityDTO>> getExpiringLicenses(Integer days) {
        try {
            int lookAheadDays = (days != null) ? days : 60;
            logger.debug("Fetching entities with licenses expiring in {} days", lookAheadDays);
            
            List<FinancialEntity> entities = entityService.getEntitiesWithExpiringLicenses(lookAheadDays);
            List<FinancialEntityDTO> dtos = entities.stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} entities with expiring licenses", dtos.size());
            return ApiResponse.success(dtos)
                    .addMetadata("days", lookAheadDays)
                    .addMetadata("count", dtos.size());
                    
        } catch (Exception e) {
            logger.error("Error fetching expiring licenses", e);
            return ApiResponse.error("Failed to retrieve expiring licenses");
        }
    }
    
    /**
     * Get entities with overdue reviews
     * GET /api/entities/alerts/overdue-reviews
     * 
     * @return ApiResponse with list of entities
     */
    public ApiResponse<List<FinancialEntityDTO>> getOverdueReviews() {
        try {
            logger.debug("Fetching entities with overdue reviews");
            
            List<FinancialEntity> entities = entityService.getEntitiesWithOverdueReviews();
            List<FinancialEntityDTO> dtos = entities.stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} entities with overdue reviews", dtos.size());
            return ApiResponse.success(dtos)
                    .addMetadata("count", dtos.size());
                    
        } catch (Exception e) {
            logger.error("Error fetching overdue reviews", e);
            return ApiResponse.error("Failed to retrieve overdue reviews");
        }
    }
    
    /**
     * Get non-compliant entities
     * GET /api/entities/alerts/non-compliant
     * 
     * @return ApiResponse with list of entities
     */
    public ApiResponse<List<FinancialEntityDTO>> getNonCompliant() {
        try {
            logger.debug("Fetching non-compliant entities");
            
            List<FinancialEntity> entities = entityService.getNonCompliantEntities();
            List<FinancialEntityDTO> dtos = entities.stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} non-compliant entities", dtos.size());
            return ApiResponse.success(dtos)
                    .addMetadata("count", dtos.size());
                    
        } catch (Exception e) {
            logger.error("Error fetching non-compliant entities", e);
            return ApiResponse.error("Failed to retrieve non-compliant entities");
        }
    }
    
    /**
     * Get high risk entities
     * GET /api/entities/alerts/high-risk
     * 
     * @return ApiResponse with list of entities
     */
    public ApiResponse<List<FinancialEntityDTO>> getHighRisk() {
        try {
            logger.debug("Fetching high risk entities");
            
            List<FinancialEntity> entities = entityService.getHighRiskEntities();
            List<FinancialEntityDTO> dtos = entities.stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} high risk entities", dtos.size());
            return ApiResponse.success(dtos)
                    .addMetadata("count", dtos.size());
                    
        } catch (Exception e) {
            logger.error("Error fetching high risk entities", e);
            return ApiResponse.error("Failed to retrieve high risk entities");
        }
    }
    
    // ========================================================================
    // REPORTING (2 endpoints)
    // ========================================================================
    
    /**
     * Get compliance summary
     * GET /api/entities/reports/compliance-summary
     * 
     * @return ApiResponse with summary statistics
     */
    public ApiResponse<ComplianceSummary> getComplianceSummary() {
        try {
            logger.debug("Generating compliance summary");
            
            ComplianceSummary summary = entityService.getComplianceSummary();
            
            logger.debug("Generated compliance summary with {} total entities", 
                    summary.getTotalEntities());
            return ApiResponse.success(summary);
            
        } catch (Exception e) {
            logger.error("Error generating compliance summary", e);
            return ApiResponse.error("Failed to generate compliance summary");
        }
    }
    
    /**
     * Validate entity data
     * POST /api/entities/validate
     * 
     * @param dto entity data to validate
     * @return ApiResponse with validation result
     */
    public ApiResponse<ValidationResult> validateEntity(FinancialEntityDTO dto) {
        try {
            logger.debug("Validating entity data for: {}", dto.getEntityName());
            
            FinancialEntity entity = DTOMapper.toEntity(dto);
            ValidationResult result = entityService.validateEntityRegistration(entity);
            
            if (result.isValid()) {
                logger.debug("Validation passed for entity: {}", dto.getEntityName());
                return ApiResponse.success(result, "Validation passed");
            } else {
                logger.debug("Validation failed for entity: {} ({} errors)", 
                        dto.getEntityName(), result.getErrors().size());
                return ApiResponse.success(result, "Validation failed")
                        .addMetadata("errorCount", result.getErrors().size());
            }
            
        } catch (Exception e) {
            logger.error("Error validating entity", e);
            return ApiResponse.error("Failed to validate entity");
        }
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    /**
     * Helper method to extract username from HTTP request
     * In production, this would extract from security context / JWT / session
     * 
     * @param request HTTP request
     * @return username for audit trail
     */
    private String getUserFromRequest(HttpServletRequest request) {
        // In production implementation:
        // - Extract from Spring Security context
        // - Parse JWT token
        // - Get from HTTP session
        // - Use OAuth/OIDC user info
        
        // For now, return system user
        return "SYSTEM_USER";
    }
}