package com.fashionshop.be.exception;

public class BusinessException extends BusinessRuleException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
