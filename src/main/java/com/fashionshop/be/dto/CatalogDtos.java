package com.fashionshop.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CatalogDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySimpleDto {
        private Long id;
        private String name;
        private String slug;
        private CategorySimpleDto parent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private String id;
        private Long numericId;
        private Long parentId;
        private String parentName;
        private String name;
        private String slug;
        private String description;
        private long productCount;
        private String status;
        @JsonProperty("isActive")
        private boolean isActive;
        private List<CategoryDto> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryUpsertRequest {
        @NotBlank(message = "Tên danh mục không được để trống")
        private String name;
        private String slug;
        private String description;
        private Long parentId;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColorDto {
        private Long id;
        private String name;
        private String hexCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeDto {
        private Long id;
        private String name;
        private int sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageDto {
        private Long id;
        private String imageUrl;
        private int displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDto {
        private Long id;
        private String sku;
        private BigDecimal price;
        private BigDecimal costPrice;
        private int stock;
        private SizeDto size;
        private String colorName;
        private String hexCode;
        private Long productId;
        private String productName;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductColorDto {
        private Long id;
        @JsonProperty("isActive")
        private boolean isActive;
        private ColorDto color;
        private List<ProductImageDto> images;
        private List<VariantDto> variants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminOptionDto {
        private String name;
        private List<String> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminVariantPayloadDto {
        private Long id;
        private String sku;
        private BigDecimal price;
        private BigDecimal costPrice;
        private int stock;
        private Map<String, String> options;
        private String image;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductResponseDto {
        private Long id;
        private String name;
        private String slug;
        private String sku;
        private String brand;
        private BigDecimal basePrice;
        private BigDecimal effectivePrice;
        private String formattedPrice;
        private int discountPercent;
        private String description;
        private String material;
        private String careInstructions;
        @JsonProperty("isNew")
        private boolean isNew;
        @JsonProperty("isBestSeller")
        private boolean isBestSeller;
        @JsonProperty("isActive")
        private boolean isActive;
        private int totalStock;
        private int sold;
        private double rating;
        private long reviewCount;
        private CategorySimpleDto category;
        private List<ProductColorDto> productColors;
        private List<String> images;
        private Map<String, List<String>> colorImages;
        private List<AdminOptionDto> options;
        private List<AdminVariantPayloadDto> adminVariants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminProductUpsertRequest {
        @NotBlank(message = "Tên sản phẩm không được để trống")
        private String name;
        private String description;
        private String material;
        private String careInstructions;
        @NotNull(message = "Giá bán không được để trống")
        private BigDecimal price;
        private Integer discountPercent;
        private Boolean isNew;
        private Boolean isBestSeller;
        private String sku;
        private Integer stock;
        @NotBlank(message = "Danh mục không được để trống")
        private String category;
        private String brand;
        private List<String> images;
        private Map<String, List<String>> colorImages;
        private List<AdminOptionDto> options;
        private List<AdminVariantPayloadDto> variants;
        private String status;

        public AdminProductUpsertRequest(
                String name,
                String description,
                BigDecimal price,
                Integer discountPercent,
                Boolean isNew,
                Boolean isBestSeller,
                String sku,
                Integer stock,
                String category,
                String brand,
                List<String> images,
                List<AdminOptionDto> options,
                List<AdminVariantPayloadDto> variants,
                String status
        ) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.discountPercent = discountPercent;
            this.isNew = isNew;
            this.isBestSeller = isBestSeller;
            this.sku = sku;
            this.stock = stock;
            this.category = category;
            this.brand = brand;
            this.images = images;
            this.options = options;
            this.variants = variants;
            this.status = status;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionSuggestionsDto {
        private List<String> names;
        private Map<String, List<String>> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDto {
        private String id;
        private Long numericId;
        private String productId;
        private String productName;
        private String customerName;
        private String customerPhone;
        private int rating;
        private String comment;
        private String reply;
        @JsonProperty("isHidden")
        private boolean isHidden;
        private String createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReviewRequest {
        @Min(value = 1, message = "Số sao tối thiểu là 1")
        @Max(value = 5, message = "Số sao tối đa là 5")
        private int rating;

        @NotBlank(message = "Nội dung đánh giá không được để trống")
        private String comment;
    }
}
