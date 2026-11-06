package com.fashionshop.be.exception;

public class AppException extends DomainException {
    public AppException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AppException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
