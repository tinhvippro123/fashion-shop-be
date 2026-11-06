package com.fashionshop.be.exception;

public class BusinessException extends BusinessRuleException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
