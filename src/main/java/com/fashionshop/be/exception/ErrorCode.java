package com.fashionshop.be.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 9xxx: Lỗi chung & Hệ thống
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(9001, "Dữ liệu yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(9002, "Vui lòng đăng nhập để tiếp tục", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(9003, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),

    // 1xxx: Tài khoản & Xác thực
    RESOURCE_NOT_FOUND(1000, "Không tìm thấy dữ liệu yêu cầu", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1001, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(1002, "Email này đã được đăng ký trong hệ thống", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1003, "Email hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(1004, "Tài khoản của bạn đã bị khóa", HttpStatus.FORBIDDEN),
    OTP_EXPIRED(1005, "Mã OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1006, "Mã OTP không chính xác", HttpStatus.BAD_REQUEST),
    OTP_COOLDOWN(1007, "Vui lòng đợi trước khi yêu cầu gửi lại mã OTP", HttpStatus.BAD_REQUEST),

    // 2xxx: Danh mục & Sản phẩm
    PRODUCT_NOT_FOUND(2001, "Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(2002, "Không tìm thấy danh mục", HttpStatus.NOT_FOUND),
    INVALID_PRODUCT_SLUG(2003, "Đường dẫn sản phẩm không hợp lệ", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND(2004, "Biến thể sản phẩm không tồn tại", HttpStatus.NOT_FOUND),

    // 3xxx: Đơn hàng & Giỏ hàng
    ORDER_NOT_FOUND(3001, "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_STATUS_INVALID(3002, "Trạng thái đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_ACCESS_DENIED(3003, "Bạn không có quyền truy cập đơn hàng này", HttpStatus.FORBIDDEN),
    INSUFFICIENT_STOCK(3004, "Số lượng sản phẩm trong kho không đủ", HttpStatus.BAD_REQUEST),

    // 4xxx: Khuyến mãi & Voucher
    VOUCHER_NOT_FOUND(4001, "Mã giảm giá không tồn tại", HttpStatus.NOT_FOUND),
    VOUCHER_EXPIRED(4002, "Mã giảm giá đã hết hạn sử dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_USAGE_LIMIT(4003, "Mã giảm giá đã hết lượt sử dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_ORDER(4004, "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
