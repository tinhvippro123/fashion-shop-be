package com.fashionshop.be;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.PaymentMethod;
import com.fashionshop.be.domain.enums.PaymentStatus;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.domain.enums.UserStatus;
import com.fashionshop.be.repository.OrderRepository;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationService notificationService;

    @Test
    @Transactional
    void testOrderNotificationLifecycle() {
        // 1. Create customer user
        String email = "notif_user_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash("password123")
                .fullName("Test Customer")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        // 2. Create order for customer
        String orderCode = "ORD-TEST-" + System.currentTimeMillis();
        Order order = orderRepository.save(Order.builder()
                .orderCode(orderCode)
                .user(user)
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.UNPAID)
                .recipientName("Test Customer")
                .recipientPhone("0987654321")
                .shippingAddress("123 Test Street, Hanoi")
                .subtotal(new BigDecimal("500000"))
                .shippingFee(new BigDecimal("30000"))
                .totalAmount(new BigDecimal("530000"))
                .build());

        long initialUnread = notificationService.getUnreadCount(user);
        assertEquals(0, initialUnread);

        // 3. Transition order to PROCESSING -> SHIPPING -> COMPLETED
        notificationService.createOrderNotification(order, OrderStatus.PROCESSING);
        notificationService.createOrderNotification(order, OrderStatus.SHIPPING);
        notificationService.createOrderNotification(order, OrderStatus.COMPLETED);

        long unreadCount = notificationService.getUnreadCount(user);
        assertEquals(3, unreadCount);

        var recentList = notificationService.getRecentNotifications(user, 10);
        assertEquals(3, recentList.size());
        assertTrue(recentList.stream().anyMatch(n -> n.getTitle().contains("đã giao thành công")));

        // 4. Mark all as read
        notificationService.markAllAsRead(user);
        assertEquals(0, notificationService.getUnreadCount(user));
    }
}

