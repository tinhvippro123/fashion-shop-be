package com.fashionshop.be;

import com.fashionshop.be.domain.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusStateTest {

    @Test
    void validLifecycleTransitionsShouldBeAllowed() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.SHIPPING));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.COMPLETED));
        assertTrue(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void invalidReverseOrTerminalTransitionsShouldBeBlocked() {
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.SHIPPING));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PROCESSING));
        assertFalse(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.COMPLETED));
    }
}

