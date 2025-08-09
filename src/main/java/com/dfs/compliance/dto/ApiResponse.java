package com.dfs.compliance.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standardized API response wrapper
 * Ensures consistent response format across all endpoints
 * 
 * @param <T> the type of data being returned
 */
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    
    /**
     * Private constructor - use static factory methods
     */
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }
    
    /**
     * Creates a successful response with data
     * 
     * @param data the response data
     * @param <T> the type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = "Operation completed successfully";
        response.data = data;
        return response;
    }
    
    /**
     * Creates a successful response with data and custom message
     * 
     * @param data the response data
     * @param message custom success message
     * @param <T> the type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }
    
    /**
     * Creates an error response with message
     * 
     * @param message error message
     * @param <T> the type of data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.data = null;
        return response;
    }
    
    /**
     * Creates an error response with message and error details
     * 
     * @param message error message
     * @param errorDetails additional error information
     * @param <T> the type of data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message, Object errorDetails) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.data = null;
        response.addMetadata("errorDetails", errorDetails);
        return response;
    }
    
    /**
     * Adds metadata to the response
     * 
     * @param key metadata key
     * @param value metadata value
     * @return this response for method chaining
     */
    public ApiResponse<T> addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}