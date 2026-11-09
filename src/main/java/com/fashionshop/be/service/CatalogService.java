package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Category;
import com.fashionshop.be.domain.entity.Color;
import com.fashionshop.be.domain.entity.Product;
import com.fashionshop.be.domain.entity.ProductColor;
import com.fashionshop.be.domain.entity.ProductImage;
import com.fashionshop.be.domain.entity.ProductVariant;
import com.fashionshop.be.domain.entity.Review;
import com.fashionshop.be.domain.entity.Size;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.entity.Voucher;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.VoucherStatus;
import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ErrorCode;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.pattern.discount.DiscountStrategy;
import com.fashionshop.be.pattern.discount.DiscountStrategyFactory;
import com.fashionshop.be.repository.CategoryRepository;
import com.fashionshop.be.repository.ColorRepository;
import com.fashionshop.be.repository.OrderItemRepository;
import com.fashionshop.be.repository.ProductColorRepository;
import com.fashionshop.be.repository.ProductRepository;
import com.fashionshop.be.repository.ProductVariantRepository;
import com.fashionshop.be.repository.ReviewRepository;
import com.fashionshop.be.repository.SizeRepository;
import com.fashionshop.be.repository.VoucherRepository;
import com.fashionshop.be.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private static final Map<String, String> DEFAULT_COLOR_HEX = Map.ofEntries(
            Map.entry("đen", "#111111"),
            Map.entry("den", "#111111"),
            Map.entry("black", "#111111"),
            Map.entry("trắng", "#FFFFFF"),
            Map.entry("trang", "#FFFFFF"),
            Map.entry("white", "#FFFFFF"),
            Map.entry("trắng sữa", "#F5F5F0"),
            Map.entry("trang sua", "#F5F5F0"),
            Map.entry("kem", "#FFF4D4"),
            Map.entry("cream", "#FFF4D4"),
            Map.entry("be", "#D8C7B0"),
            Map.entry("beige", "#D8C7B0"),
            Map.entry("be vàng", "#E6C587"),
            Map.entry("be vang", "#E6C587"),
            Map.entry("be sữa", "#EFE5D5"),
            Map.entry("be sua", "#EFE5D5"),
            Map.entry("đỏ", "#C92A2A"),
            Map.entry("do", "#C92A2A"),
            Map.entry("red", "#C92A2A"),
            Map.entry("đỏ tươi", "#E02020"),
            Map.entry("do tuoi", "#E02020"),
            Map.entry("đỏ đô", "#800020"),
            Map.entry("do do", "#800020"),
            Map.entry("đỏ mận", "#5B1E31"),
            Map.entry("do man", "#5B1E31"),
            Map.entry("đỏ rượu", "#67162E"),
            Map.entry("do ruou", "#67162E"),
            Map.entry("đỏ ruby", "#A61C1C"),
            Map.entry("do ruby", "#A61C1C"),
            Map.entry("hồng", "#F2A6B3"),
            Map.entry("hong", "#F2A6B3"),
            Map.entry("pink", "#F2A6B3"),
            Map.entry("hồng phấn", "#FFC0CB"),
            Map.entry("hong phan", "#FFC0CB"),
            Map.entry("hồng pastel", "#F9D5DC"),
            Map.entry("hong pastel", "#F9D5DC"),
            Map.entry("hồng đất", "#B86B77"),
            Map.entry("hong dat", "#B86B77"),
            Map.entry("hồng cánh sen", "#D81B60"),
            Map.entry("hong canh sen", "#D81B60"),
            Map.entry("vàng", "#F5C542"),
            Map.entry("vang", "#F5C542"),
            Map.entry("yellow", "#F5C542"),
            Map.entry("vàng chanh", "#F4E04D"),
            Map.entry("vang chanh", "#F4E04D"),
            Map.entry("vàng mù tạt", "#E1AD01"),
            Map.entry("vang mu tat", "#E1AD01"),
            Map.entry("xanh navy", "#1B365D"),
            Map.entry("navy", "#1B365D"),
            Map.entry("xanh tím than", "#232B3E"),
            Map.entry("xanh tim than", "#232B3E"),
            Map.entry("tím than", "#3B234A"),
            Map.entry("tim than", "#3B234A"),
            Map.entry("xanh đen", "#0F172A"),
            Map.entry("xanh den", "#0F172A"),
            Map.entry("xanh dương", "#2563EB"),
            Map.entry("xanh duong", "#2563EB"),
            Map.entry("blue", "#2563EB"),
            Map.entry("xanh coban", "#0047AB"),
            Map.entry("xanh da trời", "#6BA4FF"),
            Map.entry("xanh da troi", "#6BA4FF"),
            Map.entry("xanh baby", "#89CFF0"),
            Map.entry("xanh rêu", "#4A5D4E"),
            Map.entry("xanh reu", "#4A5D4E"),
            Map.entry("xanh lá", "#198754"),
            Map.entry("xanh la", "#198754"),
            Map.entry("green", "#198754"),
            Map.entry("xanh bơ", "#9DB672"),
            Map.entry("xanh bo", "#9DB672"),
            Map.entry("xanh mint", "#8FE3D4"),
            Map.entry("xanh ngọc", "#20B2AA"),
            Map.entry("xanh ngoc", "#20B2AA"),
            Map.entry("xanh oliu", "#6B7A40"),
            Map.entry("xám", "#9CA3AF"),
            Map.entry("xam", "#9CA3AF"),
            Map.entry("gray", "#9CA3AF"),
            Map.entry("grey", "#9CA3AF"),
            Map.entry("xám nhạt", "#E5E7EB"),
            Map.entry("xam nhat", "#E5E7EB"),
            Map.entry("xám đậm", "#374151"),
            Map.entry("xam dam", "#374151"),
            Map.entry("ghi", "#6B7280"),
            Map.entry("nâu", "#6F4E37"),
            Map.entry("nau", "#6F4E37"),
            Map.entry("brown", "#6F4E37"),
            Map.entry("nâu tây", "#A67B5B"),
            Map.entry("nau tay", "#A67B5B"),
            Map.entry("nâu bò", "#C17737"),
            Map.entry("nau bo", "#C17737"),
            Map.entry("nâu socola", "#382216"),
            Map.entry("nau socola", "#382216"),
            Map.entry("nâu cà phê", "#4A3728"),
            Map.entry("nau ca phe", "#4A3728"),
            Map.entry("cam", "#FD7E14"),
            Map.entry("orange", "#FD7E14"),
            Map.entry("cam cháy", "#BF4611"),
            Map.entry("cam chay", "#BF4611"),
            Map.entry("cam đất", "#CF6943"),
            Map.entry("cam dat", "#CF6943"),
            Map.entry("cam san hô", "#FF7F66"),
            Map.entry("cam san ho", "#FF7F66"),
            Map.entry("tím", "#7952B3"),
            Map.entry("tim", "#7952B3"),
            Map.entry("purple", "#7952B3"),
            Map.entry("tím pastel", "#C3B1E1"),
            Map.entry("tím huế", "#581845")
    );

    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReviewRepository reviewRepository;
    private final VoucherRepository voucherRepository;
    private final OrderItemRepository orderItemRepository;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<CatalogDtos.CategoryDto> getCategoryTree() {
        return categoryRepository.findByParentIsNullOrderByIdAsc()
                .stream()
                .filter(Category::isActive)
                .map(this::toCategoryTreeDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.CategoryDto> getAllCategoriesFlat() {
        return categoryRepository.findAllByOrderByIdAsc()
                .stream()
                .map(c -> CatalogDtos.CategoryDto.builder()
                        .id(String.valueOf(c.getId()))
                        .numericId(c.getId())
                        .parentId(c.getParent() == null ? null : c.getParent().getId())
                        .parentName(c.getParent() == null ? null : c.getParent().getName())
                        .name(c.getName())
                        .slug(c.getSlug())
                        .description(c.getDescription())
                        .productCount(productRepository.countByCategoryId(c.getId()))
                        .isActive(c.isActive())
                        .status(c.isActive() ? "Hoạt động" : "Ẩn")
                        .children(List.of())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ColorDto> getAllColors() {
        return colorRepository.findAllByOrderByIdAsc().stream()
                .map(c -> CatalogDtos.ColorDto.builder().id(c.getId()).name(c.getName()).hexCode(c.getHexCode()).build())
                .toList();
    }

    @Transactional
    public CatalogDtos.ColorDto createColor(CatalogDtos.ColorDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException(ErrorCode.COLOR_NAME_EMPTY);
        }
        String trimmed = dto.getName().trim();
        if (colorRepository.findByNameIgnoreCase(trimmed).isPresent()) {
            throw new BusinessException(ErrorCode.COLOR_ALREADY_EXISTS);
        }
        String hex = (dto.getHexCode() != null && !dto.getHexCode().isBlank())
                ? dto.getHexCode().trim()
                : resolveColorHex(trimmed);

        Color color = colorRepository.save(Color.builder()
                .name(trimmed)
                .hexCode(hex)
                .build());
        return CatalogDtos.ColorDto.builder().id(color.getId()).name(color.getName()).hexCode(color.getHexCode()).build();
    }

    @Transactional
    public CatalogDtos.ColorDto updateColor(Long id, CatalogDtos.ColorDto dto) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COLOR_NOT_FOUND));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String trimmed = dto.getName().trim();
            colorRepository.findByNameIgnoreCase(trimmed)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BusinessException(ErrorCode.COLOR_ALREADY_EXISTS);
                        }
                    });
            color.setName(trimmed);
        }

        if (dto.getHexCode() != null && !dto.getHexCode().isBlank()) {
            color.setHexCode(dto.getHexCode().trim());
        }

        Color updated = colorRepository.save(color);
        return CatalogDtos.ColorDto.builder().id(updated.getId()).name(updated.getName()).hexCode(updated.getHexCode()).build();
    }

    @Transactional
    public void deleteColor(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COLOR_NOT_FOUND));
        if (productColorRepository.existsByColorId(id)) {
            throw new BusinessException(ErrorCode.COLOR_IN_USE);
        }
        colorRepository.delete(color);
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.SizeDto> getAllSizes() {
        return sizeRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(s -> CatalogDtos.SizeDto.builder().id(s.getId()).name(s.getName()).sortOrder(s.getSortOrder()).build())
                .toList();
    }

    @Transactional
    public CatalogDtos.SizeDto createSize(CatalogDtos.SizeDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException(ErrorCode.SIZE_NAME_EMPTY);
        }
        String trimmed = dto.getName().trim().toUpperCase(Locale.ROOT);
        if (sizeRepository.findByNameIgnoreCase(trimmed).isPresent()) {
            throw new BusinessException(ErrorCode.SIZE_ALREADY_EXISTS);
        }
        int order = dto.getSortOrder() > 0 ? dto.getSortOrder() : 10;
        Size size = sizeRepository.save(Size.builder().name(trimmed).sortOrder(order).build());
        return CatalogDtos.SizeDto.builder().id(size.getId()).name(size.getName()).sortOrder(size.getSortOrder()).build();
    }

    @Transactional
    public CatalogDtos.SizeDto updateSize(Long id, CatalogDtos.SizeDto dto) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SIZE_NOT_FOUND));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String trimmed = dto.getName().trim().toUpperCase(Locale.ROOT);
            sizeRepository.findByNameIgnoreCase(trimmed)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BusinessException(ErrorCode.SIZE_ALREADY_EXISTS);
                        }
                    });
            size.setName(trimmed);
        }
        if (dto.getSortOrder() > 0) {
            size.setSortOrder(dto.getSortOrder());
        }

        Size updated = sizeRepository.save(size);
        return CatalogDtos.SizeDto.builder().id(updated.getId()).name(updated.getName()).sortOrder(updated.getSortOrder()).build();
    }

    @Transactional
    public void deleteSize(Long id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SIZE_NOT_FOUND));
        if (productVariantRepository.existsBySizeId(id)) {
            throw new BusinessException(ErrorCode.SIZE_IN_USE);
        }
        sizeRepository.delete(size);
    }

    @Transactional(readOnly = true)
    public CatalogDtos.OptionSuggestionsDto getOptionSuggestions() {
        List<String> colorNames = colorRepository.findAllByOrderByIdAsc().stream().map(Color::getName).toList();
        List<String> sizeNames = sizeRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(Size::getName).toList();
        Map<String, List<String>> values = new LinkedHashMap<>();
        values.put("Màu sắc", colorNames);
        values.put("Kích thước", sizeNames);
        return CatalogDtos.OptionSuggestionsDto.builder()
                .names(List.of("Màu sắc", "Kích thước"))
                .values(values)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ProductResponseDto> filterPublicProducts(
            String categorySlug,
            String keyword,
            String color,
            String size,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isNew,
            Boolean isBestSeller,
            Boolean onSale,
            String sort
    ) {
        List<Product> activeProducts = productRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        Set<Long> matchingCategoryIds = resolveCategoryFilterIds(categorySlug);
        boolean filterNuNew = "hang-nu-moi-ve".equalsIgnoreCase(categorySlug);
        boolean filterNamNew = "hang-nam-moi-ve".equalsIgnoreCase(categorySlug);
        boolean filterNuSale = "sale-nu".equalsIgnoreCase(categorySlug);
        boolean filterNamSale = "sale-nam".equalsIgnoreCase(categorySlug);
        boolean filterSaleAll = "sale".equalsIgnoreCase(categorySlug) || "san-pham-giam-gia".equalsIgnoreCase(categorySlug);

        Map<Long, ProductReviewStat> reviewStats = getReviewStatsMap();
        Map<Long, Integer> soldMap = getSoldMap();

        List<CatalogDtos.ProductResponseDto> filtered = activeProducts.stream()
                .filter(p -> {
                    if (filterNuNew) {
                        return p.isNew() && belongsToRootSlug(p.getCategory(), "nu");
                    }
                    if (filterNamNew) {
                        return p.isNew() && belongsToRootSlug(p.getCategory(), "nam");
                    }
                    if (filterNuSale) {
                        return p.getDiscountPercent() > 0 && belongsToRootSlug(p.getCategory(), "nu");
                    }
                    if (filterNamSale) {
                        return p.getDiscountPercent() > 0 && belongsToRootSlug(p.getCategory(), "nam");
                    }
                    if (filterSaleAll) {
                        return p.getDiscountPercent() > 0;
                    }
                    if (!matchingCategoryIds.isEmpty()) {
                        return matchingCategoryIds.contains(p.getCategory().getId());
                    }
                    return categorySlug == null || categorySlug.isBlank() || "all".equalsIgnoreCase(categorySlug);
                })
                .filter(p -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String normKw = SlugUtils.normalizeSearchText(keyword);
                    String normName = SlugUtils.normalizeSearchText(p.getName());
                    String normCat = p.getCategory() != null ? SlugUtils.normalizeSearchText(p.getCategory().getName()) : "";
                    String normSku = p.getSkuPrefix() != null ? p.getSkuPrefix().toLowerCase(Locale.ROOT) : "";

                    // 1. Khớp cụm từ chính xác trong Tên sản phẩm, Tên danh mục hoặc Mã SKU
                    if (normName.contains(normKw) || normCat.contains(normKw) || normSku.contains(normKw)) {
                        return true;
                    }

                    // 2. Nếu từ khóa gồm nhiều từ (VD: "áo sơ mi trắng"): Tất cả các từ con phải cùng xuất hiện
                    String[] tokens = normKw.split("\\s+");
                    if (tokens.length > 1) {
                        String combined = normName + " " + normCat;
                        boolean allTokensMatch = true;
                        for (String token : tokens) {
                            if (!combined.contains(token)) {
                                allTokensMatch = false;
                                break;
                            }
                        }
                        return allTokensMatch;
                    }

                    return false;
                })
                .filter(p -> {
                    if (color == null || color.isBlank()) return true;
                    return p.getProductColors().stream()
                            .anyMatch(pc -> pc.getColor().getName().equalsIgnoreCase(color.trim()));
                })
                .filter(p -> {
                    if (size == null || size.isBlank()) return true;
                    return p.getProductColors().stream()
                            .flatMap(pc -> pc.getVariants().stream())
                            .anyMatch(v -> v.getSize().getName().equalsIgnoreCase(size.trim()) && v.getStock() > 0);
                })
                .filter(p -> minPrice == null || p.getEffectivePrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getEffectivePrice().compareTo(maxPrice) <= 0)
                .filter(p -> isNew == null || p.isNew() == isNew)
                .filter(p -> isBestSeller == null || p.isBestSeller() == isBestSeller)
                .filter(p -> onSale == null || (!onSale || p.getDiscountPercent() > 0))
                .map(p -> {
                    ProductReviewStat stat = reviewStats.get(p.getId());
                    double rating = stat != null ? stat.avgRating : 5.0;
                    int count = stat != null ? stat.reviewCount : 0;
                    return toProductDto(p, false, rating, count, soldMap.getOrDefault(p.getId(), 0));
                })
                .collect(Collectors.toCollection(ArrayList::new));

        if ("price_asc".equalsIgnoreCase(sort) || "price-asc".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparing(CatalogDtos.ProductResponseDto::getEffectivePrice));
        } else if ("price_desc".equalsIgnoreCase(sort) || "price-desc".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparing(CatalogDtos.ProductResponseDto::getEffectivePrice).reversed());
        } else if ("best_seller".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparingInt(CatalogDtos.ProductResponseDto::getSold).reversed());
        } else if (keyword != null && !keyword.isBlank()) {
            final String normKw = SlugUtils.normalizeSearchText(keyword);
            filtered.sort((a, b) -> {
                String normA = SlugUtils.normalizeSearchText(a.getName());
                String normB = SlugUtils.normalizeSearchText(b.getName());
                boolean aExact = normA.contains(normKw);
                boolean bExact = normB.contains(normKw);
                if (aExact && !bExact) return -1;
                if (!aExact && bExact) return 1;

                boolean aStart = normA.startsWith(normKw);
                boolean bStart = normB.startsWith(normKw);
                if (aStart && !bStart) return -1;
                if (!aStart && bStart) return 1;

                return 0;
            });
        }

        return filtered;
    }

    public static String buildProductSlug(Product p) {
        if (p == null) return "";
        String baseSlug = p.getSlug() != null ? p.getSlug().trim() : "";
        if (p.getSkuPrefix() != null && !p.getSkuPrefix().isBlank()) {
            String cleanSku = p.getSkuPrefix().trim().toLowerCase().replaceAll("[^a-z0-9_-]", "");
            return baseSlug + "-ms-" + cleanSku;
        }
        return baseSlug;
    }

    @Transactional(readOnly = true)
    public CatalogDtos.ProductResponseDto getProductBySlug(String slug) {
        if (slug == null || slug.isBlank() || slug.trim().matches("^\\d+$")) {
            throw new ResourceNotFoundException(ErrorCode.INVALID_PRODUCT_SLUG);
        }
        String clean = slug.trim().toLowerCase();

        // 1. Thử tìm chính xác theo slug
        Optional<Product> productOpt = productRepository.findBySlug(clean);

        // 2. Nếu slug có dạng [base-slug]-ms-[sku] (chuẩn định dạng mã sản phẩm)
        if (productOpt.isEmpty() && clean.contains("-ms-")) {
            int msIdx = clean.lastIndexOf("-ms-");
            String baseSlug = clean.substring(0, msIdx);
            String skuPart = clean.substring(msIdx + 4);

            productOpt = productRepository.findBySlug(baseSlug);
            if (productOpt.isEmpty() && !skuPart.isBlank()) {
                productOpt = productRepository.findFirstBySkuPrefixIgnoreCase(skuPart);
            }
        }

        // 3. Nếu vẫn không thấy, thử tìm theo SKU trực tiếp
        if (productOpt.isEmpty()) {
            productOpt = productRepository.findFirstBySkuPrefixIgnoreCase(clean);
        }

        Product product = productOpt.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return toProductDto(product, true);
    }

    @Transactional(readOnly = true)
    public CatalogDtos.ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return toProductDto(product, true);
    }

    @Transactional(readOnly = true)
    public CatalogDtos.ProductResponseDto getProductDetail(String idOrSlug) {
        return getProductBySlug(idOrSlug);
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ReviewDto> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toReviewDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogDtos.ReviewEligibilityDto getReviewEligibility(Long productId) {
        Optional<User> userOpt = securityUtils.getCurrentUserOptional();
        if (userOpt.isEmpty()) {
            return CatalogDtos.ReviewEligibilityDto.builder()
                    .canReview(false)
                    .purchased(false)
                    .reviewed(false)
                    .reason("NOT_LOGGED_IN")
                    .build();
        }
        User user = userOpt.get();
        boolean reviewed = reviewRepository.existsByUserIdAndProductId(user.getId(), productId);
        if (reviewed) {
            return CatalogDtos.ReviewEligibilityDto.builder()
                    .canReview(false)
                    .purchased(true)
                    .reviewed(true)
                    .reason("ALREADY_REVIEWED")
                    .build();
        }

        boolean purchased = orderItemRepository.hasUserPurchasedProduct(user.getId(), productId, OrderStatus.COMPLETED);
        if (!purchased) {
            return CatalogDtos.ReviewEligibilityDto.builder()
                    .canReview(false)
                    .purchased(false)
                    .reviewed(false)
                    .reason("NOT_PURCHASED")
                    .build();
        }

        return CatalogDtos.ReviewEligibilityDto.builder()
                .canReview(true)
                .purchased(true)
                .reviewed(false)
                .reason(null)
                .build();
    }

    @Transactional
    public CatalogDtos.ReviewDto addProductReview(Long productId, CatalogDtos.CreateReviewRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        boolean purchased = orderItemRepository.hasUserPurchasedProduct(user.getId(), productId, OrderStatus.COMPLETED);
        if (!purchased) {
            throw new BusinessException(ErrorCode.REVIEW_PURCHASE_REQUIRED);
        }

        boolean reviewed = reviewRepository.existsByUserIdAndProductId(user.getId(), productId);
        if (reviewed) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .isVisible(true)
                .build();

        return toReviewDto(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.VoucherDto> getActiveVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findByStatusOrderByCreatedAtDesc(VoucherStatus.ACTIVE)
                .stream()
                .filter(v -> (v.getStartDate() == null || !now.isBefore(v.getStartDate()))
                        && (v.getEndDate() == null || !now.isAfter(v.getEndDate()))
                        && (v.getUsageLimit() == null || v.getUsedCount() < v.getUsageLimit()))
                .map(CatalogService::toVoucherDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDtos.ValidateVoucherResponse validateVoucher(OrderDtos.ValidateVoucherRequest request) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(request.getCode().trim())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOUCHER_NOT_FOUND));

        voucher.validateApplicable(request.getSubtotal(), LocalDateTime.now());
        DiscountStrategy strategy = discountStrategyFactory.getStrategy(voucher.getDiscountType());
        BigDecimal discount = strategy.calculateDiscount(request.getSubtotal(), voucher);
        BigDecimal finalSubtotal = request.getSubtotal().subtract(discount).max(BigDecimal.ZERO);

        return OrderDtos.ValidateVoucherResponse.builder()
                .code(voucher.getCode())
                .name(voucher.getName())
                .discountAmount(discount)
                .subtotal(request.getSubtotal())
                .finalSubtotal(finalSubtotal)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ProductResponseDto> getAllAdminProducts() {
        Map<Long, ProductReviewStat> reviewStats = getReviewStatsMap();
        Map<Long, Integer> soldMap = getSoldMap();
        return productRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(p -> {
                    ProductReviewStat stat = reviewStats.get(p.getId());
                    double rating = stat != null ? stat.avgRating : 5.0;
                    int count = stat != null ? stat.reviewCount : 0;
                    return toProductDto(p, true, rating, count, soldMap.getOrDefault(p.getId(), 0));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.VariantDto> getAllVariantsForImport() {
        return productVariantRepository.findAll().stream()
                .map(v -> {
                    ProductColor pc = v.getProductColor();
                    Product p = pc.getProduct();
                    String img = pc.getImages().isEmpty() ? null : pc.getImages().get(0).getImageUrl();
                    return CatalogDtos.VariantDto.builder()
                            .id(v.getId())
                            .sku(v.getSku())
                            .price(v.getPrice())
                            .costPrice(v.getCostPrice())
                            .stock(v.getStock())
                            .size(CatalogDtos.SizeDto.builder().id(v.getSize().getId()).name(v.getSize().getName()).sortOrder(v.getSize().getSortOrder()).build())
                            .colorName(pc.getColor().getName())
                            .hexCode(pc.getColor().getHexCode())
                            .productId(p.getId())
                            .productName(p.getName())
                            .imageUrl(img)
                            .build();
                })
                .toList();
    }

    @Transactional
    public CatalogDtos.CategoryDto createCategory(CatalogDtos.CategoryUpsertRequest request) {
        String slug = (request.getSlug() == null || request.getSlug().isBlank())
                ? SlugUtils.toSlug(request.getName())
                : SlugUtils.toSlug(request.getSlug());
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + (System.currentTimeMillis() % 10000);
        }
        Category parent = request.getParentId() == null ? null :
                categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARENT_CATEGORY_NOT_FOUND));

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .parent(parent)
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();
        Category saved = categoryRepository.save(category);
        return toCategoryTreeDto(saved);
    }

    @Transactional
    public CatalogDtos.CategoryDto updateCategory(Long id, CatalogDtos.CategoryUpsertRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setName(request.getName().trim());
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            category.setSlug(SlugUtils.toSlug(request.getSlug()));
        }
        category.setDescription(request.getDescription());
        if (request.getParentId() != null && !request.getParentId().equals(id)) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARENT_CATEGORY_NOT_FOUND));
            category.setParent(parent);
        } else if (request.getParentId() == null) {
            category.setParent(null);
        }
        if (request.getIsActive() != null) {
            category.setActive(request.getIsActive());
        }
        return toCategoryTreeDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }
        if (productRepository.countByCategoryId(id) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_PRODUCTS);
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public CatalogDtos.ProductResponseDto createAdminProduct(CatalogDtos.AdminProductUpsertRequest request) {
        Category category = resolveCategoryByIdOrSlug(request.getCategory());
        String baseSlug = SlugUtils.toSlug(request.getName());
        String slug = productRepository.existsBySlug(baseSlug)
                ? baseSlug + "-" + (System.currentTimeMillis() % 10000)
                : baseSlug;

        boolean active = !"draft".equalsIgnoreCase(request.getStatus());
        int discountPercent = request.getDiscountPercent() == null
                ? 0
                : Math.min(100, Math.max(0, request.getDiscountPercent()));
        Product product = Product.builder()
                .category(category)
                .name(request.getName().trim())
                .slug(slug)
                .skuPrefix((request.getSku() == null || request.getSku().isBlank())
                        ? "LUXE-" + (System.currentTimeMillis() % 100000)
                        : request.getSku().trim())
                .brand((request.getBrand() == null || request.getBrand().isBlank()) ? "LUXE" : request.getBrand().trim())
                .basePrice(request.getPrice())
                .discountPercent(discountPercent)
                .description(request.getDescription())
                .material(request.getMaterial())
                .careInstructions(request.getCareInstructions())
                .isNew(request.getIsNew() == null || request.getIsNew())
                .isBestSeller(Boolean.TRUE.equals(request.getIsBestSeller()))
                .isActive(active)
                .build();

        populateColorsAndVariants(product, request);
        Product saved = productRepository.save(product);
        return toProductDto(saved, true);
    }

    @Transactional
    public CatalogDtos.ProductResponseDto updateAdminProduct(Long id, CatalogDtos.AdminProductUpsertRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        Category category = resolveCategoryByIdOrSlug(request.getCategory());
        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setBasePrice(request.getPrice());
        if (request.getDiscountPercent() != null) {
            product.setDiscountPercent(Math.min(100, Math.max(0, request.getDiscountPercent())));
        }
        product.setDescription(request.getDescription());
        product.setMaterial(request.getMaterial());
        product.setCareInstructions(request.getCareInstructions());
        if (request.getBrand() != null && !request.getBrand().isBlank()) {
            product.setBrand(request.getBrand().trim());
        }
        if (request.getSku() != null && !request.getSku().isBlank()) {
            product.setSkuPrefix(request.getSku().trim());
        }
        if (request.getIsNew() != null) {
            product.setNew(request.getIsNew());
        }
        if (request.getIsBestSeller() != null) {
            product.setBestSeller(request.getIsBestSeller());
        }
        if (request.getStatus() != null) {
            product.setActive(!"draft".equalsIgnoreCase(request.getStatus()));
        }

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            mergeColorsAndVariantsOnUpdate(product, request);
        } else if (request.getImages() != null && !request.getImages().isEmpty() && !product.getProductColors().isEmpty()) {
            ProductColor firstColor = product.getProductColors().get(0);
            firstColor.getImages().clear();
            int ord = 0;
            for (String img : request.getImages()) {
                if (img != null && !img.isBlank()) {
                    firstColor.addImage(ProductImage.builder().imageUrl(img.trim()).displayOrder(ord++).build());
                }
            }
        }

        return toProductDto(productRepository.save(product), true);
    }

    @Transactional
    public void deleteAdminProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setActive(false);
        productRepository.save(product);
    }

    private String extractOptionValue(Map<String, String> opts, String type) {
        if (opts == null || opts.isEmpty()) return null;
        for (Map.Entry<String, String> entry : opts.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            String lowerKey = entry.getKey().trim().toLowerCase(Locale.ROOT);
            if ("color".equals(type)) {
                if (lowerKey.contains("màu") || lowerKey.contains("mau") || lowerKey.contains("color")) {
                    return entry.getValue().trim();
                }
            } else if ("size".equals(type)) {
                if (lowerKey.contains("kích") || lowerKey.contains("kich") || lowerKey.contains("size")) {
                    return entry.getValue().trim();
                }
            }
        }
        return null;
    }

    private List<String> findColorImagesCaseInsensitive(Map<String, List<String>> colorImages, String colorName) {
        if (colorImages == null || colorName == null) return null;
        List<String> exact = colorImages.get(colorName);
        if (exact != null && !exact.isEmpty()) return exact;
        for (Map.Entry<String, List<String>> entry : colorImages.entrySet()) {
            if (entry.getKey() != null && entry.getKey().trim().equalsIgnoreCase(colorName.trim())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void populateColorsAndVariants(Product product, CatalogDtos.AdminProductUpsertRequest request) {
        List<String> fallbackImages = (request.getImages() == null || request.getImages().isEmpty())
                ? List.of()
                : request.getImages();

        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            Color defaultColor = getOrCreateColor("Đen");
            Size defaultSize = getOrCreateSize("M");
            ProductColor pc = ProductColor.builder().color(defaultColor).isActive(true).build();
            int order = 0;
            for (String img : fallbackImages) {
                pc.addImage(ProductImage.builder().imageUrl(img).displayOrder(order++).build());
            }
            String sku = ensureUniqueSku(product.getSkuPrefix() + "-BLK-M");
            pc.addVariant(ProductVariant.builder()
                    .size(defaultSize)
                    .sku(sku)
                    .price(request.getPrice())
                    .costPrice(request.getPrice().multiply(new BigDecimal("0.55")))
                    .stock(request.getStock() == null ? 10 : request.getStock())
                    .build());
            product.addProductColor(pc);
            return;
        }

        Map<String, ProductColor> colorGroupMap = new LinkedHashMap<>();
        int index = 1;
        for (CatalogDtos.AdminVariantPayloadDto vReq : request.getVariants()) {
            Map<String, String> opts = vReq.getOptions() == null ? Map.of() : vReq.getOptions();
            String extractedColor = extractOptionValue(opts, "color");
            String colorName = (extractedColor != null && !extractedColor.isBlank()) ? extractedColor : "Mặc định";
            String extractedSize = extractOptionValue(opts, "size");
            String sizeName = (extractedSize != null && !extractedSize.isBlank()) ? extractedSize : "Freesize";

            ProductColor pc = colorGroupMap.computeIfAbsent(colorName, cn -> {
                Color color = getOrCreateColor(cn);
                ProductColor created = ProductColor.builder().color(color).isActive(true).build();
                List<String> specificImgs = findColorImagesCaseInsensitive(request.getColorImages(), cn);
                if (specificImgs != null && !specificImgs.isEmpty()) {
                    int ord = 0;
                    for (String img : specificImgs) {
                        if (img != null && !img.isBlank()) {
                            created.addImage(ProductImage.builder().imageUrl(img.trim()).displayOrder(ord++).build());
                        }
                    }
                } else if (vReq.getImage() != null && !vReq.getImage().isBlank()) {
                    created.addImage(ProductImage.builder().imageUrl(vReq.getImage().trim()).displayOrder(0).build());
                } else {
                    int ord = 0;
                    for (String img : fallbackImages) {
                        created.addImage(ProductImage.builder().imageUrl(img).displayOrder(ord++).build());
                    }
                }
                product.addProductColor(created);
                return created;
            });

            if (vReq.getImage() != null && !vReq.getImage().isBlank()
                    && pc.getImages().stream().noneMatch(i -> i.getImageUrl().equals(vReq.getImage().trim()))) {
                pc.addImage(ProductImage.builder()
                        .imageUrl(vReq.getImage().trim())
                        .displayOrder(pc.getImages().size())
                        .build());
            }

            Size size = getOrCreateSize(sizeName);
            String candidateSku = (vReq.getSku() == null || vReq.getSku().isBlank())
                    ? product.getSkuPrefix() + "-" + index
                    : vReq.getSku().trim();
            index++;

            pc.addVariant(ProductVariant.builder()
                    .size(size)
                    .sku(ensureUniqueSku(candidateSku))
                    .price(vReq.getPrice() == null ? request.getPrice() : vReq.getPrice())
                    .costPrice(vReq.getCostPrice() == null
                            ? request.getPrice().multiply(new BigDecimal("0.55"))
                            : vReq.getCostPrice())
                    .stock(Math.max(0, vReq.getStock()))
                    .build());
        }
    }

    private void mergeColorsAndVariantsOnUpdate(Product product, CatalogDtos.AdminProductUpsertRequest request) {
        List<String> fallbackImages = (request.getImages() == null || request.getImages().isEmpty())
                ? List.of()
                : request.getImages();

        Map<String, ProductColor> existingColorsByLowerName = new LinkedHashMap<>();
        for (ProductColor pc : product.getProductColors()) {
            existingColorsByLowerName.put(pc.getColor().getName().toLowerCase(Locale.ROOT), pc);
        }

        Set<String> updatedColorImages = new LinkedHashSet<>();
        int index = 1;
        for (CatalogDtos.AdminVariantPayloadDto vReq : request.getVariants()) {
            Map<String, String> opts = vReq.getOptions() == null ? Map.of() : vReq.getOptions();
            String extractedColor = extractOptionValue(opts, "color");
            String colorName = ((extractedColor != null && !extractedColor.isBlank()) ? extractedColor : "Mặc định").trim();
            String extractedSize = extractOptionValue(opts, "size");
            String sizeName = ((extractedSize != null && !extractedSize.isBlank()) ? extractedSize : "Freesize").trim();
            String colorKey = colorName.toLowerCase(Locale.ROOT);

            ProductColor pc = existingColorsByLowerName.get(colorKey);
            if (pc == null) {
                Color color = getOrCreateColor(colorName);
                pc = ProductColor.builder().color(color).isActive(true).build();
                int ord = 0;
                List<String> specificImgs = findColorImagesCaseInsensitive(request.getColorImages(), colorName);
                if (specificImgs != null && !specificImgs.isEmpty()) {
                    for (String img : specificImgs) {
                        if (img != null && !img.isBlank()) {
                            pc.addImage(ProductImage.builder().imageUrl(img.trim()).displayOrder(ord++).build());
                        }
                    }
                } else if (vReq.getImage() != null && !vReq.getImage().isBlank()) {
                    pc.addImage(ProductImage.builder().imageUrl(vReq.getImage().trim()).displayOrder(ord++).build());
                } else {
                    for (String img : fallbackImages) {
                        pc.addImage(ProductImage.builder().imageUrl(img).displayOrder(ord++).build());
                    }
                }
                product.addProductColor(pc);
                existingColorsByLowerName.put(colorKey, pc);
                updatedColorImages.add(colorKey);
            } else if (!updatedColorImages.contains(colorKey)) {
                updatedColorImages.add(colorKey);
                pc.setActive(true);
                List<String> specificImgs = findColorImagesCaseInsensitive(request.getColorImages(), colorName);
                if (specificImgs != null && !specificImgs.isEmpty()) {
                    pc.getImages().clear();
                    int ord = 0;
                    for (String img : specificImgs) {
                        if (img != null && !img.isBlank()) {
                            pc.addImage(ProductImage.builder().imageUrl(img.trim()).displayOrder(ord++).build());
                        }
                    }
                } else if (pc.getImages().isEmpty() && request.getImages() != null && !request.getImages().isEmpty()) {
                    int ord = 0;
                    for (String img : request.getImages()) {
                        if (img != null && !img.isBlank()) {
                            pc.addImage(ProductImage.builder().imageUrl(img.trim()).displayOrder(ord++).build());
                        }
                    }
                }
            }

            if (vReq.getImage() != null && !vReq.getImage().isBlank()) {
                String trimmedImg = vReq.getImage().trim();
                if (pc.getImages().stream().noneMatch(i -> i.getImageUrl().equals(trimmedImg))) {
                    pc.addImage(ProductImage.builder()
                            .imageUrl(trimmedImg)
                            .displayOrder(pc.getImages().size())
                            .build());
                }
            }

            Size size = getOrCreateSize(sizeName);
            String candidateSku = (vReq.getSku() == null || vReq.getSku().isBlank())
                    ? product.getSkuPrefix() + "-" + index
                    : vReq.getSku().trim();
            index++;

            ProductVariant matchedVariant = null;
            if (vReq.getId() != null) {
                for (ProductVariant ev : pc.getVariants()) {
                    if (vReq.getId().equals(ev.getId())) {
                        matchedVariant = ev;
                        break;
                    }
                }
            }
            if (matchedVariant == null) {
                for (ProductVariant ev : pc.getVariants()) {
                    if (ev.getSize().getName().equalsIgnoreCase(size.getName())) {
                        matchedVariant = ev;
                        break;
                    }
                }
            }

            if (matchedVariant != null) {
                matchedVariant.setSize(size);
                if (!matchedVariant.getSku().equalsIgnoreCase(candidateSku)) {
                    matchedVariant.setSku(ensureUniqueSku(candidateSku));
                }
                matchedVariant.setPrice(vReq.getPrice() == null ? request.getPrice() : vReq.getPrice());
                if (vReq.getCostPrice() != null) {
                    matchedVariant.setCostPrice(vReq.getCostPrice());
                } else if (matchedVariant.getCostPrice() == null) {
                    matchedVariant.setCostPrice(request.getPrice().multiply(new BigDecimal("0.55")));
                }
                matchedVariant.setStock(Math.max(0, vReq.getStock()));
            } else {
                pc.addVariant(ProductVariant.builder()
                        .size(size)
                        .sku(ensureUniqueSku(candidateSku))
                        .price(vReq.getPrice() == null ? request.getPrice() : vReq.getPrice())
                        .costPrice(vReq.getCostPrice() == null
                                ? request.getPrice().multiply(new BigDecimal("0.55"))
                                : vReq.getCostPrice())
                        .stock(Math.max(0, vReq.getStock()))
                        .build());
            }
        }
    }

    private String ensureUniqueSku(String candidate) {
        if (!productVariantRepository.existsBySkuIgnoreCase(candidate)) {
            return candidate;
        }
        return candidate + "-" + (System.currentTimeMillis() % 10000);
    }

    private String resolveColorHex(String name) {
        if (name == null || name.isBlank()) return "#111111";
        String clean = name.trim().toLowerCase(Locale.ROOT);
        if (DEFAULT_COLOR_HEX.containsKey(clean)) {
            return DEFAULT_COLOR_HEX.get(clean);
        }

        // 1. Be - Kem
        if (clean.contains("be vàng") || clean.contains("be vang")) return "#E6C587";
        if (clean.contains("be sữa") || clean.contains("be sua")) return "#EFE5D5";
        if (clean.contains("kem") || clean.contains("cream")) return "#FFF4D4";
        if (clean.contains("be") || clean.contains("beige")) return "#D8C7B0";

        // 2. Đỏ - Hồng
        if (clean.contains("đỏ đô") || clean.contains("do do") || clean.contains("ruby")) return "#800020";
        if (clean.contains("đỏ mận") || clean.contains("do man") || clean.contains("rượu") || clean.contains("ruou")) return "#5B1E31";
        if (clean.contains("hồng đất") || clean.contains("hong dat")) return "#B86B77";
        if (clean.contains("hồng phấn") || clean.contains("hong phan")) return "#FFC0CB";
        if (clean.contains("hồng pastel") || clean.contains("hong pastel")) return "#F9D5DC";
        if (clean.contains("cánh sen") || clean.contains("canh sen")) return "#D81B60";
        if (clean.contains("hồng") || clean.contains("hong") || clean.contains("pink")) return "#F2A6B3";
        if (clean.contains("đỏ") || clean.contains("do") || clean.contains("red")) return "#C92A2A";

        // 3. Xanh dương & Tối
        if (clean.contains("xanh tím than") || clean.contains("xanh tim than")) return "#232B3E";
        if (clean.contains("tím than") || clean.contains("tim than")) return "#3B234A";
        if (clean.contains("xanh đen") || clean.contains("xanh den")) return "#0F172A";
        if (clean.contains("navy")) return "#1B365D";
        if (clean.contains("coban") || clean.contains("cobalt")) return "#0047AB";
        if (clean.contains("da trời") || clean.contains("da troi") || clean.contains("baby")) return "#6BA4FF";

        // 4. Xanh lá & Rêu
        if (clean.contains("xanh bơ") || clean.contains("xanh bo")) return "#9DB672";
        if (clean.contains("xanh mint") || clean.contains("mint")) return "#8FE3D4";
        if (clean.contains("xanh ngọc") || clean.contains("ngoc")) return "#20B2AA";
        if (clean.contains("rêu") || clean.contains("reu") || clean.contains("oliu")) return "#4A5D4E";
        if (clean.contains("lá") || clean.contains("la")) return "#198754";
        if (clean.contains("xanh") || clean.contains("blue")) return "#2563EB";

        // 5. Nâu
        if (clean.contains("nâu tây") || clean.contains("nau tay")) return "#A67B5B";
        if (clean.contains("nâu bò") || clean.contains("nau bo")) return "#C17737";
        if (clean.contains("socola") || clean.contains("chocolate")) return "#382216";
        if (clean.contains("nâu") || clean.contains("nau") || clean.contains("brown")) return "#6F4E37";

        // 6. Cam
        if (clean.contains("cam cháy") || clean.contains("cam chay")) return "#BF4611";
        if (clean.contains("cam đất") || clean.contains("cam dat")) return "#CF6943";
        if (clean.contains("san hô") || clean.contains("san ho")) return "#FF7F66";
        if (clean.contains("cam")) return "#FD7E14";

        // 7. Vàng
        if (clean.contains("chanh")) return "#F4E04D";
        if (clean.contains("mù tạt") || clean.contains("mu tat")) return "#E1AD01";
        if (clean.contains("vàng") || clean.contains("vang")) return "#F5C542";

        // 8. Trắng, Đen, Xám
        if (clean.contains("trắng sữa") || clean.contains("trang sua")) return "#F5F5F0";
        if (clean.contains("trắng") || clean.contains("trang") || clean.contains("white")) return "#FFFFFF";
        if (clean.contains("đen") || clean.contains("den") || clean.contains("black")) return "#111111";
        if (clean.contains("xám đậm") || clean.contains("xam dam")) return "#374151";
        if (clean.contains("xám nhạt") || clean.contains("xam nhat")) return "#E5E7EB";
        if (clean.contains("xám") || clean.contains("xam") || clean.contains("ghi")) return "#9CA3AF";
        if (clean.contains("tím") || clean.contains("tim")) return "#7952B3";

        return "#8C8C8C";
    }

    private Color getOrCreateColor(String name) {
        String trimmed = (name == null || name.isBlank()) ? "Đen" : name.trim();
        return colorRepository.findByNameIgnoreCase(trimmed)
                .map(existing -> {
                    String betterHex = resolveColorHex(trimmed);
                    if (!betterHex.equalsIgnoreCase(existing.getHexCode())) {
                        existing.setHexCode(betterHex);
                        return colorRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> colorRepository.save(Color.builder()
                        .name(trimmed)
                        .hexCode(resolveColorHex(trimmed))
                        .build()));
    }

    private Size getOrCreateSize(String name) {
        String trimmed = (name == null || name.isBlank()) ? "M" : name.trim().toUpperCase(Locale.ROOT);
        return sizeRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> {
                    int order = switch (trimmed) {
                        case "XS" -> 1;
                        case "S" -> 2;
                        case "M" -> 3;
                        case "L" -> 4;
                        case "XL" -> 5;
                        case "XXL" -> 6;
                        default -> 10;
                    };
                    return sizeRepository.save(Size.builder().name(trimmed).sortOrder(order).build());
                });
    }

    private Category resolveCategoryByIdOrSlug(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            return categoryRepository.findAllByOrderByIdAsc().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        }
        try {
            Long id = Long.parseLong(idOrSlug.trim());
            return categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        } catch (NumberFormatException ignored) {
            return categoryRepository.findBySlug(idOrSlug.trim())
                    .orElseGet(() -> categoryRepository.findAllByOrderByIdAsc().stream()
                            .filter(c -> c.getName().equalsIgnoreCase(idOrSlug.trim()))
                            .findFirst()
                            .orElseGet(() -> categoryRepository.findAllByOrderByIdAsc().stream().findFirst()
                                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND))));
        }
    }

    private Set<Long> resolveCategoryFilterIds(String slug) {
        if (slug == null || slug.isBlank()) {
            return Set.of();
        }
        return categoryRepository.findBySlug(slug.trim())
                .map(cat -> {
                    Set<Long> ids = new LinkedHashSet<>();
                    collectCategoryTreeIds(cat, ids);
                    return ids;
                })
                .orElse(Set.of());
    }

    private void collectCategoryTreeIds(Category cat, Set<Long> ids) {
        ids.add(cat.getId());
        for (Category child : cat.getChildren()) {
            collectCategoryTreeIds(child, ids);
        }
    }

    private boolean belongsToRootSlug(Category cat, String rootSlug) {
        Category curr = cat;
        while (curr != null) {
            if (rootSlug.equalsIgnoreCase(curr.getSlug())) {
                return true;
            }
            curr = curr.getParent();
        }
        return false;
    }

    private static class ProductReviewStat {
        final double avgRating;
        final int reviewCount;

        ProductReviewStat(double avgRating, int reviewCount) {
            this.avgRating = avgRating;
            this.reviewCount = reviewCount;
        }
    }

    private Map<Long, ProductReviewStat> getReviewStatsMap() {
        return reviewRepository.findReviewStatsGrouped().stream()
                .filter(row -> row != null && row.length >= 3 && row[0] != null)
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> {
                            int count = row[1] != null ? ((Number) row[1]).intValue() : 0;
                            double avg = row[2] != null ? Math.round(((Number) row[2]).doubleValue() * 10.0) / 10.0 : 5.0;
                            return new ProductReviewStat(avg, count);
                        },
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, Integer> getSoldMap() {
        return orderItemRepository.findTotalSoldByProductGrouped(OrderStatus.CANCELLED).stream()
                .filter(row -> row != null && row.length >= 2 && row[0] != null && row[1] != null)
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue(),
                        (existing, replacement) -> existing
                ));
    }

    public CatalogDtos.ProductResponseDto toProductDto(Product p, boolean includeAdminPayload) {
        Double avg = reviewRepository.findAverageRatingByProductId(p.getId());
        double avgRating = avg == null ? 5.0 : Math.round(avg * 10.0) / 10.0;
        int reviewCount = (int) reviewRepository.countByProductIdAndIsVisibleTrue(p.getId());
        Integer sold = orderItemRepository.findTotalSoldByProductId(p.getId(), OrderStatus.CANCELLED);
        int soldCount = sold != null ? sold : 0;
        return toProductDto(p, includeAdminPayload, avgRating, reviewCount, soldCount);
    }

    public CatalogDtos.ProductResponseDto toProductDto(Product p, boolean includeAdminPayload, double avgRating, int reviewCount, int soldCount) {
        Set<String> allImages = new LinkedHashSet<>();
        Set<String> colorNames = new LinkedHashSet<>();
        Set<String> sizeNames = new LinkedHashSet<>();
        List<CatalogDtos.AdminVariantPayloadDto> adminVariants = new ArrayList<>();

        List<CatalogDtos.ProductColorDto> colorDtos = p.getProductColors().stream().map(pc -> {
            colorNames.add(pc.getColor().getName());
            List<CatalogDtos.ProductImageDto> imgs = pc.getImages().stream().map(img -> {
                allImages.add(img.getImageUrl());
                return CatalogDtos.ProductImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .displayOrder(img.getDisplayOrder())
                        .build();
            }).toList();

            String primaryImg = imgs.isEmpty() ? null : imgs.get(0).getImageUrl();

            List<CatalogDtos.VariantDto> variants = pc.getVariants().stream().map(v -> {
                sizeNames.add(v.getSize().getName());
                if (includeAdminPayload) {
                    Map<String, String> optMap = new LinkedHashMap<>();
                    optMap.put("Màu sắc", pc.getColor().getName());
                    optMap.put("Kích thước", v.getSize().getName());
                    adminVariants.add(CatalogDtos.AdminVariantPayloadDto.builder()
                            .id(v.getId())
                            .sku(v.getSku())
                            .price(v.getPrice())
                            .costPrice(v.getCostPrice())
                            .stock(v.getStock())
                            .options(optMap)
                            .image(primaryImg)
                            .build());
                }
                return CatalogDtos.VariantDto.builder()
                        .id(v.getId())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .costPrice(v.getCostPrice())
                        .stock(v.getStock())
                        .size(CatalogDtos.SizeDto.builder()
                                .id(v.getSize().getId())
                                .name(v.getSize().getName())
                                .sortOrder(v.getSize().getSortOrder())
                                .build())
                        .colorName(pc.getColor().getName())
                        .hexCode(pc.getColor().getHexCode())
                        .productId(p.getId())
                        .productName(p.getName())
                        .imageUrl(primaryImg)
                        .build();
            }).toList();

            return CatalogDtos.ProductColorDto.builder()
                    .id(pc.getId())
                    .isActive(pc.isActive())
                    .color(CatalogDtos.ColorDto.builder()
                            .id(pc.getColor().getId())
                            .name(pc.getColor().getName())
                            .hexCode(pc.getColor().getHexCode())
                            .build())
                    .images(imgs)
                    .variants(variants)
                    .build();
        }).toList();

        Category cat = p.getCategory();
        CatalogDtos.CategorySimpleDto parentDto = cat.getParent() == null ? null :
                CatalogDtos.CategorySimpleDto.builder()
                        .id(cat.getParent().getId())
                        .name(cat.getParent().getName())
                        .slug(cat.getParent().getSlug())
                        .build();

        List<CatalogDtos.AdminOptionDto> adminOptions = includeAdminPayload ? List.of(
                CatalogDtos.AdminOptionDto.builder().name("Màu sắc").values(new ArrayList<>(colorNames)).build(),
                CatalogDtos.AdminOptionDto.builder().name("Kích thước").values(new ArrayList<>(sizeNames)).build()
        ) : List.of();

        Map<String, List<String>> colorImagesMap = new LinkedHashMap<>();
        for (CatalogDtos.ProductColorDto pcDto : colorDtos) {
            if (pcDto.getColor() != null && pcDto.getColor().getName() != null) {
                List<String> imgUrls = pcDto.getImages().stream().map(CatalogDtos.ProductImageDto::getImageUrl).toList();
                colorImagesMap.put(pcDto.getColor().getName(), imgUrls);
            }
        }

        return CatalogDtos.ProductResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(buildProductSlug(p))
                .sku(p.getSkuPrefix())
                .brand(p.getBrand())
                .basePrice(p.getBasePrice())
                .effectivePrice(p.getEffectivePrice())
                .formattedPrice(SlugUtils.formatVnd(p.getEffectivePrice()))
                .discountPercent(p.getDiscountPercent())
                .description(p.getDescription())
                .material(p.getMaterial())
                .careInstructions(p.getCareInstructions())
                .isNew(p.isNew())
                .isBestSeller(p.isBestSeller())
                .isActive(p.isActive())
                .totalStock(p.getTotalStock())
                .sold(soldCount)
                .rating(avgRating)
                .reviewCount(reviewCount)
                .category(CatalogDtos.CategorySimpleDto.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .slug(cat.getSlug())
                        .parent(parentDto)
                        .build())
                .productColors(colorDtos)
                .images(new ArrayList<>(allImages))
                .colorImages(colorImagesMap)
                .options(adminOptions)
                .adminVariants(adminVariants)
                .build();
    }

    private CatalogDtos.CategoryDto toCategoryTreeDto(Category c) {
        List<CatalogDtos.CategoryDto> children = c.getChildren().stream()
                .filter(Category::isActive)
                .map(this::toCategoryTreeDto)
                .toList();
        long directCount = productRepository.countByCategoryId(c.getId());
        long childSum = children.stream().mapToLong(CatalogDtos.CategoryDto::getProductCount).sum();
        return CatalogDtos.CategoryDto.builder()
                .id(String.valueOf(c.getId()))
                .numericId(c.getId())
                .parentId(c.getParent() == null ? null : c.getParent().getId())
                .parentName(c.getParent() == null ? null : c.getParent().getName())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .productCount(directCount + childSum)
                .isActive(c.isActive())
                .status(c.isActive() ? "Hoạt động" : "Ẩn")
                .children(children)
                .build();
    }

    public CatalogDtos.ReviewDto toReviewDto(Review r) {
        return CatalogDtos.ReviewDto.builder()
                .id(String.valueOf(r.getId()))
                .numericId(r.getId())
                .productId(String.valueOf(r.getProduct().getId()))
                .productName(r.getProduct().getName())
                .customerName(r.getUser().getFullName())
                .customerPhone(r.getUser().getPhone() == null ? "090***8899" : r.getUser().getPhone())
                .rating(r.getRating())
                .comment(r.getComment())
                .reply(r.getAdminReply())
                .isHidden(!r.isVisible())
                .createdAt(SlugUtils.formatDate(r.getCreatedAt()))
                .build();
    }

    public static OrderDtos.VoucherDto toVoucherDto(Voucher v) {
        LocalDateTime now = LocalDateTime.now();
        String uiStatus;
        if (v.getStatus() == VoucherStatus.CANCELLED) {
            uiStatus = "Đã hủy";
        } else if (v.getStatus() == VoucherStatus.EXPIRED
                || (v.getEndDate() != null && now.isAfter(v.getEndDate()))
                || (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit())) {
            uiStatus = "Đã kết thúc";
        } else if (v.getStatus() == VoucherStatus.SCHEDULED
                || (v.getStartDate() != null && now.isBefore(v.getStartDate()))) {
            uiStatus = "Sắp diễn ra";
        } else {
            uiStatus = "Đang diễn ra";
        }

        String formattedDiscount = v.getDiscountType().name().equals("PERCENTAGE")
                ? v.getDiscountValue().stripTrailingZeros().toPlainString() + "%"
                  + (v.getMaxDiscountAmount() != null ? " (Tối đa " + SlugUtils.formatVnd(v.getMaxDiscountAmount()) + ")" : "")
                : SlugUtils.formatVnd(v.getDiscountValue());

        String qty = v.getUsageLimit() == null
                ? v.getUsedCount() + " / ∞"
                : v.getUsedCount() + " / " + v.getUsageLimit();

        return OrderDtos.VoucherDto.builder()
                .id(String.valueOf(v.getId()))
                .numericId(v.getId())
                .code(v.getCode())
                .name(v.getName())
                .discountType(v.getDiscountType().name())
                .discountValue(v.getDiscountValue())
                .minOrderValueRaw(v.getMinOrderValue())
                .maxDiscountAmount(v.getMaxDiscountAmount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .usageLimit(v.getUsageLimit())
                .usedCount(v.getUsedCount())
                .rawStatus(v.getStatus().name())
                .discountAmount(formattedDiscount)
                .minOrderValue(SlugUtils.formatVnd(v.getMinOrderValue()))
                .quantity(qty)
                .status(uiStatus)
                .duration(SlugUtils.formatDate(v.getStartDate()) + " - " + SlugUtils.formatDate(v.getEndDate()))
                .build();
    }
}

