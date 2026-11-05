package com.fashionshop.be.dto;

import com.fashionshop.be.domain.enums.BannerPosition;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BannerDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BannerResponseDto {
        private Long id;
        private String title;
        private String subtitle;
        private String imageUrl;
        private String targetUrl;
        private BannerPosition position;
        private Integer sortOrder;
        @JsonProperty("isActive")
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBannerRequest {
        @NotBlank(message = "Tiêu đề banner không được để trống")
        private String title;

        private String subtitle;

        @NotBlank(message = "Đường dẫn ảnh không được để trống")
        private String imageUrl;

        private String targetUrl;

        @NotNull(message = "Vị trí banner không được để trống")
        private BannerPosition position;

        private Integer sortOrder;

        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateBannerRequest {
        private String title;
        private String subtitle;
        private String imageUrl;
        private String targetUrl;
        private BannerPosition position;
        private Integer sortOrder;
        private Boolean isActive;
    }
}

