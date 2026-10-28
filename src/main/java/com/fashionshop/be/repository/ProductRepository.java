package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Product> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Product> findAllByOrderByCreatedAtDesc();
    long countByCategoryId(Long categoryId);
}

