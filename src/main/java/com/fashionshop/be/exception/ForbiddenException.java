package com.fashionshop.be.exception;

public class ForbiddenException extends DomainException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ForbiddenException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

