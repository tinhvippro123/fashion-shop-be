package com.fashionshop.be.pattern.discount;

import com.fashionshop.be.domain.enums.DiscountType;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern:
 * Selects and returns the appropriate DiscountStrategy implementation based on DiscountType.
 */
@Component
@RequiredArgsConstructor
public class DiscountStrategyFactory {

    private final PercentageDiscountStrategy percentageDiscountStrategy;
    private final FixedAmountDiscountStrategy fixedAmountDiscountStrategy;

    public DiscountStrategy getStrategy(DiscountType discountType) {
        if (discountType == null) {
            throw new BusinessException(ErrorCode.INVALID_KEY);
        }
        return switch (discountType) {
            case PERCENTAGE -> percentageDiscountStrategy;
            case FIXED_AMOUNT -> fixedAmountDiscountStrategy;
        };
    }
}

