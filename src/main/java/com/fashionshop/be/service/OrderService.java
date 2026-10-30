package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.OrderItem;
import com.fashionshop.be.domain.entity.Product;
import com.fashionshop.be.domain.entity.ProductColor;
import com.fashionshop.be.domain.entity.ProductVariant;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.entity.Voucher;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.PaymentMethod;
import com.fashionshop.be.domain.enums.PaymentStatus;
import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.pattern.discount.DiscountStrategy;
import com.fashionshop.be.pattern.discount.DiscountStrategyFactory;
import com.fashionshop.be.repository.OrderRepository;
import com.fashionshop.be.repository.ProductVariantRepository;
import com.fashionshop.be.repository.VoucherRepository;
import com.fashionshop.be.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Transactional
    public OrderDtos.OrderResponseDto createOrder(OrderDtos.CreateOrderRequest request) {
        User currentUser = securityUtils.getCurrentUserOptional().orElse(null);

        PaymentMethod paymentMethod = parsePaymentMethod(request.getPaymentMethod());
        PaymentStatus initialPaymentStatus = PaymentStatus.UNPAID;

        String orderCode = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + "-" + (1000 + (System.currentTimeMillis() % 9000));

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(currentUser)
                .recipientName(request.getRecipientName().trim())
                .recipientPhone(request.getRecipientPhone().trim())
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
            ProductVariant variant = productVariantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm #" + itemReq.getVariantId()));

            // OOP Encapsulation: ProductVariant validates & deducts its own stock
            variant.deductStock(itemReq.getQuantity());
            productVariantRepository.save(variant);

            ProductColor pc = variant.getProductColor();
            Product product = pc.getProduct();
            BigDecimal effectiveUnitPrice = product.getDiscountPercent() > 0
                    ? product.getEffectivePrice()
                    : (variant.getPrice() != null && variant.getPrice().compareTo(BigDecimal.ZERO) > 0
                        ? variant.getPrice()
                        : product.getEffectivePrice());

            BigDecimal lineTotal = effectiveUnitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            String img = pc.getImages().isEmpty() ? "" : pc.getImages().get(0).getImageUrl();

            order.addItem(OrderItem.builder()
                    .variant(variant)
                    .productId(product.getId())
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
            Voucher voucher = voucherRepository.findByCodeIgnoreCase(request.getVoucherCode().trim())
                    .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại"));

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
        return toOrderDto(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponseDto> getMyOrders() {
        User user = securityUtils.getCurrentUserRequired();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(OrderService::toOrderDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponseDto getOrderByIdOrCode(String idOrCode) {
        Order order = resolveOrder(idOrCode);
        User currentUser = securityUtils.getCurrentUserOptional().orElse(null);
        if (currentUser != null && !currentUser.isAdmin()
                && order.getUser() != null
                && !order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền xem đơn hàng này");
        }
        return toOrderDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponseDto trackOrder(String orderCode, String phone) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new BusinessException("Vui lòng nhập mã đơn hàng");
        }
        Order order = orderRepository.findByOrderCodeIgnoreCase(orderCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));
        if (phone != null && !phone.isBlank() && !order.getRecipientPhone().equals(phone.trim())) {
            throw new BusinessException("Số điện thoại không khớp với mã đơn hàng");
        }
        return toOrderDto(order);
    }

    @Transactional
    public OrderDtos.OrderResponseDto cancelMyOrder(String idOrCode, String reason) {
        User currentUser = securityUtils.getCurrentUserRequired();
        Order order = resolveOrder(idOrCode);
        if (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền hủy đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể hủy đơn hàng khi đang ở trạng thái Chờ xử lý");
        }
        return transitionOrderInternal(order, OrderStatus.CANCELLED,
                (reason == null || reason.isBlank()) ? "Khách hàng yêu cầu hủy đơn" : reason.trim());
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponseDto> getAllAdminOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(OrderService::toOrderDto)
                .toList();
    }

    @Transactional
    public OrderDtos.OrderResponseDto updateAdminOrderStatus(String idOrCode, OrderDtos.UpdateOrderStatusRequest request) {
        Order order = resolveOrder(idOrCode);
        OrderStatus targetStatus;
        try {
            targetStatus = OrderStatus.valueOf(request.getStatus().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Trạng thái đơn hàng không hợp lệ: " + request.getStatus());
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
        return toOrderDto(orderRepository.save(order));
    }

    public Order resolveOrder(String idOrCode) {
        if (idOrCode == null || idOrCode.isBlank()) {
            throw new BusinessException("Mã đơn hàng không hợp lệ");
        }
        String trimmed = idOrCode.trim();
        return orderRepository.findByOrderCodeIgnoreCase(trimmed)
                .orElseGet(() -> {
                    try {
                        Long id = Long.parseLong(trimmed);
                        return orderRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng #" + id));
                    } catch (NumberFormatException ex) {
                        throw new ResourceNotFoundException("Không tìm thấy đơn hàng: " + trimmed);
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

    public static OrderDtos.OrderResponseDto toOrderDto(Order order) {
        String methodLabel = order.getPaymentMethod() == PaymentMethod.BANK_TRANSFER
                ? "Chuyển khoản"
                : "COD";
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

        List<OrderDtos.OrderItemDto> items = order.getItems().stream()
                .map(i -> OrderDtos.OrderItemDto.builder()
                        .id(String.valueOf(i.getId()))
                        .variantId(i.getVariant() == null ? null : i.getVariant().getId())
                        .productId(i.getProductId())
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

        return OrderDtos.OrderResponseDto.builder()
                .id(order.getOrderCode())
                .numericId(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getRecipientName())
                .customerEmail(order.getRecipientEmail() == null ? "khachhang@luxe.vn" : order.getRecipientEmail())
                .customerPhone(order.getRecipientPhone())
                .date(SlugUtils.formatDate(order.getCreatedAt()))
                .createdAt(SlugUtils.formatDateTime(order.getCreatedAt()))
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
}

