package com.fashionshop.be.pattern.discount;

import com.fashionshop.be.domain.entity.Voucher;

import java.math.BigDecimal;

/**
 * Strategy Pattern (OOP Abstraction & Polymorphism):
 * Abstracts discount calculation algorithms for different voucher types.
 */
public interface DiscountStrategy {
    BigDecimal calculateDiscount(BigDecimal orderSubtotal, Voucher voucher);
}

