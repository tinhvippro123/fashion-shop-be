package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.StockImportItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockImportItemRepository extends JpaRepository<StockImportItem, Long> {
}
