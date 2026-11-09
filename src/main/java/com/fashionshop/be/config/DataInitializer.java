package com.fashionshop.be.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fashionshop.be.domain.entity.Banner;
import com.fashionshop.be.domain.entity.Category;
import com.fashionshop.be.domain.entity.Color;
import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.OrderItem;
import com.fashionshop.be.domain.entity.Product;
import com.fashionshop.be.domain.entity.ProductColor;
import com.fashionshop.be.domain.entity.ProductImage;
import com.fashionshop.be.domain.entity.ProductVariant;
import com.fashionshop.be.domain.entity.Review;
import com.fashionshop.be.domain.entity.Size;
import com.fashionshop.be.domain.entity.StockImport;
import com.fashionshop.be.domain.entity.StockImportItem;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.entity.UserAddress;
import com.fashionshop.be.domain.entity.Voucher;
import com.fashionshop.be.domain.entity.Wishlist;
import com.fashionshop.be.domain.enums.BannerPosition;
import com.fashionshop.be.domain.enums.DiscountType;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.PaymentMethod;
import com.fashionshop.be.domain.enums.PaymentStatus;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.domain.enums.UserStatus;
import com.fashionshop.be.domain.enums.VoucherStatus;
import com.fashionshop.be.repository.BannerRepository;
import com.fashionshop.be.repository.CategoryRepository;
import com.fashionshop.be.repository.ColorRepository;
import com.fashionshop.be.repository.OrderRepository;
import com.fashionshop.be.repository.ProductRepository;
import com.fashionshop.be.repository.ProductVariantRepository;
import com.fashionshop.be.repository.ReviewRepository;
import com.fashionshop.be.repository.SizeRepository;
import com.fashionshop.be.repository.StockImportRepository;
import com.fashionshop.be.repository.UserAddressRepository;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.repository.VoucherRepository;
import com.fashionshop.be.repository.WishlistRepository;
import com.fashionshop.be.service.AddressUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StockImportRepository stockImportRepository;
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    private final BannerRepository bannerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            sanitizeLegacyDataIfNeeded();
            seedBannersIfEmpty();
            return;
        }

        // 1. Seed Users & Addresses
        User admin = userRepository.save(User.builder()
                .email("admin@luxe.vn")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("Quản Trị Viên LUXE")
                .phone("0909888999")
                .gender("MALE")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        User customer1 = userRepository.save(User.builder()
                .email("khachhang@gmail.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .fullName("Nguyễn Minh Anh")
                .phone("0901234567")
                .gender("FEMALE")
                .dateOfBirth(LocalDate.of(2000, 8, 15))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        User customer2 = userRepository.save(User.builder()
                .email("tranthibao@gmail.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .fullName("Trần Thị Bảo Ngọc")
                .phone("0912345678")
                .gender("FEMALE")
                .dateOfBirth(LocalDate.of(1998, 11, 2))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        User customer3 = userRepository.save(User.builder()
                .email("lehoangnam@gmail.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .fullName("Lê Hoàng Nam")
                .phone("0987654321")
                .gender("MALE")
                .dateOfBirth(LocalDate.of(1997, 3, 10))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        userAddressRepository.save(UserAddress.builder()
                .user(customer1)
                .recipientName("Nguyễn Minh Anh")
                .phone("0901234567")
                .province("TP. Hồ Chí Minh")
                .district("Quận 1")
                .ward("Phường Bến Nghé")
                .streetAddress("68 Nguyễn Huệ")
                .fullAddress("68 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh")
                .isDefault(true)
                .build());

        userAddressRepository.save(UserAddress.builder()
                .user(customer2)
                .recipientName("Trần Thị Bảo Ngọc")
                .phone("0912345678")
                .province("Thành phố Hà Nội")
                .district("Quận Hoàn Kiếm")
                .ward("Phường Tràng Tiền")
                .streetAddress("25 Tràng Tiền")
                .fullAddress("25 Tràng Tiền, Phường Tràng Tiền, Quận Hoàn Kiếm, Thành phố Hà Nội")
                .isDefault(true)
                .build());

        // 2. Seed Categories (Hierarchy 3 Cấp: Root (Nam/Nữ) -> Level 2 (Áo, Quần, Đầm, Chân váy) -> Level 3 (Chi tiết theo LUXE))
        // --- LEVEL 1: ROOT ---
        Category catNu = categoryRepository.save(Category.builder()
                .name("Thời trang Nữ")
                .slug("nu")
                .description("Bộ sưu tập thời trang công sở & dạo phố cao cấp dành cho phái đẹp")
                .isActive(true)
                .build());

        Category catNam = categoryRepository.save(Category.builder()
                .name("Thời trang Nam")
                .slug("nam")
                .description("Trang phục lịch lãm, hiện đại dành cho quý ông")
                .isActive(true)
                .build());

        // --- LEVEL 2: NHÓM DANH MỤC NỮ ---
        Category catAoNu = categoryRepository.save(Category.builder()
                .parent(catNu).name("Áo Nữ").slug("ao-nu").description("Áo sơ mi, áo khoác phái đẹp").isActive(true).build());
        Category catDamNu = categoryRepository.save(Category.builder()
                .parent(catNu).name("Đầm").slug("dam-nu").description("Đầm chữ A, đầm công sở thanh lịch").isActive(true).build());
        Category catQuanNu = categoryRepository.save(Category.builder()
                .parent(catNu).name("Quần Nữ").slug("quan-nu").description("Quần âu thanh lịch").isActive(true).build());
        Category catChanVayNu = categoryRepository.save(Category.builder()
                .parent(catNu).name("Chân Váy").slug("chan-vay-nu").description("Chân váy chữ A, chân váy xòe").isActive(true).build());

        // --- LEVEL 3: CHI TIẾT DANH MỤC NỮ (Gắn sản phẩm) ---
        Category catSoMiNu = categoryRepository.save(Category.builder()
                .parent(catAoNu).name("Áo sơ mi").slug("ao-so-mi-nu").description("Áo sơ mi lụa, sơ mi công sở").isActive(true).build());
        Category catKhoacNu = categoryRepository.save(Category.builder()
                .parent(catAoNu).name("Áo khoác").slug("ao-khoac-nu").description("Áo khoác dạ tweed, áo jacket").isActive(true).build());

        Category catDamXoeNu = categoryRepository.save(Category.builder()
                .parent(catDamNu).name("Đầm chữ A").slug("dam-xoe-nu").description("Đầm chữ A xếp ly, dáng xòe bay bổng").isActive(true).build());
        Category catDamCongSoNu = categoryRepository.save(Category.builder()
                .parent(catDamNu).name("Đầm công sở").slug("dam-cong-so-nu").description("Đầm công sở thanh lịch, đầm lụa sang trọng").isActive(true).build());

        Category catQuanAuNu = categoryRepository.save(Category.builder()
                .parent(catQuanNu).name("Quần âu").slug("quan-au-nu").description("Quần âu ống loe, quần suông Tuytsi tôn dáng").isActive(true).build());

        Category catChanVayANu = categoryRepository.save(Category.builder()
                .parent(catChanVayNu).name("Chân váy chữ A").slug("chan-vay-a-nu").description("Chân váy chữ A, chân váy midi xẻ tà").isActive(true).build());
        Category catChanVayXoeNu = categoryRepository.save(Category.builder()
                .parent(catChanVayNu).name("Chân váy xòe").slug("chan-vay-xoe-nu").description("Chân váy xếp ly, ren hai lớp bồng bềnh").isActive(true).build());

        // --- LEVEL 2: NHÓM DANH MỤC NAM ---
        Category catAoNam = categoryRepository.save(Category.builder()
                .parent(catNam).name("Áo Nam").slug("ao-nam").description("Áo sơ mi, áo polo và áo khoác nam").isActive(true).build());
        Category catQuanNam = categoryRepository.save(Category.builder()
                .parent(catNam).name("Quần Nam").slug("quan-nam").description("Quần dài và quần short chuẩn phom").isActive(true).build());

        // --- LEVEL 3: CHI TIẾT DANH MỤC NAM (Gắn sản phẩm) ---
        Category catPoloNam = categoryRepository.save(Category.builder()
                .parent(catAoNam).name("Áo polo").slug("ao-polo-nam").description("Áo polo Cool Air, modal, len dệt kim").isActive(true).build());
        Category catSoMiNam = categoryRepository.save(Category.builder()
                .parent(catAoNam).name("Áo sơ mi").slug("ao-so-mi-nam").description("Áo sơ mi caro, denim và regular fit").isActive(true).build());
        Category catKhoacNam = categoryRepository.save(Category.builder()
                .parent(catAoNam).name("Áo khoác").slug("ao-khoac-nam").description("Áo khoác dạ lông cừu, bomber và áo nỉ").isActive(true).build());

        Category catQuanDaiNam = categoryRepository.save(Category.builder()
                .parent(catQuanNam).name("Quần dài").slug("quan-dai-nam").description("Quần dài kaki, quần âu chuẩn phom").isActive(true).build());
        Category catQuanShortNam = categoryRepository.save(Category.builder()
                .parent(catQuanNam).name("Quần short").slug("quan-short-nam").description("Quần short regular cotton thoải mái").isActive(true).build());

        // 3. Seed Colors & Sizes
        colorRepository.save(Color.builder().name("Đen").hexCode("#111111").build());
        colorRepository.save(Color.builder().name("Trắng").hexCode("#FFFFFF").build());
        colorRepository.save(Color.builder().name("Kem Be").hexCode("#E8DCC8").build());
        colorRepository.save(Color.builder().name("Xanh Navy").hexCode("#1B365D").build());
        colorRepository.save(Color.builder().name("Đỏ Ruby").hexCode("#A61C1C").build());
        colorRepository.save(Color.builder().name("Xám Nhạt").hexCode("#D1D5DB").build());

        Size sS = sizeRepository.save(Size.builder().name("S").sortOrder(1).build());
        Size sM = sizeRepository.save(Size.builder().name("M").sortOrder(2).build());
        Size sL = sizeRepository.save(Size.builder().name("L").sortOrder(3).build());
        Size sXL = sizeRepository.save(Size.builder().name("XL").sortOrder(4).build());
        List<Size> standardSizes = List.of(sS, sM, sL, sXL);

        // 4. Seed 41 Realistic Fashion Products across 12 Subcategories for LUXE Catalog
        Map<String, Category> catMap = Map.ofEntries(
                Map.entry("ao-so-mi-nu", catSoMiNu),
                Map.entry("ao-khoac-nu", catKhoacNu),
                Map.entry("dam-xoe-nu", catDamXoeNu),
                Map.entry("dam-cong-so-nu", catDamCongSoNu),
                Map.entry("quan-au-nu", catQuanAuNu),
                Map.entry("chan-vay-a-nu", catChanVayANu),
                Map.entry("chan-vay-xoe-nu", catChanVayXoeNu),
                Map.entry("ao-so-mi-nam", catSoMiNam),
                Map.entry("ao-polo-nam", catPoloNam),
                Map.entry("ao-khoac-nam", catKhoacNam),
                Map.entry("quan-dai-nam", catQuanDaiNam),
                Map.entry("quan-short-nam", catQuanShortNam)
        );
        List<Product> luxeProducts = seedProductsFromLuxeJson(catMap, standardSizes);

        Product p1 = luxeProducts.get(0);
        Product p2 = luxeProducts.size() > 1 ? luxeProducts.get(1) : p1;
        Product p3 = luxeProducts.size() > 2 ? luxeProducts.get(2) : p1;
        Product p7 = luxeProducts.stream()
                .filter(p -> p.getCategory() != null && "ao-so-mi-nam".equals(p.getCategory().getSlug()))
                .findFirst()
                .orElse(p1);
        Product p9 = luxeProducts.stream()
                .filter(p -> p.getCategory() != null && "ao-khoac-nam".equals(p.getCategory().getSlug()))
                .findFirst()
                .orElse(p7);

        // 5. Seed Stock Imports (Phiếu Nhập Kho) demonstrating OOP importStock
        ProductVariant v1 = p1.getProductColors().get(0).getVariants().get(0);
        ProductVariant v2 = p7.getProductColors().get(0).getVariants().get(1);
        ProductVariant v3 = p9.getProductColors().get(0).getVariants().get(1);

        StockImport import1 = StockImport.builder()
                .importCode("NK-261010-1001")
                .supplierName("Xưởng May Thời Trang Cao Cấp Đông Á")
                .note("Nhập lô hàng Thu Đông đợt 1")
                .createdByEmail(admin.getEmail())
                .build();
        v1.importStock(20, new BigDecimal("620000"));
        v2.importStock(25, new BigDecimal("420000"));
        import1.addItem(StockImportItem.builder()
                .variant(v1).quantity(20).unitCost(new BigDecimal("620000")).subtotal(new BigDecimal("12400000")).build());
        import1.addItem(StockImportItem.builder()
                .variant(v2).quantity(25).unitCost(new BigDecimal("420000")).subtotal(new BigDecimal("10500000")).build());
        stockImportRepository.save(import1);

        StockImport import2 = StockImport.builder()
                .importCode("NK-261022-1002")
                .supplierName("Công ty Dệt May Sài Gòn LUXE")
                .note("Bổ sung tồn kho Blazer nam")
                .createdByEmail(admin.getEmail())
                .build();
        v3.importStock(10, new BigDecimal("920000"));
        import2.addItem(StockImportItem.builder()
                .variant(v3).quantity(10).unitCost(new BigDecimal("920000")).subtotal(new BigDecimal("9200000")).build());
        stockImportRepository.save(import2);

        // 6. Seed Vouchers
        voucherRepository.save(Voucher.builder()
                .code("WELCOME50")
                .name("Chào bạn mới giảm 50.000đ")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("50000"))
                .minOrderValue(new BigDecimal("300000"))
                .startDate(LocalDateTime.now().minusMonths(1))
                .endDate(LocalDateTime.now().plusMonths(3))
                .usageLimit(200)
                .usedCount(14)
                .status(VoucherStatus.ACTIVE)
                .build());

        voucherRepository.save(Voucher.builder()
                .code("LUXE10")
                .name("Ưu đãi 10% tối đa 150.000đ")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .minOrderValue(new BigDecimal("600000"))
                .maxDiscountAmount(new BigDecimal("150000"))
                .startDate(LocalDateTime.now().minusDays(15))
                .endDate(LocalDateTime.now().plusMonths(2))
                .usageLimit(100)
                .usedCount(28)
                .status(VoucherStatus.ACTIVE)
                .build());

        voucherRepository.save(Voucher.builder()
                .code("VIP200")
                .name("Đặc quyền đơn từ 2 triệu giảm 200.000đ")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("200000"))
                .minOrderValue(new BigDecimal("2000000"))
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().plusMonths(2))
                .usageLimit(50)
                .usedCount(7)
                .status(VoucherStatus.ACTIVE)
                .build());

        voucherRepository.save(Voucher.builder()
                .code("BLACKFRIDAY20")
                .name("Siêu hội cuối năm giảm 20%")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20"))
                .minOrderValue(new BigDecimal("1000000"))
                .maxDiscountAmount(new BigDecimal("300000"))
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusMonths(2))
                .usageLimit(150)
                .usedCount(0)
                .status(VoucherStatus.SCHEDULED)
                .build());

        // 7. Seed Sample Orders across recent months for Dashboard Charts
        seedHistoricalOrder("ORD-260715-8001", customer1, p1, 2, OrderStatus.COMPLETED, PaymentMethod.BANK_TRANSFER,
                PaymentStatus.PAID, "LUXE10", new BigDecimal("150000"), LocalDateTime.now().minusMonths(3));
        seedHistoricalOrder("ORD-260818-8002", customer2, p7, 3, OrderStatus.COMPLETED, PaymentMethod.COD,
                PaymentStatus.PAID, "WELCOME50", new BigDecimal("50000"), LocalDateTime.now().minusMonths(2));
        seedHistoricalOrder("ORD-260920-8003", customer3, p9, 1, OrderStatus.COMPLETED, PaymentMethod.BANK_TRANSFER,
                PaymentStatus.PAID, null, BigDecimal.ZERO, LocalDateTime.now().minusMonths(1));
        seedHistoricalOrder("ORD-261025-8004", customer1, p2, 1, OrderStatus.SHIPPING, PaymentMethod.COD,
                PaymentStatus.UNPAID, null, BigDecimal.ZERO, LocalDateTime.now().minusDays(3));
        seedHistoricalOrder("ORD-261028-8005", customer2, p3, 2, OrderStatus.PENDING, PaymentMethod.COD,
                PaymentStatus.UNPAID, "WELCOME50", new BigDecimal("50000"), LocalDateTime.now().minusHours(5));

        // 8. Seed Reviews & Wishlist
        reviewRepository.save(Review.builder()
                .user(customer1).product(p1).rating(5)
                .comment("Chất lụa cực kỳ mát và mềm, mặc đi làm ai cũng khen sang! Sẽ tiếp tục ủng hộ LUXE.")
                .adminReply("Cảm ơn chị Minh Anh đã tin chọn thiết kế của LUXE ạ!")
                .isVisible(true).build());
        reviewRepository.save(Review.builder()
                .user(customer2).product(p1).rating(5)
                .comment("Đường may tỉ mỉ, đóng gói hộp giấy thơm tho chỉn chu.")
                .isVisible(true).build());
        reviewRepository.save(Review.builder()
                .user(customer3).product(p7).rating(4)
                .comment("Áo sơ mi bamboo mặc thoáng, giặt máy không bị nhăn nhiều, rất đáng tiền.")
                .isVisible(true).build());

        wishlistRepository.save(Wishlist.builder().user(customer1).product(p1).build());
        wishlistRepository.save(Wishlist.builder().user(customer1).product(p3).build());
        seedBannersIfEmpty();
    }

    private void seedBannersIfEmpty() {
        if (bannerRepository.count() > 0) {
            return;
        }

        // 1. HERO Banners
        bannerRepository.save(Banner.builder()
                .title("BỘ SƯU TẬP THỜI TRANG NỮ")
                .subtitle("Thanh lịch, hiện đại và cuốn hút trong từng đường nét")
                .imageUrl("/client/img/banner-1.jpg")
                .targetUrl("/danh-muc/nu")
                .position(BannerPosition.HERO)
                .sortOrder(1)
                .isActive(true)
                .build());

        bannerRepository.save(Banner.builder()
                .title("XU HƯỚNG THỜI TRANG NAM")
                .subtitle("Phong cách lịch lãm, bản lĩnh phái mạnh")
                .imageUrl("/client/img/banner-2.jpg")
                .targetUrl("/danh-muc/nam")
                .position(BannerPosition.HERO)
                .sortOrder(2)
                .isActive(true)
                .build());

        // 2. FEATURED Banners (Featured Categories on Home)
        bannerRepository.save(Banner.builder()
                .title("ĐẦM")
                .subtitle("BST Đầm thanh lịch, sang trọng")
                .imageUrl("/uploads/products/002eab5bd95091694139ae9e8f046007.webp")
                .targetUrl("/danh-muc/dam-nu")
                .position(BannerPosition.FEATURED)
                .sortOrder(1)
                .isActive(true)
                .build());

        bannerRepository.save(Banner.builder()
                .title("ÁO NỮ")
                .subtitle("Sơ mi & áo kiểu nữ tính, hiện đại")
                .imageUrl("/uploads/products/f4a49449c18a61d9d49c0f976bc56606.webp")
                .targetUrl("/danh-muc/ao-nu")
                .position(BannerPosition.FEATURED)
                .sortOrder(2)
                .isActive(true)
                .build());

        bannerRepository.save(Banner.builder()
                .title("ÁO NAM")
                .subtitle("Polo, sơ mi chuẩn phom lịch lãm")
                .imageUrl("/uploads/products/9278e16772c52fe0c41b035b775a2f9c.jpg")
                .targetUrl("/danh-muc/ao-nam")
                .position(BannerPosition.FEATURED)
                .sortOrder(3)
                .isActive(true)
                .build());

        bannerRepository.save(Banner.builder()
                .title("SẢN PHẨM SALE")
                .subtitle("Ưu đãi đặc biệt giảm đến 50%")
                .imageUrl("/uploads/products/0284007651a63e1aa34493daa2754ba1.webp")
                .targetUrl("/danh-muc/sale")
                .position(BannerPosition.FEATURED)
                .sortOrder(4)
                .isActive(true)
                .build());

        log.info("Initialized default banners (2 HERO, 4 FEATURED).");
    }

    private void sanitizeLegacyDataIfNeeded() {
        try {
            jdbcTemplate.update("UPDATE users SET gender = 'FEMALE' WHERE gender IS NULL");
            jdbcTemplate.update("UPDATE users SET date_of_birth = DATE '2000-08-15' WHERE date_of_birth IS NULL");
            jdbcTemplate.execute("DROP TABLE IF EXISTS contact_messages");
            jdbcTemplate.update("UPDATE banners SET title = 'ĐẦM' WHERE title = 'ĐẦM & CHÂN VÁY'");
            jdbcTemplate.update("UPDATE banners SET title = 'ÁO NAM' WHERE title = 'ÁO POLO & SƠ MI NAM'");
            jdbcTemplate.update("UPDATE banners SET title = 'SẢN PHẨM SALE' WHERE title = 'ƯU ĐÃI ĐẶC BIỆT'");
        } catch (Exception e) {
            log.warn("Notice in user sanitize: {}", e.getMessage());
        }

        try {
            jdbcTemplate.update("DELETE FROM product_images WHERE image_url NOT LIKE 'products/%' AND image_url NOT LIKE '/uploads/%' AND (image_url LIKE '%.webp' OR image_url LIKE '%-%-%-%-%.webp')");
            jdbcTemplate.update(
                    "UPDATE order_items oi SET image_url = (" +
                    "  SELECT pi.image_url FROM product_images pi " +
                    "  JOIN product_colors pc ON pc.id = pi.product_color_id " +
                    "  JOIN product_variants pv ON pv.product_color_id = pc.id " +
                    "  WHERE pv.id = oi.variant_id " +
                    "  ORDER BY pi.display_order ASC LIMIT 1" +
                    ") WHERE oi.image_url LIKE '%.webp' AND oi.image_url NOT LIKE 'products/%' AND oi.image_url NOT LIKE '/uploads/%'"
            );
        } catch (Exception e) {
            log.warn("Notice in image cleanup: {}", e.getMessage());
        }

        // Dọn dẹp an toàn size M2 và L2: gộp tồn kho biến thể M2 -> M, L2 -> L và xóa size M2, L2
        try {
            mergeAndRemoveSize("M2", "M");
            mergeAndRemoveSize("L2", "L");
        } catch (Exception e) {
            log.warn("Notice in mergeAndRemoveSize: {}", e.getMessage());
        }

        // Chuẩn hóa tên màu sắc lạ trong bảng colors
        try {
            Map<String, String> colorFixes = Map.ofEntries(
                    Map.entry("Xám đậm thanh lịch.", "Xám đậm"),
                    Map.entry("Xám đậm thanh lịch", "Xám đậm"),
                    Map.entry("Họa tiết Xanh bơ", "Xanh bơ"),
                    Map.entry("Họa tiết Xanh bầu trời", "Xanh da trời"),
                    Map.entry("Kẻ Bạc hà", "Bạc hà"),
                    Map.entry("Kẻ Xanh cổ vịt đậm", "Xanh cổ vịt"),
                    Map.entry("Khói đậm", "Xám khói"),
                    Map.entry("Be vàng", "Be"),
                    Map.entry("Ghi sáng", "Ghi"),
                    Map.entry("Ghi khói", "Ghi"),
                    Map.entry("Nâu rêu", "Nâu"),
                    Map.entry("Nâu socola", "Nâu")
            );
            for (Map.Entry<String, String> entry : colorFixes.entrySet()) {
                Optional<Color> badCol = colorRepository.findByNameIgnoreCase(entry.getKey());
                if (badCol.isPresent()) {
                    Optional<Color> goodCol = colorRepository.findByNameIgnoreCase(entry.getValue());
                    if (goodCol.isPresent()) {
                        try {
                            jdbcTemplate.update("UPDATE product_colors SET color_id = ? WHERE color_id = ?", goodCol.get().getId(), badCol.get().getId());
                            colorRepository.delete(badCol.get());
                        } catch (Exception ignored) {}
                    } else {
                        Color col = badCol.get();
                        col.setName(entry.getValue());
                        colorRepository.save(col);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Notice in colorFixes: {}", e.getMessage());
        }

        // Đồng bộ toàn bộ mô tả HTML và hình ảnh đầy đủ từ luxe-products.json
        try {
            syncLuxeProductsData();
        } catch (Exception e) {
            log.warn("Notice in syncLuxeProductsData: {}", e.getMessage());
        }

        try {
            if (productRepository.count() < 20) {
                Map<String, Category> map = new HashMap<>();
                categoryRepository.findAll().forEach(c -> map.put(c.getSlug(), c));
                if (!map.isEmpty()) {
                    List<Size> standardSizes = sizeRepository.findAllByOrderBySortOrderAscIdAsc();
                    seedProductsFromLuxeJson(map, standardSizes);
                }
            }
        } catch (Exception e) {
            log.warn("Notice in seedProductsFromLuxeJson: {}", e.getMessage());
        }

        // Tạo phiếu nhập kho toàn diện cho tất cả các sản phẩm chưa có chứng từ nhập kho
        seedStockImportsForAllProductsIfNeeded();

        // Đồng bộ tự động các địa chỉ từ đơn hàng vào sổ địa chỉ người dùng nếu chưa có
        syncAddressesFromOrders();
    }

    private void syncAddressesFromOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            int count = 0;
            for (Order order : orders) {
                User user = order.getUser();
                if (user == null && order.getRecipientEmail() != null && !order.getRecipientEmail().isBlank()) {
                    user = userRepository.findByEmailIgnoreCase(order.getRecipientEmail().trim()).orElse(null);
                    if (user != null) {
                        order.setUser(user);
                        orderRepository.save(order);
                    }
                }
                if (user == null) {
                    continue;
                }

                String shippingAddr = order.getShippingAddress();
                if (shippingAddr == null || shippingAddr.isBlank()) {
                    continue;
                }

                List<UserAddress> existingAddresses = userAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId());
                String normalizedTarget = AddressUtils.normalize(shippingAddr);

                boolean alreadyExists = existingAddresses.stream().anyMatch(a ->
                        a.getFullAddress() != null && AddressUtils.normalize(a.getFullAddress()).equals(normalizedTarget)
                );

                if (!alreadyExists) {
                    AddressUtils.ParsedAddress parsed = AddressUtils.parseAddress(shippingAddr);
                    String recipientName = (order.getRecipientName() != null && !order.getRecipientName().isBlank())
                            ? order.getRecipientName().trim()
                            : (user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : "Khách hàng");
                    String phone = (order.getRecipientPhone() != null && !order.getRecipientPhone().isBlank())
                            ? order.getRecipientPhone().trim()
                            : (user.getPhone() != null && !user.getPhone().isBlank() ? user.getPhone() : "0900000000");

                    boolean isDefault = existingAddresses.isEmpty();
                    UserAddress newAddress = UserAddress.builder()
                            .user(user)
                            .recipientName(recipientName)
                            .phone(phone)
                            .province(parsed.getProvince())
                            .district(parsed.getDistrict())
                            .ward(parsed.getWard())
                            .streetAddress(parsed.getStreetAddress())
                            .fullAddress(parsed.getFullAddress())
                            .isDefault(isDefault)
                            .build();

                    userAddressRepository.save(newAddress);
                    count++;
                    log.info("Synced address from order {} for user {}: {}", order.getOrderCode(), user.getEmail(), parsed.getFullAddress());

                    if ((user.getPhone() == null || user.getPhone().isBlank()) && !phone.isBlank()) {
                        user.setPhone(phone);
                        userRepository.save(user);
                    }
                }
            }
            if (count > 0) {
                log.info("Finished syncing {} addresses from orders into user address books.", count);
            }
        } catch (Exception e) {
            log.warn("Notice in syncAddressesFromOrders: {}", e.getMessage(), e);
        }
    }

    private void seedStockImportsForAllProductsIfNeeded() {
        try {
            List<Long> importedVariantIds = jdbcTemplate.queryForList(
                    "SELECT DISTINCT variant_id FROM stock_import_items", Long.class);
            Set<Long> importedSet = new HashSet<>(importedVariantIds);

            List<ProductVariant> allVariants = productVariantRepository.findAll();
            List<ProductVariant> unimportedVariants = allVariants.stream()
                    .filter(v -> !importedSet.contains(v.getId()))
                    .toList();

            if (unimportedVariants.isEmpty()) {
                log.info("All product variants already have corresponding stock import receipts.");
                return;
            }

            log.info("Found {} product variants without stock import receipts. Creating batch stock import receipts...", unimportedVariants.size());

            String[] suppliers = {
                    "Xưởng May Thời Trang Cao Cấp Đông Á",
                    "Công ty Dệt May Sài Gòn LUXE",
                    "Xưởng May Veston & Dạ Hà Nội",
                    "Nhà Cung Cấp Phụ Kiện & Thiết Kế LUXE Prime"
            };
            String[] notes = {
                    "Nhập lô hàng thời trang công sở Nữ Thu Đông đợt 1",
                    "Nhập lô hàng thời trang Nam cao cấp (Polo, Sơ mi, Quần âu)",
                    "Nhập bộ sưu tập Áo khoác & Đầm thiết kế cao cấp",
                    "Nhập bổ sung toàn bộ size và màu các mẫu thiết kế mới"
            };

            int batchSize = (int) Math.ceil((double) unimportedVariants.size() / suppliers.length);
            for (int i = 0; i < suppliers.length; i++) {
                int fromIndex = i * batchSize;
                if (fromIndex >= unimportedVariants.size()) break;
                int toIndex = Math.min(fromIndex + batchSize, unimportedVariants.size());
                List<ProductVariant> batch = unimportedVariants.subList(fromIndex, toIndex);
                if (batch.isEmpty()) continue;

                String code = "NK-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + (2001 + i);
                StockImport receipt = StockImport.builder()
                        .importCode(code)
                        .supplierName(suppliers[i])
                        .note(notes[i])
                        .createdByEmail("admin@luxe.vn")
                        .build();

                for (ProductVariant v : batch) {
                    int qty = v.getStock() > 0 ? v.getStock() : 20;
                    BigDecimal cost = (v.getCostPrice() != null && v.getCostPrice().compareTo(BigDecimal.ZERO) > 0)
                            ? v.getCostPrice()
                            : (v.getPrice() != null && v.getPrice().compareTo(BigDecimal.ZERO) > 0
                                ? v.getPrice().multiply(new BigDecimal("0.55")).setScale(0, RoundingMode.HALF_UP)
                                : new BigDecimal("450000"));

                    if (v.getStock() <= 0) {
                        v.setStock(qty);
                    }
                    if (v.getCostPrice() == null || v.getCostPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        v.setCostPrice(cost);
                    }
                    productVariantRepository.save(v);

                    StockImportItem item = StockImportItem.builder()
                            .variant(v)
                            .quantity(qty)
                            .unitCost(cost)
                            .subtotal(cost.multiply(BigDecimal.valueOf(qty)))
                            .build();
                    receipt.addItem(item);
                }

                stockImportRepository.save(receipt);
            }
            log.info("Successfully seeded stock import receipts covering all {} product variants.", unimportedVariants.size());
        } catch (Exception e) {
            log.warn("Notice in seedStockImportsForAllProductsIfNeeded: {}", e.getMessage(), e);
        }
    }

    private void mergeAndRemoveSize(String fromSizeName, String toSizeName) {
        try {
            Optional<Size> fromOpt = sizeRepository.findByNameIgnoreCase(fromSizeName);
            Optional<Size> toOpt = sizeRepository.findByNameIgnoreCase(toSizeName);
            if (fromOpt.isEmpty()) {
                jdbcTemplate.update("DELETE FROM sizes WHERE UPPER(name) = ?", fromSizeName.toUpperCase());
                return;
            }

            Long fromId = fromOpt.get().getId();
            if (toOpt.isPresent()) {
                Long toId = toOpt.get().getId();
                List<Map<String, Object>> fromVariants = jdbcTemplate.queryForList(
                        "SELECT id, product_color_id, stock FROM product_variants WHERE size_id = ?", fromId);
                for (Map<String, Object> fv : fromVariants) {
                    Long fvId = ((Number) fv.get("id")).longValue();
                    Long pcId = ((Number) fv.get("product_color_id")).longValue();
                    int fvStock = ((Number) fv.get("stock")).intValue();

                    List<Map<String, Object>> toVariants = jdbcTemplate.queryForList(
                            "SELECT id, stock FROM product_variants WHERE product_color_id = ? AND size_id = ?", pcId, toId);
                    if (!toVariants.isEmpty()) {
                        Long targetId = ((Number) toVariants.get(0).get("id")).longValue();
                        jdbcTemplate.update("UPDATE product_variants SET stock = stock + ? WHERE id = ?", fvStock, targetId);
                        try {
                            jdbcTemplate.update("UPDATE order_items SET variant_id = ? WHERE variant_id = ?", targetId, fvId);
                        } catch (Exception ignored) {}
                        try {
                            jdbcTemplate.update("UPDATE stock_import_items SET variant_id = ? WHERE variant_id = ?", targetId, fvId);
                        } catch (Exception ignored) {}
                        jdbcTemplate.update("DELETE FROM product_variants WHERE id = ?", fvId);
                    } else {
                        jdbcTemplate.update("UPDATE product_variants SET size_id = ? WHERE id = ?", toId, fvId);
                    }
                }
            }
            jdbcTemplate.update("DELETE FROM sizes WHERE id = ?", fromId);
            log.info("Successfully merged size {} into {} and removed {}", fromSizeName, toSizeName, fromSizeName);
        } catch (Exception e) {
            log.warn("Error merging size {} into {}: {}", fromSizeName, toSizeName, e.getMessage());
        }
    }

    private void syncLuxeProductsData() {
        try {
            jdbcTemplate.update("UPDATE product_images SET image_url = REPLACE(image_url, '/uploads/ivy/', '/uploads/products/') WHERE image_url LIKE '%/uploads/ivy/%'");
            jdbcTemplate.update("UPDATE product_images SET image_url = REPLACE(image_url, 'ivy/', 'products/') WHERE image_url LIKE '%ivy/%'");
            jdbcTemplate.update("UPDATE products SET brand = 'LUXE' WHERE brand LIKE '%IVY%'");
            jdbcTemplate.update("UPDATE banners SET image_url = REPLACE(image_url, '/uploads/ivy/', '/uploads/products/') WHERE image_url LIKE '%/uploads/ivy/%'");
            jdbcTemplate.update("UPDATE banners SET image_url = REPLACE(image_url, 'ivy/', 'products/') WHERE image_url LIKE '%ivy/%'");
        } catch (Exception e) {
            log.warn("Notice updating legacy image paths: {}", e.getMessage());
        }

        try (InputStream is = getClass().getResourceAsStream("/seed/luxe-products.json")) {
            if (is == null) return;
            JsonNode root = objectMapper.readTree(is);
            if (!root.isArray() || root.isEmpty()) return;

            for (JsonNode pNode : root) {
                String slug = pNode.path("slug").asText();
                String desc = pNode.path("description").asText("");

                List<Long> productIds = jdbcTemplate.queryForList("SELECT id FROM products WHERE slug = ?", Long.class, slug);
                if (productIds.isEmpty()) continue;
                Long productId = productIds.get(0);

                if (desc != null && !desc.isBlank()) {
                    jdbcTemplate.update("UPDATE products SET description = ? WHERE id = ?", desc, productId);
                }

                JsonNode colorsNode = pNode.path("colors");
                if (colorsNode.isArray()) {
                    for (JsonNode cNode : colorsNode) {
                        String rawColor = cNode.path("name").asText("Mặc định").trim();
                        final String colorName = rawColor.endsWith(".") ? rawColor.substring(0, rawColor.length() - 1).trim() : rawColor;
                        JsonNode imagesNode = cNode.path("images");
                        if (!imagesNode.isArray() || imagesNode.isEmpty()) continue;

                        List<Long> pcIds = jdbcTemplate.queryForList(
                                "SELECT pc.id FROM product_colors pc " +
                                "JOIN colors c ON c.id = pc.color_id " +
                                "WHERE pc.product_id = ? AND (LOWER(c.name) = LOWER(?) OR LOWER(c.name) LIKE LOWER(?))",
                                Long.class, productId, colorName, colorName + "%");
                        if (pcIds.isEmpty()) continue;

                        for (Long pcId : pcIds) {
                            jdbcTemplate.update("DELETE FROM product_images WHERE product_color_id = ?", pcId);
                            int sortOrder = 0;
                            for (JsonNode imgNode : imagesNode) {
                                String imgUrl = imgNode.asText();
                                jdbcTemplate.update(
                                        "INSERT INTO product_images (product_color_id, image_url, display_order, created_at, updated_at) " +
                                        "VALUES (?, ?, ?, NOW(), NOW())",
                                        pcId, imgUrl, sortOrder++);
                            }
                        }
                    }
                }
            }
            log.info("Synchronized descriptions and all images from luxe-products.json into database via JDBC.");
        } catch (Exception e) {
            log.warn("Notice in syncLuxeProductsData: {}", e.getMessage(), e);
        }
    }

    private List<Product> seedProductsFromLuxeJson(
            Map<String, Category> catMap,
            List<Size> defaultSizes
    ) {
        try (InputStream is = getClass().getResourceAsStream("/seed/luxe-products.json")) {
            if (is == null) {
                log.info("luxe-products.json not found on classpath, skipping seed.");
                return List.of();
            }
            JsonNode root = objectMapper.readTree(is);
            if (!root.isArray() || root.isEmpty()) {
                return List.of();
            }

            Category defaultCategory = catMap.values().stream().findFirst().orElse(null);

            List<Product> seeded = new ArrayList<>();
            for (JsonNode pNode : root) {
                String name = pNode.path("name").asText();
                String slug = pNode.path("slug").asText();
                String sku = pNode.path("sku").asText();
                String catKey = pNode.path("categoryKey").asText();
                BigDecimal basePrice = new BigDecimal(pNode.path("basePrice").asText("0"));
                int discount = pNode.path("discountPercent").asInt(0);
                boolean isNew = pNode.path("isNew").asBoolean(false);
                boolean isBestSeller = pNode.path("isBestSeller").asBoolean(false);
                String desc = pNode.path("description").asText("");

                if (productRepository.existsBySlug(slug)) {
                    productRepository.findBySlug(slug).ifPresent(seeded::add);
                    continue;
                }

                Category category = catMap.getOrDefault(catKey, defaultCategory);

                Product product = Product.builder()
                        .category(category)
                        .name(name)
                        .slug(slug)
                        .skuPrefix(sku)
                        .brand("LUXE")
                        .basePrice(basePrice)
                        .discountPercent(discount)
                        .isNew(isNew)
                        .isBestSeller(isBestSeller)
                        .isActive(true)
                        .description(formatDescriptionToHtml(desc, name))
                        .build();

                BigDecimal costPrice = basePrice.multiply(new BigDecimal("0.50"));

                JsonNode colorsNode = pNode.path("colors");
                int colorIndex = 1;
                for (JsonNode cNode : colorsNode) {
                    String rawColor = cNode.path("name").asText("Mặc định").trim();
                    final String colorName = rawColor.endsWith(".") ? rawColor.substring(0, rawColor.length() - 1).trim() : rawColor;
                    final String hex = cNode.path("hex").asText("#111111");

                    Color color = colorRepository.findByNameIgnoreCase(colorName)
                            .orElseGet(() -> colorRepository.save(Color.builder().name(colorName).hexCode(hex).build()));

                    ProductColor pc = ProductColor.builder().color(color).isActive(true).build();

                    JsonNode imagesNode = cNode.path("images");
                    int imgOrder = 0;
                    for (JsonNode imgNode : imagesNode) {
                        pc.addImage(ProductImage.builder()
                                .imageUrl(imgNode.asText())
                                .displayOrder(imgOrder++)
                                .build());
                    }

                    JsonNode sizesNode = cNode.path("sizes");
                    if (sizesNode.isArray() && !sizesNode.isEmpty()) {
                        for (JsonNode sNode : sizesNode) {
                            String sName = sNode.path("name").asText("M").trim().toUpperCase(Locale.ROOT);
                            if ("M2".equals(sName) || "L2".equals(sName)) {
                                continue;
                            }
                            int stock = sNode.path("stock").asInt(10);
                            final String finalSizeName = sName;
                            Size size = sizeRepository.findByNameIgnoreCase(finalSizeName)
                                     .orElseGet(() -> sizeRepository.save(Size.builder().name(finalSizeName).sortOrder(5).build()));

                            pc.addVariant(ProductVariant.builder()
                                    .size(size)
                                    .sku(sku + "-C" + colorIndex + "-" + finalSizeName)
                                    .price(basePrice)
                                    .costPrice(costPrice)
                                    .stock(stock)
                                    .build());
                        }
                    } else {
                        for (Size s : defaultSizes) {
                            pc.addVariant(ProductVariant.builder()
                                    .size(s)
                                    .sku(sku + "-C" + colorIndex + "-" + s.getName())
                                    .price(basePrice)
                                    .costPrice(costPrice)
                                    .stock(10)
                                    .build());
                        }
                    }

                    product.addProductColor(pc);
                    colorIndex++;
                }

                seeded.add(productRepository.save(product));
            }

            log.info("Successfully seeded {} LUXE products into catalog.", seeded.size());
            return seeded;
        } catch (Exception e) {
            log.error("Failed to seed LUXE products from json", e);
            return List.of();
        }
    }

    private void seedHistoricalOrder(
            String orderCode, User user, Product product, int qty,
            OrderStatus status, PaymentMethod method, PaymentStatus payStatus,
            String voucherCode, BigDecimal discount, LocalDateTime createdAt
    ) {
        ProductColor pc = product.getProductColors().get(0);
        ProductVariant variant = pc.getVariants().get(1); // Size M
        BigDecimal unitPrice = product.getEffectivePrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .recipientName(user.getFullName())
                .recipientPhone(user.getPhone())
                .recipientEmail(user.getEmail())
                .shippingAddress("68 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh")
                .paymentMethod(method)
                .paymentStatus(payStatus)
                .status(status)
                .voucherCode(voucherCode)
                .build();

        order.addItem(OrderItem.builder()
                .variant(variant)
                .productId(product.getId())
                .productName(product.getName())
                .colorName(pc.getColor().getName())
                .sizeName(variant.getSize().getName())
                .sku(variant.getSku())
                .imageUrl(pc.getImages().get(0).getImageUrl())
                .quantity(qty)
                .unitPrice(unitPrice)
                .costPrice(variant.getCostPrice())
                .totalPrice(lineTotal)
                .build());

        order.recalculateTotals(new BigDecimal("30000"), discount);
        order.setCreatedAt(createdAt);
        orderRepository.save(order);
    }

    private String formatDescriptionToHtml(String raw, String productName) {
        if (raw == null || raw.isBlank()) {
            return "<p>" + productName + " mang phong cách thời trang thanh lịch, chất liệu cao cấp và tôn dáng hoàn hảo.</p>";
        }
        String text = raw.trim();

        // Tách 'Lưu ý:'
        String note = "";
        int noteIdx = text.toLowerCase(Locale.ROOT).indexOf("lưu ý:");
        if (noteIdx >= 0) {
            note = text.substring(noteIdx + 6).trim();
            text = text.substring(0, noteIdx).trim();
        }

        // Tách 'Thông tin mẫu:'
        String modelInfo = "";
        int modelIdx = text.toLowerCase(Locale.ROOT).indexOf("thông tin mẫu:");
        if (modelIdx >= 0) {
            modelInfo = text.substring(modelIdx + 14).trim();
            text = text.substring(0, modelIdx).trim();
        }

        StringBuilder html = new StringBuilder();

        // Tách gạch đầu dòng hoặc các tiêu chí
        if (text.contains(" - ") || text.startsWith("- ")) {
            String[] parts = text.split("(?:^|\\s+)-\\s+");
            List<String> validParts = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    validParts.add(trimmed);
                }
            }
            if (!validParts.isEmpty()) {
                html.append("<p>").append(validParts.get(0)).append("</p>");
                if (validParts.size() > 1) {
                    html.append("<ul>");
                    for (int i = 1; i < validParts.size(); i++) {
                        html.append("<li>").append(validParts.get(i)).append("</li>");
                    }
                    html.append("</ul>");
                }
            }
        } else if (text.contains("Phom dáng:")) {
            int introEnd = text.indexOf("Phom dáng:");
            if (introEnd > 0) {
                html.append("<p>").append(text.substring(0, introEnd).trim()).append("</p>");
            }
            String specsText = text.substring(introEnd);
            String[] specs = specsText.split("(?=(?:Phom dáng|Chi tiết|Chất liệu|Màu sắc|Phù hợp):)");
            html.append("<ul>");
            for (String s : specs) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    int colIdx = trimmed.indexOf(":");
                    if (colIdx > 0) {
                        html.append("<li><strong>")
                                .append(trimmed.substring(0, colIdx + 1))
                                .append("</strong> ")
                                .append(trimmed.substring(colIdx + 1).trim())
                                .append("</li>");
                    } else {
                        html.append("<li>").append(trimmed).append("</li>");
                    }
                }
            }
            html.append("</ul>");
        } else if (!text.isEmpty()) {
            html.append("<p>").append(text).append("</p>");
        } else {
            html.append("<p>").append(productName).append(" sở hữu thiết kế thời thượng, tôn dáng và mang lại sự tự tin cho người mặc.</p>");
        }

        if (!modelInfo.isEmpty()) {
            html.append("<p><strong>Thông tin người mẫu:</strong> ").append(modelInfo).append("</p>");
        }
        if (!note.isEmpty()) {
            html.append("<p><em>* Lưu ý: ").append(note).append("</em></p>");
        }

        return html.toString();
    }
}
