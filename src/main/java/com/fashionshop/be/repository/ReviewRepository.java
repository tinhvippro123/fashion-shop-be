package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(Long productId);
    List<Review> findAllByOrderByCreatedAtDesc();
    long countByProductIdAndIsVisibleTrue(Long productId);
}
