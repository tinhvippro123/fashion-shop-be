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
    FORBIDDEN("Bạn không có quyền thực hiện hành động này"),
    UNAUTHORIZED("Bạn không có quyền thực hiện thao tác này"),
    RESOURCE_NOT_FOUND("Không tìm thấy dữ liệu yêu cầu"),

    // --- Người dùng & Tài khoản (User & Account) ---
    USER_NOT_FOUND("Người dùng không tồn tại"),
    USER_ALREADY_EXISTS("Người dùng đã tồn tại"),
    EMAIL_ALREADY_EXISTS("Email này đã được sử dụng"),
    CURRENT_PASSWORD_INCORRECT("Mật khẩu hiện tại không chính xác"),
    INVALID_CREDENTIALS("Email hoặc mật khẩu không chính xác"),
    ACCOUNT_LOCKED("Tài khoản của bạn đã bị khóa"),
    OTP_EXPIRED("Mã OTP đã hết hạn"),
    OTP_INVALID("Mã OTP không chính xác"),
    OTP_COOLDOWN("Vui lòng đợi trước khi yêu cầu gửi lại mã OTP"),
    GOOGLE_AUTH_FAILED("Đăng nhập bằng Google thất bại"),
    GOOGLE_EMAIL_NOT_FOUND("Không thể lấy địa chỉ email từ tài khoản Google"),
    ADDRESS_NOT_FOUND("Không tìm thấy địa chỉ"),
    ADDRESS_DETAIL_EMPTY("Địa chỉ chi tiết không được để trống"),

    // --- Danh mục & Thuộc tính (Category & Attributes) ---
    CATEGORY_NOT_FOUND("Danh mục không tồn tại"),
    PARENT_CATEGORY_NOT_FOUND("Không tìm thấy danh mục cha"),
    CATEGORY_HAS_CHILDREN("Không thể xóa danh mục cha đang có danh mục con"),
    CATEGORY_HAS_PRODUCTS("Không thể xóa danh mục đang chứa sản phẩm"),
    COLOR_NOT_FOUND("Màu sắc không tồn tại"),
    COLOR_NAME_EMPTY("Tên màu không được để trống"),
    COLOR_ALREADY_EXISTS("Màu sắc đã tồn tại"),
    COLOR_IN_USE("Không thể xóa màu sắc vì đang có sản phẩm sử dụng"),
    SIZE_NOT_FOUND("Kích thước không tồn tại"),
    SIZE_NAME_EMPTY("Tên kích thước không được để trống"),
    SIZE_ALREADY_EXISTS("Kích thước đã tồn tại"),
    SIZE_IN_USE("Không thể xóa kích thước vì đang có biến thể sản phẩm sử dụng"),

    // --- Sản phẩm (Product & Variant) ---
    PRODUCT_NOT_FOUND("Sản phẩm không tồn tại"),
    INVALID_PRODUCT_SLUG("Đường dẫn sản phẩm không hợp lệ"),
    PRODUCT_STOPPED_BUSINESS("Sản phẩm hiện đã ngừng kinh doanh"),
    VARIANT_NOT_FOUND("Biến thể sản phẩm không tồn tại"),
    INSUFFICIENT_STOCK("Số lượng sản phẩm trong kho không đủ"),
    INVALID_QUANTITY("Số lượng không hợp lệ"),
    INVALID_COST_PRICE("Giá vốn không hợp lệ"),

    // --- Giỏ hàng & Đơn hàng (Cart & Order) ---
    CART_ITEM_NOT_FOUND("Không tìm thấy sản phẩm trong giỏ hàng"),
    ORDER_NOT_FOUND("Đơn hàng không tồn tại"),
    ORDER_CODE_EMPTY("Vui lòng nhập mã đơn hàng"),
    ORDER_PHONE_EMPTY("Vui lòng nhập số điện thoại nhận hàng để tra cứu"),
    ORDER_PHONE_MISMATCH("Số điện thoại không khớp với mã đơn hàng"),
    ORDER_STATUS_INVALID("Trạng thái đơn hàng không hợp lệ"),
    CANNOT_CANCEL_ORDER("Chỉ có thể hủy đơn hàng khi đang ở trạng thái Chờ xử lý"),
    ORDER_ACCESS_DENIED("Bạn không có quyền truy cập đơn hàng này"),

    // --- Khuyến mãi & Voucher ---
    VOUCHER_NOT_FOUND("Mã giảm giá không tồn tại"),
    VOUCHER_ALREADY_EXISTS("Mã giảm giá đã tồn tại"),
    VOUCHER_CANCELLED("Mã giảm giá đã bị hủy"),
    VOUCHER_EXPIRED("Mã giảm giá đã hết hạn sử dụng"),
    VOUCHER_NOT_YET_ACTIVE("Mã giảm giá chưa có hiệu lực"),
    VOUCHER_USAGE_LIMIT("Mã giảm giá đã hết lượt sử dụng"),
    VOUCHER_MIN_ORDER("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã"),
    VOUCHER_DATE_INVALID("Ngày kết thúc phải sau ngày bắt đầu"),
    VOUCHER_DISCOUNT_INVALID("Mức giảm giá không hợp lệ"),

    // --- Nhập kho & Đánh giá & Banner & Thanh toán ---
    STOCK_IMPORT_NOT_FOUND("Phiếu nhập kho không tồn tại"),
    REVIEW_NOT_FOUND("Đánh giá không tồn tại"),
    REVIEW_PURCHASE_REQUIRED("Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua và nhận hàng thành công"),
    REVIEW_ALREADY_EXISTS("Bạn đã đánh giá sản phẩm này rồi"),
    BANNER_NOT_FOUND("Banner không tồn tại"),
    PAYMENT_INVALID("Đơn hàng không hợp lệ để tạo liên kết thanh toán"),
    VNPAY_SIGNATURE_MISSING("Thiếu chữ ký số bảo mật từ VNPay"),
    VNPAY_SIGNATURE_INVALID("Chữ ký số VNPay không hợp lệ");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
