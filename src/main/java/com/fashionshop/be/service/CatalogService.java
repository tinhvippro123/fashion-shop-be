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
import com.fashionshop.be.domain.enums.VoucherStatus;
import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.pattern.discount.DiscountStrategy;
import com.fashionshop.be.pattern.discount.DiscountStrategyFactory;
import com.fashionshop.be.repository.CategoryRepository;
import com.fashionshop.be.repository.ColorRepository;
import com.fashionshop.be.repository.OrderRepository;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private static final Map<String, String> DEFAULT_COLOR_HEX = Map.ofEntries(
            Map.entry("đen", "#111111"),
            Map.entry("trắng", "#FFFFFF"),
            Map.entry("kem", "#F5F0E6"),
            Map.entry("be", "#E8DCC8"),
            Map.entry("đỏ", "#C92A2A"),
            Map.entry("hồng", "#F2A6B3"),
            Map.entry("vàng", "#F5C542"),
            Map.entry("xanh navy", "#1B365D"),
            Map.entry("xanh dương", "#2563EB"),
            Map.entry("xanh rêu", "#4A5D4E"),
            Map.entry("xám", "#9CA3AF"),
            Map.entry("xám nhạt", "#E0E0E0"),
            Map.entry("nâu", "#6F4E37")
    );

    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReviewRepository reviewRepository;
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<CatalogDtos.CategoryDto> getCategoryTree() {
        return categoryRepository.findByParentIsNullOrderByIdAsc()
                .stream()
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

    @Transactional(readOnly = true)
    public List<CatalogDtos.SizeDto> getAllSizes() {
        return sizeRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(s -> CatalogDtos.SizeDto.builder().id(s.getId()).name(s.getName()).sortOrder(s.getSortOrder()).build())
                .toList();
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

        List<CatalogDtos.ProductResponseDto> filtered = activeProducts.stream()
                .filter(p -> {
                    if (filterNuNew) {
                        return p.isNew() && belongsToRootSlug(p.getCategory(), "nu");
                    }
                    if (filterNamNew) {
                        return p.isNew() && belongsToRootSlug(p.getCategory(), "nam");
                    }
                    if (!matchingCategoryIds.isEmpty()) {
                        return matchingCategoryIds.contains(p.getCategory().getId());
                    }
                    return categorySlug == null || categorySlug.isBlank() || "all".equalsIgnoreCase(categorySlug);
                })
                .filter(p -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String kw = keyword.trim().toLowerCase(Locale.ROOT);
                    return p.getName().toLowerCase(Locale.ROOT).contains(kw)
                            || (p.getDescription() != null && p.getDescription().toLowerCase(Locale.ROOT).contains(kw))
                            || (p.getSkuPrefix() != null && p.getSkuPrefix().toLowerCase(Locale.ROOT).contains(kw));
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
                .map(p -> toProductDto(p, false))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        if ("price_asc".equalsIgnoreCase(sort) || "price-asc".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparing(CatalogDtos.ProductResponseDto::getEffectivePrice));
        } else if ("price_desc".equalsIgnoreCase(sort) || "price-desc".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparing(CatalogDtos.ProductResponseDto::getEffectivePrice).reversed());
        } else if ("best_seller".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparingInt(CatalogDtos.ProductResponseDto::getSold).reversed());
        }

        return filtered;
    }

    @Transactional(readOnly = true)
    public CatalogDtos.ProductResponseDto getProductDetail(String idOrSlug) {
        Product product = findProductByIdOrSlug(idOrSlug);
        return toProductDto(product, true);
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ReviewDto> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toReviewDto)
                .toList();
    }

    @Transactional
    public CatalogDtos.ReviewDto addProductReview(Long productId, CatalogDtos.CreateReviewRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

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
                .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại"));

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
        return productRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(p -> toProductDto(p, true))
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
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        category.setName(request.getName().trim());
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            category.setSlug(SlugUtils.toSlug(request.getSlug()));
        }
        category.setDescription(request.getDescription());
        if (request.getParentId() != null && !request.getParentId().equals(id)) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        if (productRepository.countByCategoryId(id) > 0) {
            throw new BusinessException("Không thể xóa danh mục đang chứa sản phẩm");
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
        Product product = Product.builder()
                .category(category)
                .name(request.getName().trim())
                .slug(slug)
                .skuPrefix((request.getSku() == null || request.getSku().isBlank())
                        ? "LUXE-" + (System.currentTimeMillis() % 100000)
                        : request.getSku().trim())
                .brand((request.getBrand() == null || request.getBrand().isBlank()) ? "LUXE" : request.getBrand().trim())
                .basePrice(request.getPrice())
                .discountPercent(request.getDiscountPercent() == null ? 0 : Math.max(0, request.getDiscountPercent()))
                .description(request.getDescription())
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        Category category = resolveCategoryByIdOrSlug(request.getCategory());
        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setBasePrice(request.getPrice());
        if (request.getDiscountPercent() != null) {
            product.setDiscountPercent(Math.max(0, request.getDiscountPercent()));
        }
        product.setDescription(request.getDescription());
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
            product.getProductColors().clear();
            productRepository.flush();
            populateColorsAndVariants(product, request);
        }

        return toProductDto(productRepository.save(product), true);
    }

    @Transactional
    public void deleteAdminProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        product.setActive(false);
        productRepository.save(product);
    }

    private void populateColorsAndVariants(Product product, CatalogDtos.AdminProductUpsertRequest request) {
        List<String> fallbackImages = (request.getImages() == null || request.getImages().isEmpty())
                ? List.of("03321697-61dc-4f30-8da7-d25085a78741.webp", "121a23af-e7f7-4e5a-8e96-65f25b8a4a3f.webp")
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
            String colorName = opts.getOrDefault("Màu sắc", opts.getOrDefault("Color", "Mặc định"));
            String sizeName = opts.getOrDefault("Kích thước", opts.getOrDefault("Size", "Freesize"));

            ProductColor pc = colorGroupMap.computeIfAbsent(colorName, cn -> {
                Color color = getOrCreateColor(cn);
                ProductColor created = ProductColor.builder().color(color).isActive(true).build();
                if (vReq.getImage() != null && !vReq.getImage().isBlank()) {
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

    private String ensureUniqueSku(String candidate) {
        if (!productVariantRepository.existsBySkuIgnoreCase(candidate)) {
            return candidate;
        }
        return candidate + "-" + (System.currentTimeMillis() % 10000);
    }

    private Color getOrCreateColor(String name) {
        String trimmed = (name == null || name.isBlank()) ? "Đen" : name.trim();
        return colorRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> colorRepository.save(Color.builder()
                        .name(trimmed)
                        .hexCode(DEFAULT_COLOR_HEX.getOrDefault(trimmed.toLowerCase(Locale.ROOT), "#221f20"))
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
                    .orElseThrow(() -> new BusinessException("Hệ thống chưa có danh mục sản phẩm"));
        }
        try {
            Long id = Long.parseLong(idOrSlug.trim());
            return categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục #" + id));
        } catch (NumberFormatException ignored) {
            return categoryRepository.findBySlug(idOrSlug.trim())
                    .orElseGet(() -> categoryRepository.findAllByOrderByIdAsc().stream()
                            .filter(c -> c.getName().equalsIgnoreCase(idOrSlug.trim()))
                            .findFirst()
                            .orElseGet(() -> categoryRepository.findAllByOrderByIdAsc().stream().findFirst()
                                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục: " + idOrSlug))));
        }
    }

    private Product findProductByIdOrSlug(String idOrSlug) {
        try {
            Long id = Long.parseLong(idOrSlug.trim());
            return productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm #" + id));
        } catch (NumberFormatException ignored) {
            return productRepository.findBySlug(idOrSlug.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + idOrSlug));
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

    public CatalogDtos.ProductResponseDto toProductDto(Product p, boolean includeAdminPayload) {
        List<Review> visibleReviews = reviewRepository.findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(p.getId());
        double avgRating = visibleReviews.isEmpty() ? 5.0 :
                Math.round(visibleReviews.stream().mapToInt(Review::getRating).average().orElse(5.0) * 10.0) / 10.0;

        int soldCount = orderRepository.findAll().stream()
                .flatMap(o -> o.getItems().stream())
                .filter(oi -> p.getId().equals(oi.getProductId()))
                .mapToInt(oi -> oi.getQuantity())
                .sum();

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

        return CatalogDtos.ProductResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .sku(p.getSkuPrefix())
                .brand(p.getBrand())
                .basePrice(p.getBasePrice())
                .effectivePrice(p.getEffectivePrice())
                .formattedPrice(SlugUtils.formatVnd(p.getEffectivePrice()))
                .discountPercent(p.getDiscountPercent())
                .description(p.getDescription())
                .isNew(p.isNew())
                .isBestSeller(p.isBestSeller())
                .isActive(p.isActive())
                .totalStock(p.getTotalStock())
                .sold(soldCount)
                .rating(avgRating)
                .reviewCount(visibleReviews.size())
                .category(CatalogDtos.CategorySimpleDto.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .slug(cat.getSlug())
                        .parent(parentDto)
                        .build())
                .productColors(colorDtos)
                .images(new ArrayList<>(allImages))
                .options(adminOptions)
                .adminVariants(adminVariants)
                .build();
    }

    private CatalogDtos.CategoryDto toCategoryTreeDto(Category c) {
        List<CatalogDtos.CategoryDto> children = c.getChildren().stream()
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

