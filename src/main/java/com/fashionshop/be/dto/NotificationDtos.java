package com.fashionshop.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreadCountResponse {
        private long unreadCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponseDto {
        private Long id;
        private String title;
        private String message;
        private String type;
        private String orderCode;
        private boolean isRead;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;
    }
}

