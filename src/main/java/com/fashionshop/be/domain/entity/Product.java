package com.fashionshop.be.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @Column(name = "sku_prefix", length = 80)
    private String skuPrefix;

    @Column(length = 100)
    @Builder.Default
    private String brand = "LUXE";

    @Column(name = "base_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "discount_percent", nullable = false)
    @Builder.Default
    private int discountPercent = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_new", nullable = false)
    @Builder.Default
    private boolean isNew = false;

    @Column(name = "is_best_seller", nullable = false)
    @Builder.Default
    private boolean isBestSeller = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<ProductColor> productColors = new ArrayList<>();

    public void addProductColor(ProductColor productColor) {
        productColor.setProduct(this);
        this.productColors.add(productColor);
    }

    public BigDecimal getEffectivePrice() {
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        if (discountPercent <= 0) {
            return basePrice;
        }
        BigDecimal factor = BigDecimal.valueOf(100 - Math.min(discountPercent, 100))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return basePrice.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }

    public int getTotalStock() {
        return productColors.stream()
                .flatMap(pc -> pc.getVariants().stream())
                .mapToInt(ProductVariant::getStock)
                .sum();
    }
}
