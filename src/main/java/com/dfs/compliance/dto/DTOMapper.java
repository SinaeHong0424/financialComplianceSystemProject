package com.dfs.compliance.dto;

import com.dfs.compliance.model.FinancialEntity;

/**
 * Mapper utility to convert between FinancialEntity and FinancialEntityDTO
 * Provides bidirectional conversion for API layer
 */
public class DTOMapper {
    
    /**
     * Converts FinancialEntity domain model to DTO for API response
     * 
     * @param entity the domain entity
     * @return DTO representation
     */
    public static FinancialEntityDTO toDTO(FinancialEntity entity) {
        if (entity == null) {
            return null;
        }
        
        FinancialEntityDTO dto = new FinancialEntityDTO();
        dto.setEntityId(entity.getEntityId());
        dto.setEntityName(entity.getEntityName());
        dto.setEntityType(entity.getEntityType());
        dto.setNmlsId(entity.getNmlsId());
        dto.setDbaName(entity.getDbaName());
        dto.setLicenseNumber(entity.getLicenseNumber());
        dto.setLicenseExpiry(entity.getLicenseExpiry());
        dto.setRegistrationDate(entity.getRegistrationDate());
        dto.setPrimaryContact(entity.getPrimaryContact());
        dto.setContactEmail(entity.getContactEmail());
        dto.setContactPhone(entity.getContactPhone());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setZipCode(entity.getZipCode());
        dto.setComplianceStatus(entity.getComplianceStatus());
        dto.setRiskLevel(entity.getRiskLevel());
        dto.setLastReviewDate(entity.getLastReviewDate());
        dto.setNextReviewDate(entity.getNextReviewDate());
        dto.setTotalAssets(entity.getTotalAssets());
        dto.setEmployeeCount(entity.getEmployeeCount());
        dto.setActive(entity.isActive());
        dto.setNotes(entity.getNotes());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedDate(entity.getModifiedDate());
        dto.setModifiedBy(entity.getModifiedBy());
        
        return dto;
    }
    
    /**
     * Converts DTO from API request to FinancialEntity domain model
     * 
     * @param dto the DTO from API
     * @return domain entity
     */
    public static FinancialEntity toEntity(FinancialEntityDTO dto) {
        if (dto == null) {
            return null;
        }
        
        FinancialEntity entity = new FinancialEntity();
        entity.setEntityId(dto.getEntityId());
        entity.setEntityName(dto.getEntityName());
        entity.setEntityType(dto.getEntityType());
        entity.setNmlsId(dto.getNmlsId());
        entity.setDbaName(dto.getDbaName());
        entity.setLicenseNumber(dto.getLicenseNumber());
        entity.setLicenseExpiry(dto.getLicenseExpiry());
        entity.setRegistrationDate(dto.getRegistrationDate());
        entity.setPrimaryContact(dto.getPrimaryContact());
        entity.setContactEmail(dto.getContactEmail());
        entity.setContactPhone(dto.getContactPhone());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setZipCode(dto.getZipCode());
        entity.setComplianceStatus(dto.getComplianceStatus());
        entity.setRiskLevel(dto.getRiskLevel());
        entity.setLastReviewDate(dto.getLastReviewDate());
        entity.setNextReviewDate(dto.getNextReviewDate());
        entity.setTotalAssets(dto.getTotalAssets());
        entity.setEmployeeCount(dto.getEmployeeCount());
        entity.setActive(dto.isActive());
        entity.setNotes(dto.getNotes());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedDate(dto.getModifiedDate());
        entity.setModifiedBy(dto.getModifiedBy());
        
        return entity;
    }
}