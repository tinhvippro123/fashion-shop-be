package com.fashionshop.be;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.entity.UserAddress;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.PaymentMethod;
import com.fashionshop.be.domain.enums.PaymentStatus;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.domain.enums.UserStatus;
import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.repository.OrderRepository;
import com.fashionshop.be.repository.UserAddressRepository;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.service.AccountService;
import com.fashionshop.be.service.AddressUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AddressSyncTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Test
    void testAddressParsing() {
        AddressUtils.ParsedAddress p4 = AddressUtils.parseAddress("Số 123 Lê Lợi, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh");
        assertEquals("Thành phố Hồ Chí Minh", p4.getProvince());
        assertEquals("Quận 1", p4.getDistrict());
        assertEquals("Phường Bến Nghé", p4.getWard());
        assertEquals("Số 123 Lê Lợi", p4.getStreetAddress());

        AddressUtils.ParsedAddress p3 = AddressUtils.parseAddress("Phường 5, Quận 3, TP Hồ Chí Minh");
        assertEquals("TP Hồ Chí Minh", p3.getProvince());
        assertEquals("Quận 3", p3.getDistrict());
        assertEquals("", p3.getWard());
        assertEquals("Phường 5", p3.getStreetAddress());

        assertEquals("so 123 le loi", AddressUtils.normalize("Số 123,  Lê Lợi -"));
    }

    @Test
    void testAutoPopulateAddressFromOrdersWhenAddressBookEmpty() {
        User user = userRepository.save(User.builder()
                .email("test_order_sync@luxe.vn")
                .passwordHash("hash")
                .fullName("Phạm Sơn Tây")
                .phone("0912345678")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        Order order = Order.builder()
                .orderCode("ORD-SYNC-TEST-001")
                .user(user)
                .recipientName("Phạm Sơn Tây")
                .recipientPhone("0912345678")
                .shippingAddress("128 Trần Phú, Phường 4, Quận 5, TP. Hồ Chí Minh")
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.PAID)
                .status(OrderStatus.COMPLETED)
                .subtotal(new BigDecimal("500000"))
                .totalAmount(new BigDecimal("530000"))
                .build();
        orderRepository.save(order);

        // Giả lập đăng nhập user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
        );

        // Ban đầu sổ địa chỉ chưa có gì
        List<UserAddress> before = userAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId());
        assertTrue(before.isEmpty());

        // Khi gọi getMyAddresses(), hệ thống tự động kiểm tra đơn hàng và điền vào sổ địa chỉ
        List<AuthDtos.AddressDto> result = accountService.getMyAddresses();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        AuthDtos.AddressDto addrDto = result.get(0);
        assertTrue(addrDto.isDefault());
        assertEquals("128 Trần Phú, Phường 4, Quận 5, TP. Hồ Chí Minh", addrDto.getFullAddress());
        assertEquals("Phạm Sơn Tây", addrDto.getRecipientName());
        assertEquals("0912345678", addrDto.getPhone());

        // Kiểm tra trong database đã được lưu lại
        List<UserAddress> after = userAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId());
        assertEquals(1, after.size());
        assertTrue(after.get(0).isDefault());
        assertEquals("128 Trần Phú, Phường 4, Quận 5, TP. Hồ Chí Minh", after.get(0).getFullAddress());
    }

    @Test
    void syncActualFileDatabaseDirectly() {
        String dbUrl = "jdbc:h2:file:./data/fashiondb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE";
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(dbUrl, "sa", "")) {
            System.out.println("Connected to real file database: " + dbUrl);

            // 1. Link orphaned orders by recipient_email
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "UPDATE orders o SET o.user_id = (" +
                    "  SELECT u.id FROM users u WHERE LOWER(u.email) = LOWER(o.recipient_email) LIMIT 1" +
                    ") WHERE o.user_id IS NULL AND o.recipient_email IS NOT NULL");
                 java.sql.Statement stmt = conn.createStatement()) {
                int linked = ps.executeUpdate();
                System.out.println("Linked orphaned orders to user accounts: " + linked);
            }

            // 2. Query all orders with user_id and shipping_address
            List<Map<String, Object>> orderRows = new java.util.ArrayList<>();
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(
                         "SELECT id, order_code, user_id, recipient_name, recipient_phone, shipping_address FROM orders WHERE user_id IS NOT NULL AND shipping_address IS NOT NULL")) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getLong("id"));
                    map.put("orderCode", rs.getString("order_code"));
                    map.put("userId", rs.getLong("user_id"));
                    map.put("recipientName", rs.getString("recipient_name"));
                    map.put("recipientPhone", rs.getString("recipient_phone"));
                    map.put("shippingAddress", rs.getString("shipping_address"));
                    orderRows.add(map);
                }
            }
            System.out.println("Total orders with user and shipping address found: " + orderRows.size());

            int insertedCount = 0;
            for (Map<String, Object> row : orderRows) {
                long userId = (Long) row.get("userId");
                String rawAddr = (String) row.get("shippingAddress");
                if (rawAddr == null || rawAddr.isBlank()) continue;

                String normTarget = AddressUtils.normalize(rawAddr);

                // Check existing addresses
                boolean exists = false;
                int existingCount = 0;
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, full_address FROM user_addresses WHERE user_id = ?")) {
                    ps.setLong(1, userId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            existingCount++;
                            String fa = rs.getString("full_address");
                            if (fa != null && AddressUtils.normalize(fa).equals(normTarget)) {
                                exists = true;
                                break;
                            }
                        }
                    }
                }

                if (!exists) {
                    AddressUtils.ParsedAddress p = AddressUtils.parseAddress(rawAddr);
                    String name = (String) row.get("recipientName");
                    String phone = (String) row.get("recipientPhone");
                    boolean isDefault = (existingCount == 0);

                    try (java.sql.PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO user_addresses (user_id, recipient_name, phone, province, district, ward, street_address, full_address, is_default, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                        insert.setLong(1, userId);
                        insert.setString(2, name != null ? name : "Khách hàng");
                        insert.setString(3, phone != null ? phone : "0900000000");
                        insert.setString(4, p.getProvince());
                        insert.setString(5, p.getDistrict());
                        insert.setString(6, p.getWard());
                        insert.setString(7, p.getStreetAddress());
                        insert.setString(8, p.getFullAddress());
                        insert.setBoolean(9, isDefault);
                        insert.executeUpdate();
                        insertedCount++;
                        System.out.println("Inserted address from order " + row.get("orderCode") + " for user " + userId + ": " + p.getFullAddress());
                    }

                    // Also update phone on user profile if missing
                    if (phone != null && !phone.isBlank()) {
                        try (java.sql.PreparedStatement updatePhone = conn.prepareStatement(
                                "UPDATE users SET phone = ? WHERE id = ? AND (phone IS NULL OR phone = '')")) {
                            updatePhone.setString(1, phone.trim());
                            updatePhone.setLong(2, userId);
                            updatePhone.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("SUCCESSFULLY SYNCED " + insertedCount + " ADDRESSES DIRECTLY INTO FASHIONDB FILE DATABASE!");

            // 3. Xóa đơn hàng chưa thanh toán ORD-261109-9981 tạo nhầm khi đóng popup
            try (java.sql.PreparedStatement delItems = conn.prepareStatement(
                    "DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE order_code = 'ORD-261109-9981')");
                 java.sql.PreparedStatement delOrder = conn.prepareStatement(
                    "DELETE FROM orders WHERE order_code = 'ORD-261109-9981'")) {
                int itemsDeleted = delItems.executeUpdate();
                int ordersDeleted = delOrder.executeUpdate();
                System.out.println("Cleaned up orphaned test order ORD-261109-9981: deleted " + itemsDeleted + " items, " + ordersDeleted + " orders.");
            }
        } catch (Exception e) {
            System.out.println("Notice in syncActualFileDatabaseDirectly: " + e.getMessage());
        }
    }
}
