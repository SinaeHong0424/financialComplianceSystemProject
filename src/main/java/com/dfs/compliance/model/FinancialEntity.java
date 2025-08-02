package com.dfs.compliance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class representing a financial institution in the DFS compliance system.
 * Maps to the FINANCIAL_ENTITIES table in Oracle database.
 * 
 * @author DFS Technology Bureau
 * @version 1.0
 * @since 2025-08-03
 */
public class FinancialEntity {
    
    // Entity Type Enum
    public enum EntityType {
        BANK, INSURANCE, MSB, FINTECH, CREDIT_UNION, BROKER_DEALER
    }
    
    // Compliance Status Enum
    public enum ComplianceStatus {
        COMPLIANT, NON_COMPLIANT, PENDING_REVIEW, UNDER_INVESTIGATION, PROBATION, SUSPENDED
    }
    
    // Risk Level Enum
    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    // Fields matching database schema (financial_entities table)
    private Long entityId;
    private String entityName;
    private EntityType entityType;
    private String nmlsId;
    private String dbaName;
    private String primaryContact;
    private String contactEmail;
    private String contactPhone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private LocalDate registrationDate;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private ComplianceStatus complianceStatus;
    private RiskLevel riskLevel;
    private LocalDate lastReviewDate;
    private LocalDate nextReviewDate;
    private BigDecimal totalAssets;
    private Integer employeeCount;
    private boolean isActive;
    private String notes;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    
    // Constructors
    public FinancialEntity() {
        this.isActive = true; // Default value
        this.state = "NY"; // Default value
    }
    
    // Getters and Setters
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    
    public String getNmlsId() {
        return nmlsId;
    }
    
    public void setNmlsId(String nmlsId) {
        this.nmlsId = nmlsId;
    }
    
    public String getDbaName() {
        return dbaName;
    }
    
    public void setDbaName(String dbaName) {
        this.dbaName = dbaName;
    }
    
    public String getPrimaryContact() {
        return primaryContact;
    }
    
    public void setPrimaryContact(String primaryContact) {
        this.primaryContact = primaryContact;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public String getAddressLine1() {
        return addressLine1;
    }
    
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }
    
    public String getAddressLine2() {
        return addressLine2;
    }
    
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public LocalDate getLicenseExpiry() {
        return licenseExpiry;
    }
    
    public void setLicenseExpiry(LocalDate licenseExpiry) {
        this.licenseExpiry = licenseExpiry;
    }
    
    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }
    
    public void setComplianceStatus(ComplianceStatus complianceStatus) {
        this.complianceStatus = complianceStatus;
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public LocalDate getLastReviewDate() {
        return lastReviewDate;
    }
    
    public void setLastReviewDate(LocalDate lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
    }
    
    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }
    
    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }
    
    public BigDecimal getTotalAssets() {
        return totalAssets;
    }
    
    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }
    
    public Integer getEmployeeCount() {
        return employeeCount;
    }
    
    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }
    
    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    public String getModifiedBy() {
        return modifiedBy;
    }
    
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    
    @Override
    public String toString() {
        return "FinancialEntity{" +
                "entityId=" + entityId +
                ", entityName='" + entityName + '\'' +
                ", entityType=" + entityType +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", complianceStatus=" + complianceStatus +
                ", riskLevel=" + riskLevel +
                ", isActive=" + isActive +
                '}';
    }
}