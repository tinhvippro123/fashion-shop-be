package com.fashionshop.be.pattern.discount;

import com.fashionshop.be.domain.entity.Voucher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, Voucher voucher) {
        if (orderSubtotal == null || orderSubtotal.compareTo(BigDecimal.ZERO) <= 0 || voucher == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rawDiscount = orderSubtotal
                .multiply(voucher.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        if (voucher.getMaxDiscountAmount() != null
                && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                && rawDiscount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            rawDiscount = voucher.getMaxDiscountAmount();
        }
        return rawDiscount.min(orderSubtotal);
    }
}

