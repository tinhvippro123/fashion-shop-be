package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}

