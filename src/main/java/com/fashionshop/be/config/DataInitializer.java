package com.fashionshop.be.config;

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
import com.fashionshop.be.domain.enums.DiscountType;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.PaymentMethod;
import com.fashionshop.be.domain.enums.PaymentStatus;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.domain.enums.UserStatus;
import com.fashionshop.be.domain.enums.VoucherStatus;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            sanitizeLegacyDataIfNeeded();
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

        // 2. Seed Categories (Hierarchy Nam & Nữ)
        Category catNu = categoryRepository.save(Category.builder()
                .name("Thời trang Nữ")
                .slug("nu")
                .description("Bộ sưu tập thời trang công sở & dạo phố cao cấp dành cho phái đẹp")
                .isActive(true)
                .build());

        Category catDam = categoryRepository.save(Category.builder()
                .parent(catNu).name("Đầm thiết kế").slug("dam").description("Đầm lụa, đầm công sở thanh lịch").isActive(true).build());
        Category catAoNu = categoryRepository.save(Category.builder()
                .parent(catNu).name("Áo sơ mi & Kiểu Nữ").slug("ao-nu").description("Áo sơ mi lụa, áo kiểu tinh tế").isActive(true).build());
        Category catQuanNu = categoryRepository.save(Category.builder()
                .parent(catNu).name("Quần & Chân váy Nữ").slug("quan-nu").description("Quần âu ống suông, chân váy xếp ly").isActive(true).build());

        Category catNam = categoryRepository.save(Category.builder()
                .name("Thời trang Nam")
                .slug("nam")
                .description("Trang phục lịch lãm, hiện đại dành cho quý ông")
                .isActive(true)
                .build());

        Category catAoNam = categoryRepository.save(Category.builder()
                .parent(catNam).name("Áo sơ mi & Polo Nam").slug("ao-nam").description("Áo sơ mi chống nhăn, áo polo cao cấp").isActive(true).build());
        Category catKhoacNam = categoryRepository.save(Category.builder()
                .parent(catNam).name("Áo Blazer & Khoác Nam").slug("ao-khoac-nam").description("Blazer, vest và áo khoác thu đông").isActive(true).build());
        Category catQuanNam = categoryRepository.save(Category.builder()
                .parent(catNam).name("Quần Âu & Khaki Nam").slug("quan-nam").description("Quần âu chuẩn phom công sở").isActive(true).build());

        // 3. Seed Colors & Sizes
        Color cBlack = colorRepository.save(Color.builder().name("Đen").hexCode("#111111").build());
        Color cWhite = colorRepository.save(Color.builder().name("Trắng").hexCode("#FFFFFF").build());
        Color cBeige = colorRepository.save(Color.builder().name("Kem Be").hexCode("#E8DCC8").build());
        Color cNavy = colorRepository.save(Color.builder().name("Xanh Navy").hexCode("#1B365D").build());
        Color cRed = colorRepository.save(Color.builder().name("Đỏ Ruby").hexCode("#A61C1C").build());
        Color cGrey = colorRepository.save(Color.builder().name("Xám Nhạt").hexCode("#D1D5DB").build());

        Size sS = sizeRepository.save(Size.builder().name("S").sortOrder(1).build());
        Size sM = sizeRepository.save(Size.builder().name("M").sortOrder(2).build());
        Size sL = sizeRepository.save(Size.builder().name("L").sortOrder(3).build());
        Size sXL = sizeRepository.save(Size.builder().name("XL").sortOrder(4).build());
        List<Size> standardSizes = List.of(sS, sM, sL, sXL);

        // 4. Seed 10 Realistic Fashion Products using 100% real studio .webp photos in /uploads
        Product p1 = createSeedProduct(catDam, "Đầm Lụa Cổ V Dập Ly Thanh Lịch", "dam-lua-co-v-dap-ly", "LX-W001",
                new BigDecimal("1250000"), 20, true, true,
                "Đầm lụa dáng xòe nhẹ, cổ V tinh tế tôn dáng, tay ngắn thanh lịch, eo xếp ly khéo léo che khuyết điểm. Chất liệu lụa tơ nhân tạo mềm mịn, thoáng mát cả ngày.",
                cBlack, List.of("03321697-61dc-4f30-8da7-d25085a78741.webp", "0b6e2df0-0401-4eb0-a440-1cbf06651b8d.webp"),
                cWhite, List.of("121a23af-e7f7-4e5a-8e96-65f25b8a4a3f.webp", "13dddff3-1a53-4b41-8b70-7c0b2ff37345.webp"),
                standardSizes, 12);

        Product p2 = createSeedProduct(catDam, "Đầm Midi Chiết Eo Phối Cúc Ngọc", "dam-midi-chiet-eo-cuc-ngoc", "LX-W002",
                new BigDecimal("1450000"), 30, true, true,
                "Thiết kế đầm midi công sở cao cấp với hàng cúc ngọc trai sang trọng, đường cắt may chuẩn xác tôn vòng eo thon gọn.",
                cBeige, List.of("4c6dfad6-5fac-4bf0-a37e-11af96fb7191.webp", "501350eb-e04b-4abb-b6cf-58b1cb78373d.webp"),
                cRed, List.of("837d3faa-4a85-4c73-9e48-94464f727606.webp", "82dbe045-9c96-4a78-b5fa-d69d30a1c16d.webp"),
                standardSizes, 10);

        Product p3 = createSeedProduct(catAoNu, "Áo Sơ Mi Lụa Cổ Đức Tay Phồng", "ao-so-mi-lua-co-duc-tay-phong", "LX-W003",
                new BigDecimal("690000"), 0, true, true,
                "Áo sơ mi lụa cao cấp ít nhăn, phom suông vừa dễ dàng sơ vin cùng quần âu hoặc chân váy công sở.",
                cWhite, List.of("198b0924-f7c8-463c-9459-97caf17e0e80.webp", "1a273f13-e42b-4f39-a194-69123e2ad90e.webp"),
                cBeige, List.of("2525fd0c-9394-410f-8bb0-8842184d6b60.webp", "273101e9-e223-4071-a410-5978071e1617.webp"),
                standardSizes, 15);

        Product p4 = createSeedProduct(catAoNu, "Áo Kiểu Cổ Nơ Chiffon Hàn Quốc", "ao-kieu-co-no-chiffon", "LX-W004",
                new BigDecimal("750000"), 15, true, false,
                "Áo kiểu chất liệu chiffon hai lớp mềm rũ, điểm nhấn cổ thắt nơ nữ tính phù hợp hội họp và sự kiện.",
                cBeige, List.of("28b592b1-1c56-46a4-ab6c-3715c5f17fc4.webp", "2f287da2-2ced-4613-b9e8-7d95fe92f718.webp"),
                cBlack, List.of("3142ae83-5757-43df-a666-da0566f44f25.webp", "3a33de02-d052-4e00-a0b5-a0df121d4209.webp"),
                standardSizes, 8);

        Product p5 = createSeedProduct(catQuanNu, "Quần Âu Nữ Ống Suông Cạp Cao", "quan-au-nu-ong-suong-cap-cao", "LX-W005",
                new BigDecimal("890000"), 25, false, true,
                "Quần âu ống suông lưng cao giúp kéo dài đôi chân, vải tuyết mưa dày dặn đứng phom không xù lông.",
                cBlack, List.of("3b6096f2-377c-4e81-a4ce-ce42be1690c4.webp", "3ceebb27-34c3-4550-b1d9-059cf8a751d3.webp"),
                cGrey, List.of("3ec1f27b-051c-4848-b60f-e9426f53fdd3.webp", "3ff1def3-4a16-4711-ab2d-1404932720fc.webp"),
                standardSizes, 14);

        Product p6 = createSeedProduct(catQuanNu, "Chân Váy Chữ A Xếp Ly Dáng Dài", "chan-vay-chu-a-xep-ly", "LX-W006",
                new BigDecimal("790000"), 0, true, false,
                "Chân váy chữ A cạp bản to che bụng hiệu quả, dễ phối cùng áo sơ mi hoặc áo thun tối giản.",
                cBeige, List.of("41a8ee0e-1b6c-485a-929e-a3e09cdb1601.webp", "4df71260-f448-4ad5-acb4-c4602e8ea85c.webp"),
                cBlack, List.of("4874cfa4-0d43-4455-a51b-a02f0d2b6505.webp", "48da95ec-5eb0-44ac-9845-d33d4c9c4cef.webp"),
                standardSizes, 9);

        Product p7 = createSeedProduct(catAoNam, "Áo Sơ Mi Nam Bamboo Chống Nhăn Slimfit", "ao-so-mi-nam-bamboo-slimfit", "LX-M001",
                new BigDecimal("850000"), 20, true, true,
                "Áo sơ mi sợi tre tự nhiên kháng khuẩn, thấm hút mồ hôi vượt trội, giữ phom phẳng phiu suốt ngày dài làm việc.",
                cWhite, List.of("4f6a834a-c2a0-4783-9fd7-d124f25f4a88.webp", "502977aa-82e1-4697-919e-a2321b8a461e.webp"),
                cNavy, List.of("566d6e59-052f-4745-abca-ce0a8f3ec970.webp", "60999c15-57aa-49be-9d92-a9c87cac20eb.webp"),
                standardSizes, 16);

        Product p8 = createSeedProduct(catAoNam, "Áo Polo Nam Dệt Kim Cổ Phối Viền", "ao-polo-nam-det-kim-phoi-vien", "LX-M002",
                new BigDecimal("650000"), 0, true, true,
                "Áo polo dệt kim thoáng khí, cổ bẻ cứng cáp mang phong cách Smart Casual trẻ trung, năng động.",
                cNavy, List.of("631f63ec-1a0d-445a-bd29-b3b10427d726.webp", "66ec2e51-c749-4692-8d81-2fd1245ed298.webp"),
                cBlack, List.of("677203a0-73b9-4d5d-8930-f528428c631b.webp", "6790a4ca-8a83-43be-b19f-026488664b53.webp"),
                standardSizes, 12);

        Product p9 = createSeedProduct(catKhoacNam, "Áo Khoác Blazer Nam 2 Khuy Hiện Đại", "ao-khoac-blazer-nam-2-khuy", "LX-M003",
                new BigDecimal("1890000"), 30, true, true,
                "Blazer nam phom Hàn Quốc 2 khuy lịch lãm, đệm vai nhẹ tự nhiên, lót lụa thoáng khí cao cấp.",
                cBlack, List.of("684cdd93-350c-46a5-aff4-c30b6f0f0a83.webp", "6fc16a01-2f75-4b39-952f-e15db7883ed6.webp"),
                cGrey, List.of("7041a9db-a348-4a9b-87aa-94db388766d4.webp", "7259b73a-2a9d-42c2-87f8-80d3e5e95d0f.webp"),
                standardSizes, 4);

        Product p10 = createSeedProduct(catQuanNam, "Quần Âu Nam Co Giãn Nhẹ Chuẩn Công Sở", "quan-au-nam-co-gian-cong-so", "LX-M004",
                new BigDecimal("950000"), 0, true, false,
                "Quần âu nam phom Regular Tapered gọn gàng, cạp ẩn co giãn thông minh tạo sự thoải mái tối đa khi ngồi làm việc.",
                cBlack, List.of("8ca0fa01-3f9c-4d9e-ad52-4e4559f99695.webp", "8ff32ffa-bb3a-42fc-97bc-b15d94196fdb.webp"),
                cNavy, List.of("9d02a2ea-f290-4ad6-b232-0d701ac8429a.webp", "9d4de7a2-cee8-429d-996f-3f3ad504115a.webp"),
                standardSizes, 11);

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
    }

    private void sanitizeLegacyDataIfNeeded() {
        try {
            jdbcTemplate.update("UPDATE orders SET payment_method = 'BANK_TRANSFER' WHERE payment_method = 'MOMO'");
            jdbcTemplate.update("UPDATE users SET status = 'ACTIVE' WHERE status = 'UNVERIFIED'");
            jdbcTemplate.update("UPDATE users SET gender = 'FEMALE' WHERE gender IS NULL");
            jdbcTemplate.update("UPDATE users SET date_of_birth = DATE '2000-08-15' WHERE date_of_birth IS NULL");
            jdbcTemplate.execute("DROP TABLE IF EXISTS contact_messages");

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
        } catch (Exception ignored) {
            // Safe no-op if schema differs
        }
    }

    private boolean isLegacyNonWebpUpload(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String lower = url.trim().toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return false;
        }
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }

    private Product createSeedProduct(
            Category category, String name, String slug, String skuPrefix,
            BigDecimal basePrice, int discountPercent, boolean isNew, boolean isBestSeller, String description,
            Color c1, List<String> imgs1,
            Color c2, List<String> imgs2,
            List<Size> sizes, int baseStock
    ) {
        Product product = Product.builder()
                .category(category)
                .name(name)
                .slug(slug)
                .skuPrefix(skuPrefix)
                .brand("LUXE")
                .basePrice(basePrice)
                .discountPercent(discountPercent)
                .isNew(isNew)
                .isBestSeller(isBestSeller)
                .isActive(true)
                .description(description)
                .build();

        BigDecimal defaultCost = basePrice.multiply(new BigDecimal("0.50"));

        ProductColor pc1 = ProductColor.builder().color(c1).isActive(true).build();
        for (int i = 0; i < imgs1.size(); i++) {
            pc1.addImage(ProductImage.builder().imageUrl(imgs1.get(i)).displayOrder(i).build());
        }
        for (Size s : sizes) {
            pc1.addVariant(ProductVariant.builder()
                    .size(s)
                    .sku(skuPrefix + "-C1-" + s.getName())
                    .price(basePrice)
                    .costPrice(defaultCost)
                    .stock(baseStock)
                    .build());
        }
        product.addProductColor(pc1);

        ProductColor pc2 = ProductColor.builder().color(c2).isActive(true).build();
        for (int i = 0; i < imgs2.size(); i++) {
            pc2.addImage(ProductImage.builder().imageUrl(imgs2.get(i)).displayOrder(i).build());
        }
        for (Size s : sizes) {
            pc2.addVariant(ProductVariant.builder()
                    .size(s)
                    .sku(skuPrefix + "-C2-" + s.getName())
                    .price(basePrice)
                    .costPrice(defaultCost)
                    .stock(Math.max(2, baseStock - 3))
                    .build());
        }
        product.addProductColor(pc2);

        return productRepository.save(product);
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
}
