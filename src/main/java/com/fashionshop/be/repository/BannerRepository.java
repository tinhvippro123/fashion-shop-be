package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Banner;
import com.fashionshop.be.domain.enums.BannerPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByPositionAndIsActiveTrueOrderBySortOrderAsc(BannerPosition position);

    List<Banner> findByIsActiveTrueOrderBySortOrderAsc();

    List<Banner> findAllByOrderByPositionAscSortOrderAsc();
}

