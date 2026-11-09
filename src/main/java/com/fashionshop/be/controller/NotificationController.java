package com.fashionshop.be.controller;

import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.dto.NotificationDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.security.SecurityUtils;
import com.fashionshop.be.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping("/unread-count")
    public ApiResponse<NotificationDtos.UnreadCountResponse> getUnreadCount() {
        User user = securityUtils.getCurrentUserOptional().orElse(null);
        long count = notificationService.getUnreadCount(user);
        return ApiResponse.ok(new NotificationDtos.UnreadCountResponse(count));
    }

    @GetMapping
    public ApiResponse<List<NotificationDtos.NotificationResponseDto>> getRecentNotifications(
            @RequestParam(defaultValue = "10") int limit
    ) {
        User user = securityUtils.getCurrentUserOptional().orElse(null);
        return ApiResponse.ok(notificationService.getRecentNotifications(user, Math.min(limit, 50)));
    }

    @PutMapping("/mark-read")
    public ApiResponse<Void> markAllAsRead() {
        securityUtils.getCurrentUserOptional().ifPresent(notificationService::markAllAsRead);
        return ApiResponse.ok(null);
    }
}

