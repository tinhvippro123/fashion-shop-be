package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Notification;
import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.dto.NotificationDtos;
import com.fashionshop.be.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createOrderNotification(Order order, OrderStatus newStatus) {
        if (order == null || order.getUser() == null) {
            return;
        }

        String orderCode = order.getOrderCode() != null ? order.getOrderCode() : ("#" + order.getId());
        String title;
        String message;

        switch (newStatus) {
            case PROCESSING -> {
                title = "Đơn hàng đang chuẩn bị";
                message = "Đơn hàng " + orderCode + " đã được xác nhận và đang được chuẩn bị đóng gói.";
            }
            case SHIPPING -> {
                title = "Đơn hàng đang được giao";
                message = "Đơn hàng " + orderCode + " đã được gửi tới đơn vị vận chuyển và đang trên đường giao đến bạn.";
            }
            case COMPLETED -> {
                title = "Đơn hàng đã giao thành công";
                message = "Đơn hàng " + orderCode + " đã được giao hoàn tất. Cảm ơn bạn đã lựa chọn mua sắm tại LUXE Fashion!";
            }
            case CANCELLED -> {
                title = "Đơn hàng đã hủy";
                message = "Đơn hàng " + orderCode + " đã được hủy cập nhật.";
            }
            default -> {
                title = "Cập nhật đơn hàng " + orderCode;
                message = "Đơn hàng " + orderCode + " đã được cập nhật trạng thái mới.";
            }
        }

        Notification notification = Notification.builder()
                .user(order.getUser())
                .title(title)
                .message(message)
                .type("ORDER_STATUS")
                .orderCode(orderCode)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("[NOTIFICATION] Created order notification for user {} on order {}: {}", order.getUser().getEmail(), orderCode, title);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        if (user == null || user.getId() == null) return 0;
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional(readOnly = true)
    public List<NotificationDtos.NotificationResponseDto> getRecentNotifications(User user, int limit) {
        if (user == null || user.getId() == null) return List.of();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAllAsRead(User user) {
        if (user == null || user.getId() == null) return;
        notificationRepository.markAllAsReadByUserId(user.getId(), LocalDateTime.now());
    }

    private NotificationDtos.NotificationResponseDto toDto(Notification n) {
        return NotificationDtos.NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .orderCode(n.getOrderCode())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}

