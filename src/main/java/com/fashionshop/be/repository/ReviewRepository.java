package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(Long productId);
    List<Review> findAllByOrderByCreatedAtDesc();
    long countByProductIdAndIsVisibleTrue(Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT r.product.id FROM Review r WHERE r.user.id = :userId")
    List<Long> findReviewedProductIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT r.product.id, COUNT(r.id), AVG(r.rating) FROM Review r WHERE r.isVisible = true GROUP BY r.product.id")
    List<Object[]> findReviewStatsGrouped();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isVisible = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
}

