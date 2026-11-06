package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.OrderItem;
import com.fashionshop.be.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.productId, COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "WHERE oi.productId IS NOT NULL AND oi.order.status != :cancelledStatus " +
            "GROUP BY oi.productId")
    List<Object[]> findTotalSoldByProductGrouped(@Param("cancelledStatus") OrderStatus cancelledStatus);

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "WHERE oi.productId = :productId AND oi.order.status != :cancelledStatus")
    Integer findTotalSoldByProductId(@Param("productId") Long productId, @Param("cancelledStatus") OrderStatus cancelledStatus);
}

