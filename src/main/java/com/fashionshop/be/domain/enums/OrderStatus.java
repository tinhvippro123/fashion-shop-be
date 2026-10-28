package com.fashionshop.be.domain.enums;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPING,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        if (target == null || this == target) {
            return false;
        }
        return switch (this) {
            case PENDING -> EnumSet.of(PROCESSING, CANCELLED).contains(target);
            case PROCESSING -> EnumSet.of(SHIPPING, CANCELLED).contains(target);
            case SHIPPING -> EnumSet.of(COMPLETED, CANCELLED).contains(target);
            case COMPLETED, CANCELLED -> false;
        };
    }

    public Set<OrderStatus> allowedNextStatuses() {
        return switch (this) {
            case PENDING -> EnumSet.of(PROCESSING, CANCELLED);
            case PROCESSING -> EnumSet.of(SHIPPING, CANCELLED);
            case SHIPPING -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }
}

