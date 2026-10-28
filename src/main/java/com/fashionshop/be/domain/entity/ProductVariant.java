package com.fashionshop.be.domain.entity;

import com.fashionshop.be.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Rich Domain Entity demonstrating OOP Encapsulation:
 * All stock and cost-price mutations pass through invariant-checked domain methods.
 */
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_color_id", nullable = false)
    private ProductColor productColor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    @Column(nullable = false, unique = true, length = 120)
    private String sku;

    @Column(nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "cost_price", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;

    public void importStock(int quantity, BigDecimal unitCost) {
        if (quantity <= 0) {
            throw new BusinessException("Số lượng nhập kho phải lớn hơn 0");
        }
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Giá vốn nhập kho không được âm");
        }

        BigDecimal currentTotalValue = (this.costPrice == null ? BigDecimal.ZERO : this.costPrice)
                .multiply(BigDecimal.valueOf(Math.max(this.stock, 0)));
        BigDecimal incomingValue = unitCost.multiply(BigDecimal.valueOf(quantity));
        int newStock = Math.max(this.stock, 0) + quantity;

        this.costPrice = currentTotalValue.add(incomingValue)
                .divide(BigDecimal.valueOf(newStock), 2, RoundingMode.HALF_UP);
        this.stock = newStock;
    }

    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Số lượng đặt mua phải lớn hơn 0");
        }
        if (this.stock < quantity) {
            throw new BusinessException("Biến thể " + this.sku + " không đủ số lượng tồn kho (còn " + this.stock + ")");
        }
        this.stock -= quantity;
    }

    public void restoreStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Số lượng hoàn kho phải lớn hơn 0");
        }
        this.stock += quantity;
    }
}

