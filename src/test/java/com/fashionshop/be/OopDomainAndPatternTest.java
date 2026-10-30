package com.fashionshop.be;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.OrderItem;
import com.fashionshop.be.domain.entity.ProductVariant;
import com.fashionshop.be.domain.entity.Voucher;
import com.fashionshop.be.domain.enums.DiscountType;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.VoucherStatus;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.pattern.discount.DiscountStrategy;
import com.fashionshop.be.pattern.discount.DiscountStrategyFactory;
import com.fashionshop.be.pattern.discount.FixedAmountDiscountStrategy;
import com.fashionshop.be.pattern.discount.PercentageDiscountStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OopDomainAndPatternTest {

    @Test
    void productVariantShouldEncapsulateWeightedCostImportAndStockDeduction() {
        ProductVariant variant = ProductVariant.builder()
                .sku("LUXE-DRESS-BLK-S")
                .price(new BigDecimal("1250000"))
                .costPrice(new BigDecimal("500000"))
                .stock(10)
                .build();

        // Import 10 more units at 700,000 -> weighted average cost = 600,000, stock = 20
        variant.importStock(10, new BigDecimal("700000"));
        assertEquals(20, variant.getStock());
        assertEquals(0, new BigDecimal("600000.00").compareTo(variant.getCostPrice()));

        // Deduct 5 units -> stock = 15
        variant.deductStock(5);
        assertEquals(15, variant.getStock());

        // Oversell should throw BusinessException
        assertThrows(BusinessException.class, () -> variant.deductStock(25));

        // Restore 3 units on order cancellation -> stock = 18
        variant.restoreStock(3);
        assertEquals(18, variant.getStock());
    }

    @Test
    void discountStrategyFactoryAndPolymorphismShouldCalculateCorrectDiscounts() {
        DiscountStrategyFactory factory = new DiscountStrategyFactory(
                new PercentageDiscountStrategy(),
                new FixedAmountDiscountStrategy()
        );

        Voucher percentVoucher = Voucher.builder()
                .code("LUXE10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .maxDiscountAmount(new BigDecimal("80000"))
                .minOrderValue(new BigDecimal("500000"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .status(VoucherStatus.ACTIVE)
                .build();

        DiscountStrategy pctStrategy = factory.getStrategy(percentVoucher.getDiscountType());
        // 10% of 1,200,000 is 120,000 -> capped at 80,000
        BigDecimal pctDiscount = pctStrategy.calculateDiscount(new BigDecimal("1200000"), percentVoucher);
        assertEquals(0, new BigDecimal("80000").compareTo(pctDiscount));

        Voucher fixedVoucher = Voucher.builder()
                .code("WELCOME50")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("50000"))
                .minOrderValue(new BigDecimal("200000"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .status(VoucherStatus.ACTIVE)
                .build();

        DiscountStrategy fixedStrategy = factory.getStrategy(fixedVoucher.getDiscountType());
        BigDecimal fixedDiscount = fixedStrategy.calculateDiscount(new BigDecimal("450000"), fixedVoucher);
        assertEquals(0, new BigDecimal("50000").compareTo(fixedDiscount));

        fixedVoucher.setUsageLimit(1);
        fixedVoucher.consumeUsage();
        assertEquals(1, fixedVoucher.getUsedCount());
        assertEquals(VoucherStatus.EXPIRED, fixedVoucher.getStatus());

        fixedVoucher.restoreUsage();
        assertEquals(0, fixedVoucher.getUsedCount());
        assertEquals(VoucherStatus.ACTIVE, fixedVoucher.getStatus());
    }

    @Test
    void orderDomainShouldEncapsulateTotalsAndStateTransitions() {
        Order order = Order.builder()
                .orderCode("ORD-2026-0001")
                .recipientName("Nguyễn Văn A")
                .recipientPhone("0901234567")
                .shippingAddress("123 Lê Lợi, Q.1, TP.HCM")
                .build();

        order.addItem(OrderItem.builder()
                .productName("Đầm Lụa Cổ V")
                .colorName("Đen")
                .sizeName("S")
                .sku("LUXE-01")
                .quantity(2)
                .unitPrice(new BigDecimal("600000"))
                .totalPrice(new BigDecimal("1200000"))
                .build());

        order.recalculateTotals(new BigDecimal("30000"), new BigDecimal("80000"));
        assertEquals(0, new BigDecimal("1150000").compareTo(order.getTotalAmount()));

        order.transitionTo(OrderStatus.PROCESSING, null);
        order.transitionTo(OrderStatus.SHIPPING, null);
        order.transitionTo(OrderStatus.COMPLETED, null);

        // Terminal state cannot be cancelled
        assertThrows(BusinessException.class, () -> order.transitionTo(OrderStatus.CANCELLED, "Đổi ý"));
    }
}

