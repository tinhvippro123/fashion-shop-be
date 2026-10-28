package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.OrderItem;
import com.fashionshop.be.domain.entity.ProductColor;
import com.fashionshop.be.domain.entity.StockImport;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.dto.AdminDtos;
import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.repository.OrderRepository;
import com.fashionshop.be.repository.ProductRepository;
import com.fashionshop.be.repository.ProductVariantRepository;
import com.fashionshop.be.repository.StockImportRepository;
import com.fashionshop.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StockImportRepository stockImportRepository;

    @Transactional(readOnly = true)
    public AdminDtos.DashboardResponseDto getDashboardAndReports() {
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();
        List<Order> validOrders = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .toList();

        BigDecimal totalRevenue = validOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCogs = validOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .map(i -> (i.getCostPrice() == null ? BigDecimal.ZERO : i.getCostPrice())
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = totalRevenue.subtract(totalCogs).max(BigDecimal.ZERO);

        BigDecimal totalImportCost = stockImportRepository.findAll().stream()
                .map(StockImport::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long customerCount = userRepository.countByRole(Role.CUSTOMER);
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
        long completedItemsSold = validOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .mapToLong(OrderItem::getQuantity)
                .sum();

        AdminDtos.DashboardStatsDto stats = AdminDtos.DashboardStatsDto.builder()
                .totalRevenue(totalRevenue)
                .totalProfit(totalProfit)
                .totalImportCost(totalImportCost)
                .revenueChange(18.4)
                .newCustomers(customerCount)
                .customerChange(12.5)
                .totalOrders(allOrders.size())
                .orderChange(15.2)
                .onlineSales(completedItemsSold)
                .onlineSalesChange(9)
                .pendingOrders(pendingCount)
                .build();

        List<AdminDtos.RecentOrderSummaryDto> recentOrders = allOrders.stream()
                .limit(6)
                .map(o -> {
                    String mappedStatus = switch (o.getStatus()) {
                        case PENDING, PROCESSING -> "pending";
                        case SHIPPING -> "shipping";
                        case COMPLETED -> "completed";
                        case CANCELLED -> "cancelled";
                    };
                    String initials = buildInitials(o.getRecipientName());
                    return AdminDtos.RecentOrderSummaryDto.builder()
                            .id(o.getOrderCode())
                            .orderCode(o.getOrderCode())
                            .customerName(o.getRecipientName())
                            .date(SlugUtils.formatDate(o.getCreatedAt()))
                            .amount(o.getTotalAmount())
                            .status(mappedStatus)
                            .avatarFallback(initials)
                            .build();
                })
                .toList();

        // Build 6-month chart data
        YearMonth currentYm = YearMonth.now();
        List<AdminDtos.ChartPointDto> chartData = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentYm.minusMonths(i);
            List<Order> monthOrders = validOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && YearMonth.from(o.getCreatedAt()).equals(ym))
                    .toList();

            BigDecimal monthRev = monthOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal monthCost = monthOrders.stream()
                    .flatMap(o -> o.getItems().stream())
                    .map(it -> (it.getCostPrice() == null ? BigDecimal.ZERO : it.getCostPrice())
                            .multiply(BigDecimal.valueOf(it.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            chartData.add(AdminDtos.ChartPointDto.builder()
                    .name("Thg " + ym.getMonthValue())
                    .total(monthRev)
                    .profit(monthRev.subtract(monthCost).max(BigDecimal.ZERO))
                    .orders(monthOrders.size())
                    .build());
        }

        // Category breakdown
        Map<String, BigDecimal> catRevMap = new LinkedHashMap<>();
        Map<String, Long> catCountMap = new LinkedHashMap<>();
        for (Order o : validOrders) {
            for (OrderItem oi : o.getItems()) {
                String catName = oi.getProductId() == null ? "Khác" :
                        productRepository.findById(oi.getProductId())
                                .map(p -> p.getCategory().getName())
                                .orElse("Khác");
                catRevMap.merge(catName, oi.getTotalPrice(), BigDecimal::add);
                catCountMap.merge(catName, (long) oi.getQuantity(), Long::sum);
            }
        }
        List<AdminDtos.CategoryShareDto> categorySales = catRevMap.entrySet().stream()
                .map(e -> AdminDtos.CategoryShareDto.builder()
                        .name(e.getKey())
                        .value(e.getValue())
                        .count(catCountMap.getOrDefault(e.getKey(), 0L))
                        .build())
                .toList();

        List<CatalogDtos.VariantDto> lowStockVariants = productVariantRepository
                .findByStockLessThanEqualOrderByStockAsc(5)
                .stream()
                .limit(10)
                .map(v -> {
                    ProductColor pc = v.getProductColor();
                    return CatalogDtos.VariantDto.builder()
                            .id(v.getId())
                            .sku(v.getSku())
                            .price(v.getPrice())
                            .costPrice(v.getCostPrice())
                            .stock(v.getStock())
                            .size(CatalogDtos.SizeDto.builder().id(v.getSize().getId()).name(v.getSize().getName()).build())
                            .colorName(pc.getColor().getName())
                            .productId(pc.getProduct().getId())
                            .productName(pc.getProduct().getName())
                            .build();
                })
                .toList();

        return AdminDtos.DashboardResponseDto.builder()
                .stats(stats)
                .recentOrders(recentOrders)
                .chartData(chartData)
                .categorySales(categorySales)
                .lowStockVariants(lowStockVariants)
                .build();
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "KH";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}

