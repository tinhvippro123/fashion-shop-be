package com.fashionshop.be.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherDto {
        private String id;
        private Long numericId;
        private String code;
        private String name;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderValueRaw;
        private BigDecimal maxDiscountAmount;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer usageLimit;
        private int usedCount;
        private String rawStatus;

        // UI-aligned formatted fields
        private String discountAmount;
        private String minOrderValue;
        private String quantity;
        private String status;
        private String duration;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherUpsertRequest {
        @NotBlank(message = "Mã giảm giá không được để trống")
        private String code;
        private String name;
        @NotBlank(message = "Loại giảm giá không được để trống")
        private String discountType; // PERCENTAGE or FIXED_AMOUNT
        @NotNull(message = "Giá trị giảm không được để trống")
        private BigDecimal discountValue;
        private BigDecimal minOrderValue;
        private BigDecimal maxDiscountAmount;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer usageLimit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateVoucherRequest {
        @NotBlank(message = "Vui lòng nhập mã giảm giá")
        private String code;
        @NotNull(message = "Tổng tiền đơn hàng không hợp lệ")
        private BigDecimal subtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateVoucherResponse {
        private String code;
        private String name;
        private BigDecimal discountAmount;
        private BigDecimal subtotal;
        private BigDecimal finalSubtotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderItemRequest {
        @NotNull(message = "Thiếu mã biến thể sản phẩm")
        private Long variantId;

        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        private int quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderRequest {
        @NotBlank(message = "Họ tên người nhận không được để trống")
        private String recipientName;

        @NotBlank(message = "Số điện thoại người nhận không được để trống")
        @Pattern(regexp = "^(0|\\+84)[35789]\\d{8}$", message = "Số điện thoại người nhận không hợp lệ (gồm 10 số, bắt đầu bằng 03, 05, 07, 08 hoặc 09)")
        private String recipientPhone;

        private String recipientEmail;

        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        private String shippingAddress;

        private String paymentMethod; // COD, BANK_TRANSFER, MOMO
        private String voucherCode;
        private String note;

        @NotEmpty(message = "Giỏ hàng không được trống")
        @Valid
        private List<CreateOrderItemRequest> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String id;
        private Long variantId;
        private Long productId;
        private String productSlug;
        private String name;
        private String image;
        private String variantInfo;
        private String colorName;
        private String sizeName;
        private String sku;
        private BigDecimal price;
        private BigDecimal costPrice;
        private int quantity;
        private BigDecimal totalPrice;
        private boolean isReviewed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfoDto {
        private String id;
        private String name;
        private String type;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfoDto {
        private String address;
        private String method;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedPromotionDto {
        private String id;
        private String name;
        private String code;
        private BigDecimal discountAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponseDto {
        private String id;
        private Long numericId;
        private String orderCode;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private String date;
        private String createdAt;
        private String updatedAt;
        private String status;
        private String paymentStatus;
        private String paymentMethod;
        private String payment;
        private String paymentBg;
        private String paymentUrl;
        private String total;
        private BigDecimal subtotal;
        private BigDecimal shippingFee;
        private BigDecimal discount;
        private BigDecimal totalAmount;
        private String voucherCode;
        private String note;
        private String cancelReason;
        private List<OrderItemDto> items;
        private List<AppliedPromotionDto> appliedPromotions;
        private CustomerInfoDto customer;
        private ShippingInfoDto shipping;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateOrderStatusRequest {
        @NotBlank(message = "Trạng thái mới không được để trống")
        private String status;
        private String reason;
    }
}

