package com.fashionshop.be.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // --- Lỗi chung & Hệ thống ---
    INTERNAL_SERVER_ERROR("Đã xảy ra lỗi hệ thống"),
    UNCATEGORIZED_EXCEPTION("Lỗi hệ thống không xác định"),
    INVALID_KEY("Dữ liệu yêu cầu không hợp lệ"),
    INVALID_INPUT("Dữ liệu đầu vào không hợp lệ"),
    UNAUTHENTICATED("Vui lòng đăng nhập để tiếp tục"),
    UNAUTHORIZED("Bạn không có quyền thực hiện thao tác này"),
    FORBIDDEN("Bạn không có quyền thực hiện hành động này"),

    // --- Người dùng & Xác thực ---
    RESOURCE_NOT_FOUND("Không tìm thấy dữ liệu yêu cầu"),
    USER_NOT_FOUND("Người dùng không tồn tại"),
    EMAIL_ALREADY_EXISTS("Email này đã được đăng ký trong hệ thống"),
    INVALID_CREDENTIALS("Email hoặc mật khẩu không chính xác"),
    ACCOUNT_LOCKED("Tài khoản của bạn đã bị khóa"),
    OTP_EXPIRED("Mã OTP đã hết hạn"),
    OTP_INVALID("Mã OTP không chính xác"),
    OTP_COOLDOWN("Vui lòng đợi trước khi yêu cầu gửi lại mã OTP"),

    // --- Danh mục & Sản phẩm ---
    PRODUCT_NOT_FOUND("Không tìm thấy sản phẩm"),
    CATEGORY_NOT_FOUND("Không tìm thấy danh mục"),
    INVALID_PRODUCT_SLUG("Đường dẫn sản phẩm không hợp lệ"),
    VARIANT_NOT_FOUND("Biến thể sản phẩm không tồn tại"),

    // --- Đơn hàng & Giỏ hàng ---
    ORDER_NOT_FOUND("Không tìm thấy đơn hàng"),
    ORDER_STATUS_INVALID("Trạng thái đơn hàng không hợp lệ"),
    ORDER_ACCESS_DENIED("Bạn không có quyền truy cập đơn hàng này"),
    INSUFFICIENT_STOCK("Số lượng sản phẩm trong kho không đủ"),

    // --- Khuyến mãi & Voucher ---
    VOUCHER_NOT_FOUND("Mã giảm giá không tồn tại"),
    VOUCHER_EXPIRED("Mã giảm giá đã hết hạn sử dụng"),
    VOUCHER_USAGE_LIMIT("Mã giảm giá đã hết lượt sử dụng"),
    VOUCHER_MIN_ORDER("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
