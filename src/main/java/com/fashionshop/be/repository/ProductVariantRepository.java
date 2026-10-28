package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);
    List<ProductVariant> findByStockLessThanEqualOrderByStockAsc(int threshold);
}
