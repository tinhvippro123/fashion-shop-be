package com.fashionshop.be.exception;

public class BusinessRuleException extends DomainException {
    public BusinessRuleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessRuleException(String message) {
        super(ErrorCode.INVALID_KEY, message);
    }

    public BusinessRuleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

