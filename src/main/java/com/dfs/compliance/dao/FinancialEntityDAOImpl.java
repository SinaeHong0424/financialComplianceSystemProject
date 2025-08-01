package com.dfs.compliance.dao;

import com.dfs.compliance.model.FinancialEntity;
import com.dfs.compliance.model.FinancialEntity.ComplianceStatus;
import com.dfs.compliance.model.FinancialEntity.EntityType;
import com.dfs.compliance.model.FinancialEntity.RiskLevel;
import com.dfs.compliance.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of FinancialEntityDAO.
 * 
 * <p>Uses prepared statements for all queries to prevent SQL injection.
 * Implements connection pooling via DatabaseConnection utility.
 */
public class FinancialEntityDAOImpl implements FinancialEntityDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialEntityDAOImpl.class);
    
    // SQL Queries
    private static final String INSERT_ENTITY = 
        "INSERT INTO financial_entities (" +
        "entity_id, entity_name, entity_type, nmls_id, dba_name, " +
        "primary_contact, contact_email, contact_phone, " +
        "address_line1, address_line2, city, state, zip_code, " +
        "license_number, license_expiry, registration_date, " +
        "compliance_status, risk_level, last_review_date, next_review_date, " +
        "total_assets, employee_count, is_active, notes, " +
        "created_date, created_by" +
        ") VALUES (" +
        "entity_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, USER" +
        ")";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM financial_entities WHERE entity_id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM financial_entities WHERE is_active = 'Y' ORDER BY entity_name";
    
    private static final String SELECT_BY_TYPE = 
        "SELECT * FROM financial_entities WHERE entity_type = ? AND is_active = 'Y' ORDER BY entity_name";
    
    private static final String SELECT_BY_COMPLIANCE_STATUS = 
        "SELECT * FROM financial_entities WHERE compliance_status = ? AND is_active = 'Y' ORDER BY entity_name";
    
    private static final String SELECT_BY_RISK_LEVEL = 
        "SELECT * FROM financial_entities WHERE risk_level = ? AND is_active = 'Y' ORDER BY entity_name";
    
    private static final String SELECT_EXPIRING_LICENSES = 
        "SELECT * FROM financial_entities " +
        "WHERE license_expiry BETWEEN SYSDATE AND SYSDATE + ? " +
        "AND is_active = 'Y' " +
        "ORDER BY license_expiry";
    
    private static final String SELECT_OVERDUE_REVIEWS = 
        "SELECT * FROM financial_entities " +
        "WHERE next_review_date < SYSDATE " +
        "AND is_active = 'Y' " +
        "ORDER BY next_review_date";
    
    private static final String SEARCH_BY_NAME = 
        "SELECT * FROM financial_entities " +
        "WHERE UPPER(entity_name) LIKE UPPER(?) " +
        "AND is_active = 'Y' " +
        "ORDER BY entity_name";
    
    private static final String UPDATE_ENTITY = 
        "UPDATE financial_entities SET " +
        "entity_name = ?, entity_type = ?, nmls_id = ?, dba_name = ?, " +
        "primary_contact = ?, contact_email = ?, contact_phone = ?, " +
        "address_line1 = ?, address_line2 = ?, city = ?, state = ?, zip_code = ?, " +
        "license_number = ?, license_expiry = ?, " +
        "compliance_status = ?, risk_level = ?, " +
        "last_review_date = ?, next_review_date = ?, " +
        "total_assets = ?, employee_count = ?, notes = ?, " +
        "modified_date = SYSDATE, modified_by = USER " +
        "WHERE entity_id = ?";
    
    private static final String SOFT_DELETE = 
        "UPDATE financial_entities SET is_active = 'N', modified_date = SYSDATE, modified_by = USER " +
        "WHERE entity_id = ?";
    
    private static final String COUNT_ALL = 
        "SELECT COUNT(*) FROM financial_entities WHERE is_active = 'Y'";
    
    private static final String COUNT_BY_TYPE = 
        "SELECT COUNT(*) FROM financial_entities WHERE entity_type = ? AND is_active = 'Y'";
    
    @Override
    public FinancialEntity create(FinancialEntity entity) throws SQLException {
        logger.debug("Creating new financial entity: {}", entity.getEntityName());
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_ENTITY, 
                     new String[]{"entity_id"})) {
            
            setEntityParameters(pstmt, entity, false);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("Creating entity failed, no rows affected");
            }
            
            // Get generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setEntityId(generatedKeys.getLong(1));
                    logger.info("Created entity with ID: {}", entity.getEntityId());
                } else {
                    throw new SQLException("Creating entity failed, no ID obtained");
                }
            }
            
            return entity;
            
        } catch (Exception e) {
            logger.error("Error creating entity: {}", e.getMessage());
            throw new SQLException("Error creating entity", e);
        }
    }
    
    @Override
    public Optional<FinancialEntity> findById(Long entityId) throws SQLException {
        logger.debug("Finding entity by ID: {}", entityId);
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            pstmt.setLong(1, entityId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    FinancialEntity entity = mapResultSetToEntity(rs);
                    logger.debug("Found entity: {}", entity.getEntityName());
                    return Optional.of(entity);
                }
            }
            
            logger.debug("Entity not found with ID: {}", entityId);
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error finding entity by ID: {}", e.getMessage());
            throw new SQLException("Error finding entity", e);
        }
    }
    
    @Override
    public List<FinancialEntity> findAll() throws SQLException {
        logger.debug("Finding all active entities");
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            
            List<FinancialEntity> entities = new ArrayList<>();
            
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
            
            logger.info("Found {} active entities", entities.size());
            return entities;
            
        } catch (Exception e) {
            logger.error("Error finding all entities: {}", e.getMessage());
            throw new SQLException("Error finding entities", e);
        }
    }
    
    @Override
    public List<FinancialEntity> findByType(EntityType entityType) throws SQLException {
        logger.debug("Finding entities by type: {}", entityType);
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_TYPE)) {
            
            pstmt.setString(1, entityType.name());
            
            List<FinancialEntity> entities = new ArrayList<>();
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapResultSetToEntity(rs));
                }
            }
            
            logger.info("Found {} entities of type {}", entities.size(), entityType);
            return entities;
            
        } catch (Exception e) {
            logger.error("Error finding entities by type: {}", e.getMessage());
            throw new SQLException("Error finding entities by type", e);
        }
    }
    
    @Override
    public List<FinancialEntity> findByComplianceStatus(ComplianceStatus status) throws SQLException {
        logger.debug("Finding entities by compliance status: {}", status);
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_COMPLIANCE_STATUS)) {
            
            pstmt.setString(1, status.name());
            
            List<FinancialEntity> entities = new ArrayList<>();
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapResultSetToEntity(rs));
                }
            }
            
            logger.info("Found {} entities with status {}", entities.size(), status);
            return entities;
            
        } catch (Exception e) {
            logger.error("Error finding entities by status: {}", e.getMessage());
            throw new SQLException("Error finding entities by status", e);
        }
    }
    
    @Override
    public List<FinancialEntity> findByRiskLevel(RiskLevel riskLevel) throws SQLException {
        logger.debug("Finding entities by risk level: {}", riskLevel);
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_RISK_LEVEL)) {
            
            pstmt.setString(1, riskLevel.name());
            
            List<FinancialEntity> entities = new ArrayList<>();
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapResultSetToEntity(rs));
                }
            }
            
            logger.info("Found {} entities with risk level {}", entities.size(), riskLevel);
            return entities;
            
        } catch (Exception e) {
            logger.error("Error finding entities by risk level: {}", e.getMessage());
            throw new SQLException("Error finding entities by risk level", e);
        }
    }
    
    // Helper method to set entity parameters in PreparedStatement
    private void setEntityParameters(PreparedStatement pstmt, FinancialEntity entity, 
                                     boolean includeId) throws SQLException {
        int paramIndex = 1;
        
        pstmt.setString(paramIndex++, entity.getEntityName());
        pstmt.setString(paramIndex++, entity.getEntityType().name());
        pstmt.setString(paramIndex++, entity.getNmlsId());
        pstmt.setString(paramIndex++, entity.getDbaName());
        pstmt.setString(paramIndex++, entity.getPrimaryContact());
        pstmt.setString(paramIndex++, entity.getContactEmail());
        pstmt.setString(paramIndex++, entity.getContactPhone());
        pstmt.setString(paramIndex++, entity.getAddressLine1());
        pstmt.setString(paramIndex++, entity.getAddressLine2());
        pstmt.setString(paramIndex++, entity.getCity());
        pstmt.setString(paramIndex++, entity.getState());
        pstmt.setString(paramIndex++, entity.getZipCode());
        pstmt.setString(paramIndex++, entity.getLicenseNumber());
        pstmt.setDate(paramIndex++, entity.getLicenseExpiry() != null ? 
                Date.valueOf(entity.getLicenseExpiry()) : null);
        pstmt.setDate(paramIndex++, entity.getRegistrationDate() != null ? 
                Date.valueOf(entity.getRegistrationDate()) : Date.valueOf(LocalDate.now()));
        pstmt.setString(paramIndex++, entity.getComplianceStatus().name());
        pstmt.setString(paramIndex++, entity.getRiskLevel().name());
        pstmt.setDate(paramIndex++, entity.getLastReviewDate() != null ? 
                Date.valueOf(entity.getLastReviewDate()) : null);
        pstmt.setDate(paramIndex++, entity.getNextReviewDate() != null ? 
                Date.valueOf(entity.getNextReviewDate()) : null);
        pstmt.setBigDecimal(paramIndex++, entity.getTotalAssets());
        
        if (entity.getEmployeeCount() != null) {
            pstmt.setInt(paramIndex++, entity.getEmployeeCount());
        } else {
            pstmt.setNull(paramIndex++, Types.INTEGER);
        }
        
        pstmt.setString(paramIndex++, entity.isActive() ? "Y" : "N");
        pstmt.setString(paramIndex++, entity.getNotes());
        
        if (includeId) {
            pstmt.setLong(paramIndex++, entity.getEntityId());
        }
    }
    
    // Helper method to map ResultSet to FinancialEntity
    private FinancialEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        FinancialEntity entity = new FinancialEntity();
        
        entity.setEntityId(rs.getLong("entity_id"));
        entity.setEntityName(rs.getString("entity_name"));
        entity.setEntityType(EntityType.valueOf(rs.getString("entity_type")));
        entity.setNmlsId(rs.getString("nmls_id"));
        entity.setDbaName(rs.getString("dba_name"));
        entity.setPrimaryContact(rs.getString("primary_contact"));
        entity.setContactEmail(rs.getString("contact_email"));
        entity.setContactPhone(rs.getString("contact_phone"));
        entity.setAddressLine1(rs.getString("address_line1"));
        entity.setAddressLine2(rs.getString("address_line2"));
        entity.setCity(rs.getString("city"));
        entity.setState(rs.getString("state"));
        entity.setZipCode(rs.getString("zip_code"));
        entity.setLicenseNumber(rs.getString("license_number"));
        
        Date licenseExpiry = rs.getDate("license_expiry");
        entity.setLicenseExpiry(licenseExpiry != null ? licenseExpiry.toLocalDate() : null);
        
        Date registrationDate = rs.getDate("registration_date");
        entity.setRegistrationDate(registrationDate != null ? registrationDate.toLocalDate() : null);
        
        entity.setComplianceStatus(ComplianceStatus.valueOf(rs.getString("compliance_status")));
        entity.setRiskLevel(RiskLevel.valueOf(rs.getString("risk_level")));
        
        Date lastReview = rs.getDate("last_review_date");
        entity.setLastReviewDate(lastReview != null ? lastReview.toLocalDate() : null);
        
        Date nextReview = rs.getDate("next_review_date");
        entity.setNextReviewDate(nextReview != null ? nextReview.toLocalDate() : null);
        
        entity.setTotalAssets(rs.getBigDecimal("total_assets"));
        
        int employeeCount = rs.getInt("employee_count");
        entity.setEmployeeCount(rs.wasNull() ? null : employeeCount);
        
        entity.setActive("Y".equals(rs.getString("is_active")));
        entity.setNotes(rs.getString("notes"));
        
        Timestamp created = rs.getTimestamp("created_date");
        entity.setCreatedDate(created != null ? created.toLocalDateTime() : null);
        entity.setCreatedBy(rs.getString("created_by"));
        
        Timestamp modified = rs.getTimestamp("modified_date");
        entity.setModifiedDate(modified != null ? modified.toLocalDateTime() : null);
        entity.setModifiedBy(rs.getString("modified_by"));
        
        return entity;
    }
}