package com.fashionshop.be;

import com.fashionshop.be.domain.entity.ProductVariant;
import com.fashionshop.be.dto.AdminDtos;
import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.repository.ProductVariantRepository;
import com.fashionshop.be.service.AnalyticsService;
import com.fashionshop.be.service.AuthService;
import com.fashionshop.be.service.CatalogService;
import com.fashionshop.be.service.OrderService;
import com.fashionshop.be.service.StockImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class FashionBeApiIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockImportService stockImportService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Test
    void fullEndToEndCatalogOrderCancellationAndStockImportFlow() {
        // 1. Verify DataInitializer seeded products & categories
        List<CatalogDtos.ProductResponseDto> products = catalogService.filterPublicProducts(
                null, null, null, null, null, null, null, null, null, null
        );
        assertTrue(products.size() >= 10, "Should seed at least 10 fashion products");

        // 1b. Test Registration with Email OTP, Gender & Date of Birth
        authService.sendRegisterOtp(
                new AuthDtos.SendOtpRequest("newmember@luxe.vn")
        );
        String otpCode = authService.getOtpForTesting("newmember@luxe.vn");
        assertNotNull(otpCode);
        assertEquals(6, otpCode.length());

        AuthDtos.AuthResponse regResp = authService.register(
                AuthDtos.RegisterRequest.builder()
                        .fullName("Phạm Thu Hà")
                        .email("newmember@luxe.vn")
                        .phone("0933445566")
                        .gender("FEMALE")
                        .dateOfBirth(java.time.LocalDate.of(2002, 6, 12))
                        .otpCode(otpCode)
                        .password("123456")
                        .build()
        );
        assertNotNull(regResp.getToken());
        assertEquals("FEMALE", regResp.getUser().getGender());
        assertEquals("2002-06-12", regResp.getUser().getDateOfBirth());

        // 2. Customer authentication
        AuthDtos.AuthResponse customerAuth = authService.login(
                new AuthDtos.LoginRequest("khachhang@gmail.com", "123456")
        );
        assertNotNull(customerAuth.getToken());
        assertFalse(customerAuth.getUser().isAdmin());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "khachhang@gmail.com", null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                )
        );

        // 3. Pick a variant, place order with WELCOME50 voucher, verify stock deducted
        Long variantId = products.get(0).getProductColors().get(0).getVariants().get(0).getId();
        ProductVariant beforeOrder = productVariantRepository.findById(variantId).orElseThrow();
        int initialStock = beforeOrder.getStock();

        OrderDtos.CreateOrderRequest orderReq = new OrderDtos.CreateOrderRequest(
                "Nguyễn Minh Anh",
                "0901234567",
                "khachhang@gmail.com",
                "68 Nguyễn Huệ, Quận 1, TP.HCM",
                "COD",
                "WELCOME50",
                "Giao giờ hành chính",
                List.of(new OrderDtos.CreateOrderItemRequest(variantId, 2))
        );
        OrderDtos.OrderResponseDto createdOrder = orderService.createOrder(orderReq);
        assertNotNull(createdOrder.getOrderCode());
        assertEquals(0, new BigDecimal("50000").compareTo(createdOrder.getDiscount()));

        ProductVariant afterOrder = productVariantRepository.findById(variantId).orElseThrow();
        assertEquals(initialStock - 2, afterOrder.getStock());

        // 4. Cancel order -> verify stock restored via OOP Encapsulation
        OrderDtos.OrderResponseDto cancelledOrder = orderService.cancelMyOrder(createdOrder.getOrderCode(), "Đổi size");
        assertEquals("CANCELLED", cancelledOrder.getStatus());

        ProductVariant afterCancel = productVariantRepository.findById(variantId).orElseThrow();
        assertEquals(initialStock, afterCancel.getStock());

        // 5. Admin login & Stock Import -> verify stock increased
        AuthDtos.AuthResponse adminAuth = authService.login(
                new AuthDtos.LoginRequest("admin@luxe.vn", "admin123")
        );
        assertTrue(adminAuth.getUser().isAdmin());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@luxe.vn", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        AdminDtos.StockImportResponseDto stockImport = stockImportService.createStockImport(
                new AdminDtos.CreateStockImportRequest(
                        "Xưởng May Tân Bình",
                        "Nhập bổ sung",
                        List.of(new AdminDtos.StockImportItemRequest(variantId, 15, new BigDecimal("550000")))
                )
        );
        assertNotNull(stockImport.getImportCode());

        ProductVariant afterImport = productVariantRepository.findById(variantId).orElseThrow();
        assertEquals(initialStock + 15, afterImport.getStock());

        // 5b. Verify slug-only public product route & Admin updates an ordered & stock-imported product in-place
        assertNotNull(products.get(0).getSlug());
        CatalogDtos.ProductResponseDto publicProdBySlug = catalogService.getProductBySlug(products.get(0).getSlug());
        assertEquals(products.get(0).getId(), publicProdBySlug.getId());
        assertThrows(ResourceNotFoundException.class, () -> catalogService.getProductBySlug(String.valueOf(products.get(0).getId())));

        CatalogDtos.ProductResponseDto existingProd = catalogService.getProductById(products.get(0).getId());
        CatalogDtos.AdminProductUpsertRequest updateReq = new CatalogDtos.AdminProductUpsertRequest(
                existingProd.getName(),
                existingProd.getDescription(),
                existingProd.getBasePrice(),
                existingProd.getDiscountPercent(),
                existingProd.isNew(),
                existingProd.isBestSeller(),
                existingProd.getSku(),
                existingProd.getTotalStock(),
                existingProd.getCategory().getSlug(),
                existingProd.getBrand(),
                existingProd.getImages(),
                existingProd.getOptions(),
                existingProd.getAdminVariants(),
                "published"
        );
        CatalogDtos.ProductResponseDto updatedProd = catalogService.updateAdminProduct(existingProd.getId(), updateReq);
        assertEquals(existingProd.getId(), updatedProd.getId());
        assertEquals(initialStock + 15, productVariantRepository.findById(variantId).orElseThrow().getStock());

        // 6. Dashboard analytics returns positive revenue & chart data
        AdminDtos.DashboardResponseDto dashboard = analyticsService.getDashboardAndReports();
        assertTrue(dashboard.getStats().getTotalRevenue().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(6, dashboard.getChartData().size());

        SecurityContextHolder.clearContext();
    }
}

