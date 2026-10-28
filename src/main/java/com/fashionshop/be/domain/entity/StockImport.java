package com.fashionshop.be.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "stock_imports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockImport extends BaseEntity {

    @Column(name = "import_code", nullable = false, unique = true, length = 60)
    private String importCode;

    @Column(name = "supplier_name", nullable = false, length = 180)
    private String supplierName;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "total_cost", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "created_by_email", nullable = false, length = 180)
    private String createdByEmail;

    @OneToMany(mappedBy = "stockImport", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<StockImportItem> items = new ArrayList<>();

    public void addItem(StockImportItem item) {
        item.setStockImport(this);
        this.items.add(item);
        recalculateTotalCost();
    }

    public void recalculateTotalCost() {
        this.totalCost = this.items.stream()
                .map(StockImportItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

