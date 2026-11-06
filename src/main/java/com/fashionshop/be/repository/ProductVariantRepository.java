package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);
    boolean existsBySizeId(Long sizeId);
    List<ProductVariant> findByStockLessThanEqualOrderByStockAsc(int threshold);

    /**
     * Khóa bi quan (Pessimistic Write Lock - SELECT ... FOR UPDATE)
     * Ngăn chặn hoàn toàn hiện tượng Race Condition khi 2 khách hàng cùng đặt mua 1 sản phẩm tại cùng 1 tích tắc.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);
}

