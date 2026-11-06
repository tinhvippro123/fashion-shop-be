package com.fashionshop.be.exception;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
