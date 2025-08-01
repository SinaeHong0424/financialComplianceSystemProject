package com.dfs.compliance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a compliance violation.
 * 
 * This class corresponds to the COMPLIANCE_VIOLATIONS table in the database.
 * It tracks regulatory violations, fines, and resolution status.
 */
public class ComplianceViolation {
    
    // Primary Key
    private Long violationId;
    
    // Foreign Key
    private Long entityId;
    
    // Violation Information
    private String violationType;
    private String violationCode;
    private String description;
    private Severity severity;
    
    // Dates
    private LocalDate violationDate;
    private LocalDate discoveryDate;
    private String reportedBy;
    
    // Financial Information
    private BigDecimal fineAmount;
    private boolean finePaid;
    private LocalDate paymentDueDate;
    private LocalDate paymentDate;
    
    // Status Information
    private ViolationStatus status;
    private LocalDate resolutionDate;
    private String resolutionNotes;
    private String correctiveAction;
    
    // Follow-up
    private boolean followUpRequired;
    private LocalDate followUpDate;
    
    // Audit Columns
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    
    /**
     * Violation severity levels.
     */
    public enum Severity {
        LOW("Low", 1),
        MEDIUM("Medium", 2),
        HIGH("High", 3),
        CRITICAL("Critical", 4);
        
        private final String displayName;
        private final int level;
        
        Severity(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        /**
         * Checks if this severity is higher than another.
         * 
         * @param other severity to compare
         * @return true if this is more severe
         */
        public boolean isMoreSevereThan(Severity other) {
            return this.level > other.level;
        }
    }
    
    /**
     * Violation status values.
     */
    public enum ViolationStatus {
        UNDER_REVIEW("Under Review"),
        CONFIRMED("Confirmed"),
        RESOLVED("Resolved"),
        APPEALED("Appealed"),
        DISMISSED("Dismissed");
        
        private final String displayName;
        
        ViolationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Checks if violation is in active state.
         * 
         * @return true if active (not resolved or dismissed)
         */
        public boolean isActive() {
            return this != RESOLVED && this != DISMISSED;
        }
    }
    
    // Constructors
    
    /**
     * Default constructor.
     */
    public ComplianceViolation() {
        this.violationDate = LocalDate.now();
        this.discoveryDate = LocalDate.now();
        this.status = ViolationStatus.UNDER_REVIEW;
        this.fineAmount = BigDecimal.ZERO;
        this.finePaid = false;
        this.followUpRequired = true;
    }
    
    /**
     * Constructor with required fields.
     * 
     * @param entityId entity that violated
     * @param violationType type of violation
     * @param description violation description
     * @param severity severity level
     */
    public ComplianceViolation(Long entityId, String violationType, 
                               String description, Severity severity) {
        this();
        this.entityId = entityId;
        this.violationType = violationType;
        this.description = description;
        this.severity = severity;
    }
    
    // Business Logic Methods
    
    /**
     * Checks if the fine payment is overdue.
     * 
     * @return true if payment is overdue
     */
    public boolean isFineOverdue() {
        if (finePaid || paymentDueDate == null) {
            return false;
        }
        return paymentDueDate.isBefore(LocalDate.now());
    }
    
    /**
     * Calculates days since violation occurred.
     * 
     * @return number of days since violation
     */
    public long getDaysSinceViolation() {
        return java.time.temporal.ChronoUnit.DAYS.between(violationDate, LocalDate.now());
    }
    
    /**
     * Calculates days until payment due.
     * 
     * @return days until payment due, negative if overdue
     */
    public long getDaysUntilPaymentDue() {
        if (paymentDueDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), paymentDueDate);
    }
    
    /**
     * Checks if violation is resolved.
     * 
     * @return true if resolved
     */
    public boolean isResolved() {
        return status == ViolationStatus.RESOLVED && resolutionDate != null;
    }
    
    /**
     * Checks if violation is active (unresolved).
     * 
     * @return true if active
     */
    public boolean isActive() {
        return status.isActive();
    }
    
    /**
     * Checks if follow-up is overdue.
     * 
     * @return true if follow-up is overdue
     */
    public boolean isFollowUpOverdue() {
        if (!followUpRequired || followUpDate == null) {
            return false;
        }
        return followUpDate.isBefore(LocalDate.now()) && isActive();
    }
    
    /**
     * Checks if violation requires immediate attention.
     * 
     * @return true if requires attention
     */
    public boolean requiresAttention() {
        return severity == Severity.CRITICAL
            || isFineOverdue()
            || isFollowUpOverdue()
            || (status == ViolationStatus.UNDER_REVIEW && getDaysSinceViolation() > 60);
    }
    
    /**
     * Marks violation as resolved.
     * 
     * @param resolutionNotes notes about resolution
     */
    public void markResolved(String resolutionNotes) {
        this.status = ViolationStatus.RESOLVED;
        this.resolutionDate = LocalDate.now();
        this.resolutionNotes = resolutionNotes;
        this.followUpRequired = false;
    }
    
    /**
     * Records fine payment.
     * 
     * @param paymentDate date payment was made
     */
    public void recordPayment(LocalDate paymentDate) {
        this.finePaid = true;
        this.paymentDate = paymentDate;
    }
    
    // Getters and Setters
    
    public Long getViolationId() {
        return violationId;
    }
    
    public void setViolationId(Long violationId) {
        this.violationId = violationId;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getViolationType() {
        return violationType;
    }
    
    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }
    
    public String getViolationCode() {
        return violationCode;
    }
    
    public void setViolationCode(String violationCode) {
        this.violationCode = violationCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public LocalDate getViolationDate() {
        return violationDate;
    }
    
    public void setViolationDate(LocalDate violationDate) {
        this.violationDate = violationDate;
    }
    
    public LocalDate getDiscoveryDate() {
        return discoveryDate;
    }
    
    public void setDiscoveryDate(LocalDate discoveryDate) {
        this.discoveryDate = discoveryDate;
    }
    
    public String getReportedBy() {
        return reportedBy;
    }
    
    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }
    
    public BigDecimal getFineAmount() {
        return fineAmount;
    }
    
    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }
    
    public boolean isFinePaid() {
        return finePaid;
    }
    
    public void setFinePaid(boolean finePaid) {
        this.finePaid = finePaid;
    }
    
    public LocalDate getPaymentDueDate() {
        return paymentDueDate;
    }
    
    public void setPaymentDueDate(LocalDate paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }
    
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public ViolationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ViolationStatus status) {
        this.status = status;
    }
    
    public LocalDate getResolutionDate() {
        return resolutionDate;
    }
    
    public void setResolutionDate(LocalDate resolutionDate) {
        this.resolutionDate = resolutionDate;
    }
    
    public String getResolutionNotes() {
        return resolutionNotes;
    }
    
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
    
    public String getCorrectiveAction() {
        return correctiveAction;
    }
    
    public void setCorrectiveAction(String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }
    
    public boolean isFollowUpRequired() {
        return followUpRequired;
    }
    
    public void setFollowUpRequired(boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
    }
    
    public LocalDate getFollowUpDate() {
        return followUpDate;
    }
    
    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
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
        ComplianceViolation that = (ComplianceViolation) o;
        return Objects.equals(violationId, that.violationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(violationId);
    }
    
    @Override
    public String toString() {
        return "ComplianceViolation{" +
                "violationId=" + violationId +
                ", entityId=" + entityId +
                ", violationType='" + violationType + '\'' +
                ", severity=" + severity +
                ", status=" + status +
                ", fineAmount=" + fineAmount +
                ", finePaid=" + finePaid +
                '}';
    }
}