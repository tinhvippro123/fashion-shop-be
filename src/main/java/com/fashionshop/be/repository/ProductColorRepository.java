package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {
    boolean existsByColorId(Long colorId);
    long countByColorId(Long colorId);
}

