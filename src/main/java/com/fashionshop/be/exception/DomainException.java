package com.fashionshop.be.exception;

import lombok.Getter;

/**
 * Base exception cho toàn bộ logic nghiệp vụ (Domain).
 * Tuyệt đối KHÔNG phụ thuộc vào thư viện Spring Web ở tầng này (không chứa HttpStatus).
 */
@Getter
public class DomainException extends RuntimeException {
    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

