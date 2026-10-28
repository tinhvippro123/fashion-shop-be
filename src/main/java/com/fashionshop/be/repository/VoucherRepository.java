package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Voucher;
import com.fashionshop.be.domain.enums.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Voucher> findByStatusOrderByCreatedAtDesc(VoucherStatus status);
    List<Voucher> findAllByOrderByCreatedAtDesc();
}
