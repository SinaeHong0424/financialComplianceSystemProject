package com.dfs.compliance.controller;


import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfs.compliance.dto.ApiResponse;
import com.dfs.compliance.service.ServiceException;

/**
 * Global Exception Handler for API Controllers
 * Provides centralized exception handling and error responses
 */
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle ServiceException
     * Business logic errors
     */
    public static ApiResponse<?> handleServiceException(
            ServiceException ex, 
            HttpServletRequest request) {
        logger.warn("Service exception at {}: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponse.error(ex.getMessage())
                .addMetadata("path", request.getRequestURI())
                .addMetadata("errorType", "SERVICE_ERROR");
    }
    
    /**
     * Handle SQLException
     * Database errors
     */
    public static ApiResponse<?> handleSQLException(
            SQLException ex, 
            HttpServletRequest request) {
        logger.error("Database exception at {}", request.getRequestURI(), ex);
        return ApiResponse.error("Database error occurred. Please try again later.")
                .addMetadata("path", request.getRequestURI())
                .addMetadata("errorType", "DATABASE_ERROR");
    }
    
    /**
     * Handle IllegalArgumentException
     * Invalid parameters
     */
    public static ApiResponse<?> handleIllegalArgumentException(
            IllegalArgumentException ex, 
            HttpServletRequest request) {
        logger.warn("Invalid argument at {}: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponse.error("Invalid request parameters: " + ex.getMessage())
                .addMetadata("path", request.getRequestURI())
                .addMetadata("errorType", "VALIDATION_ERROR");
    }
    
    /**
     * Handle NullPointerException
     * Null reference errors
     */
    public static ApiResponse<?> handleNullPointerException(
            NullPointerException ex, 
            HttpServletRequest request) {
        logger.error("Null pointer exception at {}", request.getRequestURI(), ex);
        return ApiResponse.error("A required value was missing")
                .addMetadata("path", request.getRequestURI())
                .addMetadata("errorType", "NULL_ERROR");
    }
    
    /**
     * Handle all other exceptions
     * Catch-all for unexpected errors
     */
    public static ApiResponse<?> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        logger.error("Unexpected exception at {}", request.getRequestURI(), ex);
        return ApiResponse.error("An unexpected error occurred. Please contact support.")
                .addMetadata("path", request.getRequestURI())
                .addMetadata("errorType", "INTERNAL_ERROR");
    }
}