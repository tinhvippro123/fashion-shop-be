package com.fashionshop.be.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemDto {
        private Long id;
        private Long variantId;
        private Long productId;
        private String productName;
        private String productSlug;
        private Long colorId;
        private String colorName;
        private String sizeName;
        private String sku;
        private String imageUrl;
        private BigDecimal unitPrice;
        private BigDecimal originalPrice;
        private int discountPercent;
        private int quantity;
        private int stock;
        private BigDecimal lineTotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartResponseDto {
        @Builder.Default
        private List<CartItemDto> items = new ArrayList<>();
        private int totalItems;
        private BigDecimal totalPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddToCartRequest {
        @NotNull(message = "Mã biến thể không được để trống")
        private Long variantId;

        @Min(value = 1, message = "Số lượng phải từ 1 trở lên")
        private int quantity = 1;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCartItemRequest {
        @Min(value = 1, message = "Số lượng phải từ 1 trở lên")
        private int quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncCartItem {
        private Long variantId;
        private int quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncCartRequest {
        private List<SyncCartItem> items;
    }
}
