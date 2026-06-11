package com.loveyadav.traffic_pred.exception;

public class AIServiceException extends RuntimeException {
    public AIServiceException(String message) { super(message); }
    public AIServiceException(String message, Throwable cause) { super(message, cause); }
}