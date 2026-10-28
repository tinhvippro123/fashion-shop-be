package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCodeIgnoreCase(String orderCode);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findAllByOrderByCreatedAtDesc();
    long countByStatus(OrderStatus status);
}

