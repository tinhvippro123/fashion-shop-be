package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.StockImport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockImportRepository extends JpaRepository<StockImport, Long> {
    Optional<StockImport> findByImportCode(String importCode);
    List<StockImport> findAllByOrderByCreatedAtDesc();
}
