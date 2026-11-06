package com.fashionshop.be.exception;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
