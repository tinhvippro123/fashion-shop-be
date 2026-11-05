package com.fashionshop.be.domain.entity;

import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.PaymentMethod;
import com.fashionshop.be.domain.enums.PaymentStatus;
import com.fashionshop.be.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 25)
    private String recipientPhone;

    @Column(name = "recipient_email", length = 180)
    private String recipientEmail;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "voucher_code", length = 50)
    private String voucherCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public void recalculateTotals(BigDecimal shipping, BigDecimal discount) {
        this.subtotal = this.items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.shippingFee = shipping == null ? BigDecimal.ZERO : shipping.max(BigDecimal.ZERO);
        this.discountAmount = discount == null ? BigDecimal.ZERO : discount.max(BigDecimal.ZERO);

        BigDecimal rawTotal = this.subtotal.add(this.shippingFee).subtract(this.discountAmount);
        this.totalAmount = rawTotal.max(BigDecimal.ZERO);
    }

    public void transitionTo(OrderStatus nextStatus, String reason) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new BusinessException("Không thể chuyển trạng thái đơn hàng từ " + this.status + " sang " + nextStatus);
        }
        this.status = nextStatus;
        if (nextStatus == OrderStatus.PROCESSING && (this.paymentMethod == PaymentMethod.BANK_TRANSFER || this.paymentMethod == PaymentMethod.VNPAY)) {
            this.paymentStatus = PaymentStatus.PAID;
        }
        if (nextStatus == OrderStatus.COMPLETED) {
            this.paymentStatus = PaymentStatus.PAID;
        }
        if (nextStatus == OrderStatus.CANCELLED) {
            this.cancelReason = (reason == null || reason.isBlank()) ? "Hủy đơn hàng" : reason;
            if (this.paymentStatus == PaymentStatus.PAID) {
                this.paymentStatus = PaymentStatus.REFUNDED;
            }
        }
    }
}

