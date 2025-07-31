package com.dfs.compliance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a financial institution regulated by DFS.
 * 
 * This class corresponds to the FINANCIAL_ENTITIES table in the database.
 * It includes validation, business logic, and proper encapsulation.
 * 
 */
public class FinancialEntity {
    
    // Primary Key
    private Long entityId;
    
    // Basic Information
    private String entityName;
    private EntityType entityType;
    private String nmlsId;
    private String dbaName;
    
    // Contact Information
    private String primaryContact;
    private String contactEmail;
    private String contactPhone;
    
    // Address Information
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    
    // Licensing Information
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private LocalDate registrationDate;
    
    // Compliance Information
    private ComplianceStatus complianceStatus;
    private RiskLevel riskLevel;
    private LocalDate lastReviewDate;
    private LocalDate nextReviewDate;
    
    // Financial Information
    private BigDecimal totalAssets;
    private Integer employeeCount;
    
    // Status
    private boolean isActive;
    
    // Additional Information
    private String notes;
    
    // Audit Columns
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    
    /**
     * Entity types supported by DFS.
     */
    public enum EntityType {
        BANK("Bank"),
        INSURANCE("Insurance Company"),
        MSB("Money Service Business"),
        FINTECH("FinTech Company"),
        CREDIT_UNION("Credit Union"),
        BROKER_DEALER("Broker-Dealer");
        
        private final String displayName;
        
        EntityType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Compliance status values.
     */
    public enum ComplianceStatus {
        COMPLIANT("Compliant"),
        NON_COMPLIANT("Non-Compliant"),
        PENDING_REVIEW("Pending Review"),
        UNDER_INVESTIGATION("Under Investigation"),
        PROBATION("Probation"),
        SUSPENDED("Suspended");
        
        private final String displayName;
        
        ComplianceStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Risk level classifications.
     */
    public enum RiskLevel {
        LOW("Low Risk"),
        MEDIUM("Medium Risk"),
        HIGH("High Risk"),
        CRITICAL("Critical Risk");
        
        private final String displayName;
        
        RiskLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    
    /**
     * Default constructor.
     */
    public FinancialEntity() {
        this.isActive = true;
        this.state = "NY";
        this.complianceStatus = ComplianceStatus.PENDING_REVIEW;
        this.riskLevel = RiskLevel.MEDIUM;
    }
    
    /**
     * Constructor with required fields.
     * 
     * @param entityName name of the financial entity
     * @param entityType type of entity
     */
    public FinancialEntity(String entityName, EntityType entityType) {
        this();
        this.entityName = entityName;
        this.entityType = entityType;
        this.registrationDate = LocalDate.now();
    }
    
    // Business Logic Methods
    
    /**
     * Checks if the entity's license is expired.
     * 
     * @return true if license is expired, false otherwise
     */
    public boolean isLicenseExpired() {
        if (licenseExpiry == null) {
            return false;
        }
        return licenseExpiry.isBefore(LocalDate.now());
    }
    
    /**
     * Checks if the entity's license expires within specified days.
     * 
     * @param days number of days to check
     * @return true if license expires within specified days
     */
    public boolean isLicenseExpiringSoon(int days) {
        if (licenseExpiry == null) {
            return false;
        }
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return licenseExpiry.isBefore(futureDate) && !isLicenseExpired();
    }
    
    /**
     * Checks if entity review is overdue.
     * 
     * @return true if review is overdue
     */
    public boolean isReviewOverdue() {
        if (nextReviewDate == null) {
            return false;
        }
        return nextReviewDate.isBefore(LocalDate.now());
    }
    
    /**
     * Checks if entity is in good standing (compliant and active).
     * 
     * @return true if in good standing
     */
    public boolean isInGoodStanding() {
        return isActive 
            && complianceStatus == ComplianceStatus.COMPLIANT
            && !isLicenseExpired()
            && !isReviewOverdue();
    }
    
    /**
     * Checks if entity requires immediate attention.
     * 
     * @return true if requires attention
     */
    public boolean requiresAttention() {
        return riskLevel == RiskLevel.CRITICAL
            || complianceStatus == ComplianceStatus.SUSPENDED
            || complianceStatus == ComplianceStatus.UNDER_INVESTIGATION
            || isLicenseExpired()
            || isReviewOverdue();
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
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
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
    
    // Object Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinancialEntity that = (FinancialEntity) o;
        return Objects.equals(entityId, that.entityId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(entityId);
    }
    
    @Override
    public String toString() {
        return "FinancialEntity{" +
                "entityId=" + entityId +
                ", entityName='" + entityName + '\'' +
                ", entityType=" + entityType +
                ", complianceStatus=" + complianceStatus +
                ", riskLevel=" + riskLevel +
                ", isActive=" + isActive +
                '}';
    }
}