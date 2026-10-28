package com.fashionshop.be.pattern.discount;

import com.fashionshop.be.domain.entity.Voucher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, Voucher voucher) {
        if (orderSubtotal == null || orderSubtotal.compareTo(BigDecimal.ZERO) <= 0 || voucher == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = voucher.getDiscountValue() == null ? BigDecimal.ZERO : voucher.getDiscountValue();
        return value.max(BigDecimal.ZERO).min(orderSubtotal);
    }
}

