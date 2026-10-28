package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColorRepository extends JpaRepository<Color, Long> {
    Optional<Color> findByNameIgnoreCase(String name);
    List<Color> findAllByOrderByIdAsc();
}
