package com.fashionshop.be.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class AdminDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockImportItemRequest {
        @NotNull(message = "Vui lòng chọn biến thể sản phẩm")
        private Long variantId;

        @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
        private int quantity;

        @NotNull(message = "Giá vốn đơn vị không được để trống")
        private BigDecimal unitCost;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateStockImportRequest {
        @NotBlank(message = "Tên nhà cung cấp không được để trống")
        private String supplierName;

        private String note;

        @NotEmpty(message = "Phiếu nhập kho phải có ít nhất 1 mặt hàng")
        @Valid
        private List<StockImportItemRequest> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockImportItemDto {
        private Long id;
        private Long variantId;
        private String sku;
        private String productName;
        private String colorName;
        private String sizeName;
        private int quantity;
        private BigDecimal unitCost;
        private BigDecimal subtotal;
        private int currentStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockImportResponseDto {
        private Long id;
        private String importCode;
        private String supplierName;
        private String note;
        private BigDecimal totalCost;
        private String formattedTotalCost;
        private String createdByEmail;
        private String createdAt;
        private int totalQuantity;
        private int itemCount;
        private List<StockImportItemDto> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAddressItemDto {
        private Long id;
        private boolean isDefault;
        private String address;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerOrderHistoryDto {
        private String id;
        private String date;
        private String status;
        private String total;
        private int items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDto {
        private String id;
        private Long numericId;
        private String name;
        private String email;
        private String phone;
        private int orders;
        private int totalOrders;
        private String totalSpent;
        private BigDecimal totalSpentRaw;
        private String tier;
        private String accountStatus;
        private String joinedDate;
        private List<CustomerAddressItemDto> addresses;
        private List<CustomerOrderHistoryDto> orderHistory;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateContactRequest {
        @NotBlank(message = "Họ tên không được để trống")
        private String fullName;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        private String phone;

        @NotBlank(message = "Tiêu đề không được để trống")
        private String subject;

        @NotBlank(message = "Nội dung liên hệ không được để trống")
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyTextRequest {
        @NotBlank(message = "Nội dung phản hồi không được để trống")
        private String reply;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactDto {
        private String id;
        private Long numericId;
        private String name;
        private String email;
        private String phone;
        private String subject;
        private String message;
        private String reply;
        private String date;
        private String status;
        private String rawStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatsDto {
        private BigDecimal totalRevenue;
        private BigDecimal totalProfit;
        private BigDecimal totalImportCost;
        private double revenueChange;
        private long newCustomers;
        private double customerChange;
        private long totalOrders;
        private double orderChange;
        private long onlineSales;
        private long onlineSalesChange;
        private long pendingOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderSummaryDto {
        private String id;
        private String orderCode;
        private String customerName;
        private String date;
        private BigDecimal amount;
        private String status; // pending, completed, shipping, cancelled
        private String avatarFallback;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPointDto {
        private String name;
        private BigDecimal total;
        private BigDecimal profit;
        private long orders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryShareDto {
        private String name;
        private BigDecimal value;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardResponseDto {
        private DashboardStatsDto stats;
        private List<RecentOrderSummaryDto> recentOrders;
        private List<ChartPointDto> chartData;
        private List<CategoryShareDto> categorySales;
        private List<CatalogDtos.VariantDto> lowStockVariants;
    }
}
