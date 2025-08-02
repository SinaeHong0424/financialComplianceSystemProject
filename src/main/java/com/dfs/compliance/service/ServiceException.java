package com.dfs.compliance.service;

/**
 * Custom exception for service layer errors.
 * Wraps lower-level exceptions and provides business context.
 * 
 * @author DFS Technology Bureau
 * @version 1.0
 * @since 2025-08-06
 */
public class ServiceException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    
    /**
     * Constructs a new ServiceException with the specified message.
     * 
     * @param message the error message
     */
    public ServiceException(String message) {
        super(message);
        this.errorCode = "SERVICE_ERROR";
    }
    
    /**
     * Constructs a new ServiceException with message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SERVICE_ERROR";
    }
    
    /**
     * Constructs a new ServiceException with message, cause, and error code.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @param errorCode the specific error code
     */
    public ServiceException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}