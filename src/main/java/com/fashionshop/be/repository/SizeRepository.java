package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SizeRepository extends JpaRepository<Size, Long> {
    Optional<Size> findByNameIgnoreCase(String name);
    List<Size> findAllByOrderBySortOrderAscIdAsc();
}
