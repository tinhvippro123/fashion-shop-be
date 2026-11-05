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
import com.fashionshop.be.repository.ReviewRepository;
import com.fashionshop.be.repository.SizeRepository;
import com.fashionshop.be.repository.StockImportRepository;
import com.fashionshop.be.repository.UserAddressRepository;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.repository.VoucherRepository;
import com.fashionshop.be.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final List<String> CLEAN_WEBP_IMAGES = List.of(
            "03321697-61dc-4f30-8da7-d25085a78741.webp",
            "0b6e2df0-0401-4eb0-a440-1cbf06651b8d.webp",
            "121a23af-e7f7-4e5a-8e96-65f25b8a4a3f.webp",
            "13dddff3-1a53-4b41-8b70-7c0b2ff37345.webp",
            "4c6dfad6-5fac-4bf0-a37e-11af96fb7191.webp",
            "501350eb-e04b-4abb-b6cf-58b1cb78373d.webp",
            "837d3faa-4a85-4c73-9e48-94464f727606.webp",
            "82dbe045-9c96-4a78-b5fa-d69d30a1c16d.webp",
            "198b0924-f7c8-463c-9459-97caf17e0e80.webp",
            "1a273f13-e42b-4f39-a194-69123e2ad90e.webp",
            "2525fd0c-9394-410f-8bb0-8842184d6b60.webp",
            "273101e9-e223-4071-a410-5978071e1617.webp",
            "28b592b1-1c56-46a4-ab6c-3715c5f17fc4.webp",
            "2f287da2-2ced-4613-b9e8-7d95fe92f718.webp",
            "3142ae83-5757-43df-a666-da0566f44f25.webp",
            "3a33de02-d052-4e00-a0b5-a0df121d4209.webp",
            "3b6096f2-377c-4e81-a4ce-ce42be1690c4.webp",
            "3ceebb27-34c3-4550-b1d9-059cf8a751d3.webp",
            "3ec1f27b-051c-4848-b60f-e9426f53fdd3.webp",
            "3ff1def3-4a16-4711-ab2d-1404932720fc.webp",
            "41a8ee0e-1b6c-485a-929e-a3e09cdb1601.webp",
            "4df71260-f448-4ad5-acb4-c4602e8ea85c.webp",
            "4874cfa4-0d43-4455-a51b-a02f0d2b6505.webp",
            "48da95ec-5eb0-44ac-9845-d33d4c9c4cef.webp",
            "4f6a834a-c2a0-4783-9fd7-d124f25f4a88.webp",
            "502977aa-82e1-4697-919e-a2321b8a461e.webp",
            "566d6e59-052f-4745-abca-ce0a8f3ec970.webp",
            "60999c15-57aa-49be-9d92-a9c87cac20eb.webp",
            "631f63ec-1a0d-445a-bd29-b3b10427d726.webp",
            "66ec2e51-c749-4692-8d81-2fd1245ed298.webp",
            "677203a0-73b9-4d5d-8930-f528428c631b.webp",
            "6790a4ca-8a83-43be-b19f-026488664b53.webp",
            "684cdd93-350c-46a5-aff4-c30b6f0f0a83.webp",
            "6fc16a01-2f75-4b39-952f-e15db7883ed6.webp",
            "7041a9db-a348-4a9b-87aa-94db388766d4.webp",
            "7259b73a-2a9d-42c2-87f8-80d3e5e95d0f.webp",
            "8ca0fa01-3f9c-4d9e-ad52-4e4559f99695.webp",
            "8ff32ffa-bb3a-42fc-97bc-b15d94196fdb.webp",
            "9d02a2ea-f290-4ad6-b232-0d701ac8429a.webp",
            "9d4de7a2-cee8-429d-996f-3f3ad504115a.webp"
    );

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductRepository productRepository;
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

        // 2. Seed Categories (Hierarchy 3 Cấp: Root (Nam/Nữ) -> Level 2 (Áo, Quần, Đầm, Chân váy) -> Level 3 (Chi tiết theo IVY moda))
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

        // 4. Seed 41 Realistic Fashion Products across 12 Subcategories from IVY moda
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
        List<Product> ivyProducts = seedProductsFromIvyJson(catMap, standardSizes);

        Product p1 = ivyProducts.get(0);
        Product p2 = ivyProducts.size() > 1 ? ivyProducts.get(1) : p1;
        Product p3 = ivyProducts.size() > 2 ? ivyProducts.get(2) : p1;
        Product p7 = ivyProducts.stream()
                .filter(p -> p.getCategory() != null && "ao-so-mi-nam".equals(p.getCategory().getSlug()))
                .findFirst()
                .orElse(p1);
        Product p9 = ivyProducts.stream()
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
                .title("ĐẦM & CHÂN VÁY")
                .subtitle("BST Đầm & Chân váy thanh lịch, sang trọng")
                .imageUrl("/uploads/ivy/002eab5bd95091694139ae9e8f046007.webp")
                .targetUrl("/danh-muc/dam-nu")
                .position(BannerPosition.FEATURED)
                .sortOrder(1)
                .isActive(true)
                .build());

        bannerRepository.save(Banner.builder()
                .title("ÁO POLO & SƠ MI NAM")
                .subtitle("Phong cách trẻ trung, chuẩn phom lịch lãm")
                .imageUrl("/uploads/ivy/9278e16772c52fe0c41b035b775a2f9c.jpg")
                .targetUrl("/danh-muc/ao-nam")
                .position(BannerPosition.FEATURED)
                .sortOrder(2)
                .isActive(true)
                .build());

        bannerRepository.save(Banner.builder()
                .title("ƯU ĐÃI ĐẶC BIỆT")
                .subtitle("Giảm giá lên đến 50%")
                .imageUrl("/uploads/ivy/0284007651a63e1aa34493daa2754ba1.webp")
                .targetUrl("/danh-muc/sale")
                .position(BannerPosition.FEATURED)
                .sortOrder(3)
                .isActive(true)
                .build());

        log.info("Initialized 5 default banners (2 HERO, 3 FEATURED).");
    }

    private void sanitizeLegacyDataIfNeeded() {
        try {
            jdbcTemplate.update("UPDATE users SET gender = 'FEMALE' WHERE gender IS NULL");
            jdbcTemplate.update("UPDATE users SET date_of_birth = DATE '2000-08-15' WHERE date_of_birth IS NULL");
            jdbcTemplate.execute("DROP TABLE IF EXISTS contact_messages");
        } catch (Exception e) {
            log.warn("Notice in user sanitize: {}", e.getMessage());
        }

        try {
            jdbcTemplate.query(
                    "SELECT id, image_url FROM product_images ORDER BY id ASC",
                    rs -> {
                        long id = rs.getLong("id");
                        String url = rs.getString("image_url");
                        if (isLegacyNonWebpUpload(url)) {
                            int idx = (int) (Math.abs(id - 1) % CLEAN_WEBP_IMAGES.size());
                            jdbcTemplate.update("UPDATE product_images SET image_url = ? WHERE id = ?",
                                    CLEAN_WEBP_IMAGES.get(idx), id);
                        }
                    }
            );

            jdbcTemplate.query(
                    "SELECT id, image_url FROM order_items ORDER BY id ASC",
                    rs -> {
                        long id = rs.getLong("id");
                        String url = rs.getString("image_url");
                        if (isLegacyNonWebpUpload(url)) {
                            int idx = (int) (Math.abs(id - 1) % CLEAN_WEBP_IMAGES.size());
                            jdbcTemplate.update("UPDATE order_items SET image_url = ? WHERE id = ?",
                                    CLEAN_WEBP_IMAGES.get(idx), id);
                        }
                    }
            );
        } catch (Exception e) {
            log.warn("Notice in webp image cleanup: {}", e.getMessage());
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

        // Đồng bộ toàn bộ mô tả HTML và hình ảnh đầy đủ từ ivy-products.json
        try {
            syncIvyProductsData();
        } catch (Exception e) {
            log.warn("Notice in syncIvyProductsData: {}", e.getMessage());
        }

        try {
            if (productRepository.count() < 20) {
                Map<String, Category> map = new java.util.HashMap<>();
                categoryRepository.findAll().forEach(c -> map.put(c.getSlug(), c));
                if (!map.isEmpty()) {
                    List<Size> standardSizes = sizeRepository.findAllByOrderBySortOrderAscIdAsc();
                    seedProductsFromIvyJson(map, standardSizes);
                }
            }
        } catch (Exception e) {
            log.warn("Notice in seedProductsFromIvyJson: {}", e.getMessage());
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

    private void syncIvyProductsData() {
        try (InputStream is = getClass().getResourceAsStream("/seed/ivy-products.json")) {
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
                        String colorName = cNode.path("name").asText("Mặc định").trim();
                        JsonNode imagesNode = cNode.path("images");
                        if (!imagesNode.isArray() || imagesNode.isEmpty()) continue;

                        List<Long> pcIds = jdbcTemplate.queryForList(
                                "SELECT pc.id FROM product_colors pc " +
                                "JOIN colors c ON c.id = pc.color_id " +
                                "WHERE pc.product_id = ? AND (LOWER(c.name) = LOWER(?) OR LOWER(c.name) LIKE LOWER(?))",
                                Long.class, productId, colorName, colorName + "%");
                        if (pcIds.isEmpty()) continue;

                        Long pcId = pcIds.get(0);
                        List<String> existingUrls = new java.util.ArrayList<>(jdbcTemplate.queryForList(
                                "SELECT image_url FROM product_images WHERE product_color_id = ?",
                                String.class, pcId));

                        int sortOrder = existingUrls.size();
                        for (JsonNode imgNode : imagesNode) {
                            String imgUrl = imgNode.asText();
                            if (!existingUrls.contains(imgUrl)) {
                                jdbcTemplate.update(
                                        "INSERT INTO product_images (product_color_id, image_url, display_order, created_at, updated_at) " +
                                        "VALUES (?, ?, ?, NOW(), NOW())",
                                        pcId, imgUrl, sortOrder++);
                                existingUrls.add(imgUrl);
                            }
                        }
                    }
                }
            }
            log.info("Synchronized descriptions and all images from ivy-products.json into database via JDBC.");
        } catch (Exception e) {
            log.warn("Notice in syncIvyProductsData: {}", e.getMessage(), e);
        }
    }

    private List<Product> seedProductsFromIvyJson(
            Map<String, Category> catMap,
            List<Size> defaultSizes
    ) {
        try (InputStream is = getClass().getResourceAsStream("/seed/ivy-products.json")) {
            if (is == null) {
                log.info("ivy-products.json not found on classpath, skipping IVY seed.");
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
                        .brand("IVY moda x LUXE")
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

            log.info("Successfully seeded {} IVY moda products into catalog.", seeded.size());
            return seeded;
        } catch (Exception e) {
            log.error("Failed to seed IVY moda products from json", e);
            return List.of();
        }
    }

    private boolean isLegacyNonWebpUpload(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String lower = url.trim().toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("ivy/")) {
            return false;
        }
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
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
