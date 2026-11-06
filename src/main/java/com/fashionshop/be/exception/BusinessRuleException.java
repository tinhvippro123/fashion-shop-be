package com.fashionshop.be.exception;

public class BusinessRuleException extends DomainException {
    public BusinessRuleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
