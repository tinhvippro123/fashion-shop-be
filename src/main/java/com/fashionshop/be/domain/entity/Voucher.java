package com.fashionshop.be.domain.entity;

import com.fashionshop.be.domain.enums.DiscountType;
import com.fashionshop.be.domain.enums.VoucherStatus;
import com.fashionshop.be.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 30)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_value", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 14, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private int usedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ACTIVE;

    public void validateApplicable(BigDecimal orderSubtotal, LocalDateTime now) {
        if (this.status == VoucherStatus.CANCELLED) {
            throw new BusinessException("Mã giảm giá " + this.code + " đã bị hủy");
        }
        if (this.status == VoucherStatus.EXPIRED || (this.endDate != null && now.isAfter(this.endDate))) {
            throw new BusinessException("Mã giảm giá " + this.code + " đã hết hạn");
        }
        if (this.status == VoucherStatus.SCHEDULED && this.startDate != null && now.isBefore(this.startDate)) {
            throw new BusinessException("Mã giảm giá " + this.code + " chưa đến thời gian áp dụng");
        }
        if (this.startDate != null && now.isBefore(this.startDate)) {
            throw new BusinessException("Mã giảm giá " + this.code + " chưa có hiệu lực");
        }
        if (this.usageLimit != null && this.usedCount >= this.usageLimit) {
            throw new BusinessException("Mã giảm giá " + this.code + " đã hết lượt sử dụng");
        }
        if (this.minOrderValue != null && orderSubtotal.compareTo(this.minOrderValue) < 0) {
            throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã " + this.code);
        }
    }

    public void consumeUsage() {
        if (this.usageLimit != null && this.usedCount >= this.usageLimit) {
            throw new BusinessException("Mã giảm giá " + this.code + " đã hết lượt sử dụng");
        }
        this.usedCount += 1;
        if (this.usageLimit != null && this.usedCount >= this.usageLimit) {
            this.status = VoucherStatus.EXPIRED;
        }
    }

    public void startNow() {
        LocalDateTime now = LocalDateTime.now();
        if (this.startDate == null || this.startDate.isAfter(now)) {
            this.startDate = now.minusMinutes(1);
        }
        this.status = VoucherStatus.ACTIVE;
    }

    public void endEarly() {
        this.endDate = LocalDateTime.now();
        this.status = VoucherStatus.EXPIRED;
    }

    public void cancel() {
        this.status = VoucherStatus.CANCELLED;
    }
}
