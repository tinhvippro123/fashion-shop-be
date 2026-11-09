package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.OrderItem;
import com.fashionshop.be.domain.entity.Product;
import com.fashionshop.be.domain.entity.ProductColor;
import com.fashionshop.be.domain.entity.ProductVariant;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.entity.UserAddress;
import com.fashionshop.be.domain.entity.Voucher;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.PaymentMethod;
import com.fashionshop.be.domain.enums.PaymentStatus;
import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ErrorCode;
import com.fashionshop.be.exception.ForbiddenException;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.pattern.discount.DiscountStrategy;
import com.fashionshop.be.pattern.discount.DiscountStrategyFactory;
import com.fashionshop.be.repository.CartRepository;
import com.fashionshop.be.repository.OrderRepository;
import com.fashionshop.be.repository.ProductRepository;
import com.fashionshop.be.repository.ProductVariantRepository;
import com.fashionshop.be.repository.ReviewRepository;
import com.fashionshop.be.repository.UserAddressRepository;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.repository.VoucherRepository;
import com.fashionshop.be.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000");

    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VoucherRepository voucherRepository;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final SecurityUtils securityUtils;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;
    private final NotificationService notificationService;
    private final ReviewRepository reviewRepository;

    @Transactional
    public OrderDtos.OrderResponseDto createOrder(OrderDtos.CreateOrderRequest request) {
        User currentUser = securityUtils.getCurrentUserOptional().orElse(null);

        PaymentMethod paymentMethod = parsePaymentMethod(request.getPaymentMethod());
        PaymentStatus initialPaymentStatus = PaymentStatus.UNPAID;

        String orderCode = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + "-" + (1000 + (System.currentTimeMillis() % 9000));

        String recipientPhone = request.getRecipientPhone() != null ? request.getRecipientPhone().trim() : "";

        // Tự động cập nhật số điện thoại cho tài khoản người dùng nếu tài khoản chưa có số điện thoại
        if (currentUser != null && (currentUser.getPhone() == null || currentUser.getPhone().isBlank()) && !recipientPhone.isBlank()) {
            currentUser.setPhone(recipientPhone);
            userRepository.save(currentUser);
        }

        // Tự động lưu địa chỉ giao hàng vào sổ địa chỉ nếu tài khoản chưa có địa chỉ này
        if (currentUser != null && request.getShippingAddress() != null && !request.getShippingAddress().isBlank()) {
            saveShippingAddressToUserBookIfNotExists(currentUser, request.getRecipientName(), recipientPhone, request.getShippingAddress());
        }

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(currentUser)
                .recipientName(request.getRecipientName().trim())
                .recipientPhone(recipientPhone)
                .recipientEmail(request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()
                        ? request.getRecipientEmail().trim()
                        : (currentUser == null ? null : currentUser.getEmail()))
                .shippingAddress(request.getShippingAddress().trim())
                .paymentMethod(paymentMethod)
                .paymentStatus(initialPaymentStatus)
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .build();

        for (OrderDtos.CreateOrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = productVariantRepository.findByIdForUpdate(itemReq.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VARIANT_NOT_FOUND));

            // OOP Encapsulation: ProductVariant validates & deducts its own stock
            variant.deductStock(itemReq.getQuantity());
            productVariantRepository.save(variant);

            ProductColor pc = variant.getProductColor();
            Product product = pc.getProduct();
            if (!product.isActive()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOPPED_BUSINESS);
            }
            BigDecimal baseUnitPrice = (variant.getPrice() != null && variant.getPrice().compareTo(BigDecimal.ZERO) > 0)
                    ? variant.getPrice()
                    : product.getBasePrice();
            BigDecimal effectiveUnitPrice = product.getDiscountPercent() > 0
                    ? baseUnitPrice.multiply(BigDecimal.valueOf(100 - product.getDiscountPercent()))
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                    : baseUnitPrice;

            BigDecimal lineTotal = effectiveUnitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            String img = pc.getImages().isEmpty() ? "" : pc.getImages().get(0).getImageUrl();

            order.addItem(OrderItem.builder()
                    .variant(variant)
                    .productId(product.getId())
                    .productSlug(CatalogService.buildProductSlug(product))
                    .productName(product.getName())
                    .colorName(pc.getColor().getName())
                    .sizeName(variant.getSize().getName())
                    .sku(variant.getSku())
                    .imageUrl(img)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(effectiveUnitPrice)
                    .costPrice(variant.getCostPrice() == null ? BigDecimal.ZERO : variant.getCostPrice())
                    .totalPrice(lineTotal)
                    .build());
        }

        // Calculate subtotal first so Voucher Strategy can evaluate
        order.recalculateTotals(DEFAULT_SHIPPING_FEE, BigDecimal.ZERO);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            Voucher voucher = voucherRepository.findByCodeIgnoreCaseForUpdate(request.getVoucherCode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOUCHER_NOT_FOUND));

            voucher.validateApplicable(order.getSubtotal(), LocalDateTime.now());
            // OOP Polymorphism via Strategy + Factory Pattern
            DiscountStrategy strategy = discountStrategyFactory.getStrategy(voucher.getDiscountType());
            discountAmount = strategy.calculateDiscount(order.getSubtotal(), voucher);
            voucher.consumeUsage();
            voucherRepository.save(voucher);
            order.setVoucherCode(voucher.getCode());
        }

        order.recalculateTotals(DEFAULT_SHIPPING_FEE, discountAmount);
        Order saved = orderRepository.save(order);

        // Xóa giỏ hàng trên máy chủ nếu người dùng đã đăng nhập
        if (currentUser != null) {
            cartRepository.findByUserId(currentUser.getId()).ifPresent(c -> {
                c.clear();
                cartRepository.save(c);
            });
        }

        OrderDtos.OrderResponseDto dto = toOrderDto(saved);
        if (saved.getPaymentMethod() == PaymentMethod.VNPAY) {
            String vnpayUrl = paymentService.createVnPayPaymentUrl(saved, "127.0.0.1");
            dto.setPaymentUrl(vnpayUrl);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponseDto> getMyOrders() {
        User user = securityUtils.getCurrentUserRequired();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toOrderDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponseDto getOrderByIdOrCode(String idOrCode) {
        Order order = resolveOrder(idOrCode);
        User currentUser = securityUtils.getCurrentUserOptional().orElse(null);
        if (currentUser == null) {
            if (!order.getOrderCode().equalsIgnoreCase(idOrCode.trim())) {
                throw new BusinessException(ErrorCode.UNAUTHENTICATED);
            }
            return toOrderDto(order);
        }
        if (!currentUser.isAdmin()
                && order.getUser() != null
                && !order.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }
        return toOrderDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponseDto trackOrder(String orderCode, String phone) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new BusinessException(ErrorCode.ORDER_CODE_EMPTY);
        }
        if (phone == null || phone.isBlank()) {
            throw new BusinessException(ErrorCode.ORDER_PHONE_EMPTY);
        }
        Order order = orderRepository.findByOrderCodeIgnoreCase(orderCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getRecipientPhone().equals(phone.trim())) {
            throw new BusinessException(ErrorCode.ORDER_PHONE_MISMATCH);
        }
        return toOrderDto(order);
    }

    @Transactional
    public OrderDtos.OrderResponseDto cancelMyOrder(String idOrCode, String reason) {
        User currentUser = securityUtils.getCurrentUserRequired();
        Order order = resolveOrder(idOrCode);
        if (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ORDER);
        }
        return transitionOrderInternal(order, OrderStatus.CANCELLED,
                (reason == null || reason.isBlank()) ? "Khách hàng yêu cầu hủy đơn" : reason.trim());
    }

    @Transactional
    public OrderDtos.OrderResponseDto cancelUnpaidOrder(String idOrCode, String reason) {
        Order order = resolveOrder(idOrCode);
        if (order.getStatus() != OrderStatus.PENDING || order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ORDER);
        }
        return transitionOrderInternal(order, OrderStatus.CANCELLED,
                (reason == null || reason.isBlank()) ? "Khách hàng hủy thanh toán hoặc thay đổi đơn hàng" : reason.trim());
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponseDto> getAllAdminOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toOrderDto)
                .toList();
    }

    @Transactional
    public OrderDtos.OrderResponseDto updateAdminOrderStatus(String idOrCode, OrderDtos.UpdateOrderStatusRequest request) {
        Order order = resolveOrder(idOrCode);
        OrderStatus targetStatus;
        try {
            targetStatus = OrderStatus.valueOf(request.getStatus().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        return transitionOrderInternal(order, targetStatus, request.getReason());
    }

    private OrderDtos.OrderResponseDto transitionOrderInternal(Order order, OrderStatus targetStatus, String reason) {
        order.transitionTo(targetStatus, reason);
        if (targetStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getVariant() != null) {
                    item.getVariant().restoreStock(item.getQuantity());
                    productVariantRepository.save(item.getVariant());
                }
            }
            if (order.getVoucherCode() != null && !order.getVoucherCode().isBlank()) {
                voucherRepository.findByCodeIgnoreCase(order.getVoucherCode().trim())
                        .ifPresent(voucher -> {
                            voucher.restoreUsage();
                            voucherRepository.save(voucher);
                        });
            }
        }
        Order saved = orderRepository.save(order);
        try {
            notificationService.createOrderNotification(saved, targetStatus);
        } catch (Exception ex) {
            // Keep transition successful even if notification fails
        }
        return toOrderDto(saved);
    }

    public Order resolveOrder(String idOrCode) {
        if (idOrCode == null || idOrCode.isBlank()) {
            throw new BusinessException(ErrorCode.ORDER_CODE_EMPTY);
        }
        String trimmed = idOrCode.trim();
        return orderRepository.findByOrderCodeIgnoreCase(trimmed)
                .orElseGet(() -> {
                    try {
                        Long id = Long.parseLong(trimmed);
                        return orderRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
                    } catch (NumberFormatException ex) {
                        throw new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND);
                    }
                });
    }

    private PaymentMethod parsePaymentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            return PaymentMethod.COD;
        }
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return PaymentMethod.COD;
        }
    }

    public OrderDtos.OrderResponseDto toOrderDto(Order order) {
        String methodLabel = switch (order.getPaymentMethod()) {
            case BANK_TRANSFER -> "Chuyển khoản";
            case VNPAY -> "VNPay";
            case COD -> "COD";
        };
        String paymentLabel = switch (order.getPaymentStatus()) {
            case PAID -> "Đã thanh toán (" + methodLabel + ")";
            case REFUNDED -> "Đã hoàn tiền";
            case UNPAID -> "Chưa thanh toán (" + methodLabel + ")";
        };
        String paymentBg = switch (order.getPaymentStatus()) {
            case PAID -> "bg-emerald-500/10 text-emerald-600";
            case REFUNDED -> "bg-blue-500/10 text-blue-600";
            case UNPAID -> "bg-amber-500/10 text-amber-600";
        };

        java.util.Set<Long> reviewedProductIds = java.util.Collections.emptySet();
        if (order.getUser() != null) {
            reviewedProductIds = new java.util.HashSet<>(reviewRepository.findReviewedProductIdsByUserId(order.getUser().getId()));
        }
        final java.util.Set<Long> finalReviewedIds = reviewedProductIds;

        List<OrderDtos.OrderItemDto> items = order.getItems().stream()
                .map(i -> OrderDtos.OrderItemDto.builder()
                        .id(String.valueOf(i.getId()))
                        .variantId(i.getVariant() == null ? null : i.getVariant().getId())
                        .productId(i.getProductId())
                        .productSlug(resolveProductSlug(i))
                        .name(i.getProductName())
                        .image(i.getImageUrl() == null ? "" : i.getImageUrl())
                        .variantInfo("Màu: " + i.getColorName() + " | Size: " + i.getSizeName())
                        .colorName(i.getColorName())
                        .sizeName(i.getSizeName())
                        .sku(i.getSku())
                        .price(i.getUnitPrice())
                        .costPrice(i.getCostPrice())
                        .quantity(i.getQuantity())
                        .totalPrice(i.getTotalPrice())
                        .isReviewed(i.getProductId() != null && finalReviewedIds.contains(i.getProductId()))
                        .build())
                .toList();

        List<OrderDtos.AppliedPromotionDto> appliedPromotions =
                (order.getVoucherCode() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0)
                        ? List.of(OrderDtos.AppliedPromotionDto.builder()
                        .id(order.getVoucherCode())
                        .name("Mã giảm giá " + order.getVoucherCode())
                        .code(order.getVoucherCode())
                        .discountAmount(order.getDiscountAmount())
                        .build())
                        : List.of();

        String confirmedAt = order.getConfirmedAt() != null ? SlugUtils.formatDateTime(order.getConfirmedAt()) : null;
        String shippingAt = order.getShippingAt() != null ? SlugUtils.formatDateTime(order.getShippingAt()) : null;
        String completedAt = order.getCompletedAt() != null ? SlugUtils.formatDateTime(order.getCompletedAt()) : null;
        String cancelledAt = order.getCancelledAt() != null ? SlugUtils.formatDateTime(order.getCancelledAt()) : null;

        if (confirmedAt == null && (order.getStatus() == OrderStatus.PROCESSING || order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.COMPLETED)) {
            confirmedAt = (order.getStatus() == OrderStatus.PROCESSING && order.getUpdatedAt() != null)
                    ? SlugUtils.formatDateTime(order.getUpdatedAt())
                    : SlugUtils.formatDateTime(order.getCreatedAt());
        }
        if (shippingAt == null && (order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.COMPLETED)) {
            shippingAt = SlugUtils.formatDateTime(order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt());
        }
        if (completedAt == null && order.getStatus() == OrderStatus.COMPLETED) {
            completedAt = SlugUtils.formatDateTime(order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt());
        }
        if (cancelledAt == null && order.getStatus() == OrderStatus.CANCELLED) {
            cancelledAt = SlugUtils.formatDateTime(order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt());
        }

        return OrderDtos.OrderResponseDto.builder()
                .id(order.getOrderCode())
                .numericId(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getRecipientName())
                .customerEmail(order.getRecipientEmail() == null ? "khachhang@luxe.vn" : order.getRecipientEmail())
                .customerPhone(order.getRecipientPhone())
                .date(SlugUtils.formatDate(order.getCreatedAt()))
                .createdAt(SlugUtils.formatDateTime(order.getCreatedAt()))
                .updatedAt(order.getUpdatedAt() != null ? SlugUtils.formatDateTime(order.getUpdatedAt()) : SlugUtils.formatDateTime(order.getCreatedAt()))
                .confirmedAt(confirmedAt)
                .shippingAt(shippingAt)
                .completedAt(completedAt)
                .cancelledAt(cancelledAt)
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .paymentMethod(order.getPaymentMethod().name())
                .payment(paymentLabel)
                .paymentBg(paymentBg)
                .total(SlugUtils.formatVnd(order.getTotalAmount()))
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .voucherCode(order.getVoucherCode())
                .note(order.getNote())
                .cancelReason(order.getCancelReason())
                .items(items)
                .appliedPromotions(appliedPromotions)
                .customer(OrderDtos.CustomerInfoDto.builder()
                        .id(order.getUser() == null ? null : String.valueOf(order.getUser().getId()))
                        .name(order.getRecipientName())
                        .type(order.getUser() == null ? "Khách vãng lai" : "Thành viên LUXE")
                        .email(order.getRecipientEmail() == null ? "—" : order.getRecipientEmail())
                        .phone(order.getRecipientPhone())
                        .build())
                .shipping(OrderDtos.ShippingInfoDto.builder()
                        .address(order.getShippingAddress())
                        .method("Giao hàng tiêu chuẩn LUXE Express")
                        .code("LX" + String.format("%06d", order.getId() == null ? 1 : order.getId()))
                        .build())
                .build();
    }

    private void saveShippingAddressToUserBookIfNotExists(User user, String recipientName, String phone, String shippingAddress) {
        try {
            List<UserAddress> existing = userAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId());
            String normalizedTarget = AddressUtils.normalize(shippingAddress);
            boolean exists = existing.stream().anyMatch(a ->
                    a.getFullAddress() != null && AddressUtils.normalize(a.getFullAddress()).equals(normalizedTarget)
            );
            if (!exists) {
                AddressUtils.ParsedAddress parsed = AddressUtils.parseAddress(shippingAddress);
                UserAddress newAddr = UserAddress.builder()
                        .user(user)
                        .recipientName(recipientName != null && !recipientName.isBlank() ? recipientName.trim() : user.getFullName())
                        .phone(phone != null && !phone.isBlank() ? phone.trim() : (user.getPhone() != null ? user.getPhone() : "0900000000"))
                        .province(parsed.getProvince())
                        .district(parsed.getDistrict())
                        .ward(parsed.getWard())
                        .streetAddress(parsed.getStreetAddress())
                        .fullAddress(parsed.getFullAddress())
                        .isDefault(existing.isEmpty())
                        .build();
                userAddressRepository.save(newAddr);
            }
        } catch (Exception ignored) {}
    }

    private String resolveProductSlug(OrderItem item) {
        if (item.getProductSlug() != null && !item.getProductSlug().isBlank()) {
            return item.getProductSlug();
        }
        if (item.getVariant() != null && item.getVariant().getProductColor() != null
                && item.getVariant().getProductColor().getProduct() != null) {
            return CatalogService.buildProductSlug(item.getVariant().getProductColor().getProduct());
        }
        if (item.getProductId() != null) {
            return productRepository.findById(item.getProductId()).map(CatalogService::buildProductSlug).orElse(null);
        }
        return null;
    }
}

