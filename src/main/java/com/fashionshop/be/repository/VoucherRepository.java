package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.Voucher;
import com.fashionshop.be.domain.enums.VoucherStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Voucher> findByStatusOrderByCreatedAtDesc(VoucherStatus status);
    List<Voucher> findAllByOrderByCreatedAtDesc();

    /**
     * Khóa bi quan (Pessimistic Write Lock) để bảo vệ số lượt sử dụng voucher khỏi Race Condition
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE LOWER(v.code) = LOWER(:code)")
    Optional<Voucher> findByCodeIgnoreCaseForUpdate(@Param("code") String code);
}

