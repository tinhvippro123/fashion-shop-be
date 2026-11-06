package com.fashionshop.be.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Hệ thống & Bảo mật
    UNCATEGORIZED_EXCEPTION("Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY("Dữ liệu yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED("Vui lòng đăng nhập để tiếp tục", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),

    // Người dùng & Xác thực
    RESOURCE_NOT_FOUND("Không tìm thấy dữ liệu yêu cầu", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("Email này đã được đăng ký trong hệ thống", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("Email hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("Tài khoản của bạn đã bị khóa", HttpStatus.FORBIDDEN),
    OTP_EXPIRED("Mã OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_INVALID("Mã OTP không chính xác", HttpStatus.BAD_REQUEST),
    OTP_COOLDOWN("Vui lòng đợi trước khi yêu cầu gửi lại mã OTP", HttpStatus.BAD_REQUEST),

    // Danh mục & Sản phẩm
    PRODUCT_NOT_FOUND("Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("Không tìm thấy danh mục", HttpStatus.NOT_FOUND),
    INVALID_PRODUCT_SLUG("Đường dẫn sản phẩm không hợp lệ", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND("Biến thể sản phẩm không tồn tại", HttpStatus.NOT_FOUND),

    // Đơn hàng & Giỏ hàng
    ORDER_NOT_FOUND("Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_STATUS_INVALID("Trạng thái đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_ACCESS_DENIED("Bạn không có quyền truy cập đơn hàng này", HttpStatus.FORBIDDEN),
    INSUFFICIENT_STOCK("Số lượng sản phẩm trong kho không đủ", HttpStatus.BAD_REQUEST),

    // Khuyến mãi & Voucher
    VOUCHER_NOT_FOUND("Mã giảm giá không tồn tại", HttpStatus.NOT_FOUND),
    VOUCHER_EXPIRED("Mã giảm giá đã hết hạn sử dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_USAGE_LIMIT("Mã giảm giá đã hết lượt sử dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_ORDER("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
