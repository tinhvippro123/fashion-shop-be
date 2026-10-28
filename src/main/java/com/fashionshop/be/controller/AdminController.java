package com.fashionshop.be.controller;

import com.fashionshop.be.dto.AdminDtos;
import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.AdminManagementService;
import com.fashionshop.be.service.AnalyticsService;
import com.fashionshop.be.service.CatalogService;
import com.fashionshop.be.service.OrderService;
import com.fashionshop.be.service.StockImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AnalyticsService analyticsService;
    private final CatalogService catalogService;
    private final StockImportService stockImportService;
    private final OrderService orderService;
    private final AdminManagementService adminManagementService;

    // ================== ANALYTICS & REPORTS ==================

    @GetMapping({"/dashboard", "/reports"})
    public ApiResponse<AdminDtos.DashboardResponseDto> getDashboard() {
        return ApiResponse.ok(analyticsService.getDashboardAndReports());
    }

    // ================== CATEGORIES & OPTIONS ==================

    @GetMapping("/categories")
    public ApiResponse<List<CatalogDtos.CategoryDto>> getCategories() {
        return ApiResponse.ok(catalogService.getAllCategoriesFlat());
    }

    @PostMapping("/categories")
    public ApiResponse<CatalogDtos.CategoryDto> createCategory(
            @Valid @RequestBody CatalogDtos.CategoryUpsertRequest request
    ) {
        return ApiResponse.ok("Tạo danh mục thành công", catalogService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<CatalogDtos.CategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CatalogDtos.CategoryUpsertRequest request
    ) {
        return ApiResponse.ok("Cập nhật danh mục thành công", catalogService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        catalogService.deleteCategory(id);
        return ApiResponse.ok("Đã xóa danh mục", null);
    }

    @GetMapping("/options/suggestions")
    public ApiResponse<CatalogDtos.OptionSuggestionsDto> getOptionSuggestions() {
        return ApiResponse.ok(catalogService.getOptionSuggestions());
    }

    // ================== PRODUCTS & VARIANTS ==================

    @GetMapping("/products")
    public ApiResponse<List<CatalogDtos.ProductResponseDto>> getProducts() {
        return ApiResponse.ok(catalogService.getAllAdminProducts());
    }

    @GetMapping("/products/{id}")
    public ApiResponse<CatalogDtos.ProductResponseDto> getProductById(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.getProductDetail(String.valueOf(id)));
    }

    @PostMapping("/products")
    public ApiResponse<CatalogDtos.ProductResponseDto> createProduct(
            @Valid @RequestBody CatalogDtos.AdminProductUpsertRequest request
    ) {
        return ApiResponse.ok("Đã lưu sản phẩm thành công", catalogService.createAdminProduct(request));
    }

    @PutMapping("/products/{id}")
    public ApiResponse<CatalogDtos.ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CatalogDtos.AdminProductUpsertRequest request
    ) {
        return ApiResponse.ok("Đã cập nhật sản phẩm thành công", catalogService.updateAdminProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        catalogService.deleteAdminProduct(id);
        return ApiResponse.ok("Đã ẩn sản phẩm", null);
    }

    @GetMapping("/variants")
    public ApiResponse<List<CatalogDtos.VariantDto>> getAllVariants() {
        return ApiResponse.ok(catalogService.getAllVariantsForImport());
    }

    // ================== STOCK IMPORTS (NHẬP KHO) ==================

    @GetMapping("/stock-imports")
    public ApiResponse<List<AdminDtos.StockImportResponseDto>> getStockImports() {
        return ApiResponse.ok(stockImportService.getAllStockImports());
    }

    @GetMapping("/stock-imports/{id}")
    public ApiResponse<AdminDtos.StockImportResponseDto> getStockImportDetail(@PathVariable Long id) {
        return ApiResponse.ok(stockImportService.getStockImportById(id));
    }

    @PostMapping("/stock-imports")
    public ApiResponse<AdminDtos.StockImportResponseDto> createStockImport(
            @Valid @RequestBody AdminDtos.CreateStockImportRequest request
    ) {
        return ApiResponse.ok("Tạo phiếu nhập kho và cập nhật tồn kho thành công",
                stockImportService.createStockImport(request));
    }

    // ================== ORDERS ==================

    @GetMapping("/orders")
    public ApiResponse<List<OrderDtos.OrderResponseDto>> getOrders() {
        return ApiResponse.ok(orderService.getAllAdminOrders());
    }

    @GetMapping("/orders/{idOrCode}")
    public ApiResponse<OrderDtos.OrderResponseDto> getOrderDetail(@PathVariable String idOrCode) {
        return ApiResponse.ok(orderService.getOrderByIdOrCode(idOrCode));
    }

    @PutMapping("/orders/{idOrCode}/status")
    public ApiResponse<OrderDtos.OrderResponseDto> updateOrderStatus(
            @PathVariable String idOrCode,
            @Valid @RequestBody OrderDtos.UpdateOrderStatusRequest request
    ) {
        return ApiResponse.ok("Cập nhật trạng thái đơn hàng thành công",
                orderService.updateAdminOrderStatus(idOrCode, request));
    }

    // ================== VOUCHERS ==================

    @GetMapping("/vouchers")
    public ApiResponse<List<OrderDtos.VoucherDto>> getVouchers() {
        return ApiResponse.ok(adminManagementService.getAllVouchers());
    }

    @PostMapping("/vouchers")
    public ApiResponse<OrderDtos.VoucherDto> createVoucher(
            @Valid @RequestBody OrderDtos.VoucherUpsertRequest request
    ) {
        return ApiResponse.ok("Tạo mã giảm giá thành công", adminManagementService.createVoucher(request));
    }

    @PutMapping("/vouchers/{id}")
    public ApiResponse<OrderDtos.VoucherDto> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody OrderDtos.VoucherUpsertRequest request
    ) {
        return ApiResponse.ok("Cập nhật mã giảm giá thành công", adminManagementService.updateVoucher(id, request));
    }

    @PutMapping("/vouchers/{id}/action")
    public ApiResponse<OrderDtos.VoucherDto> voucherAction(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String action = body == null ? "" : body.getOrDefault("action", "");
        return ApiResponse.ok("Đã cập nhật trạng thái mã giảm giá",
                adminManagementService.changeVoucherAction(id, action));
    }

    // ================== CUSTOMERS ==================

    @GetMapping("/customers")
    public ApiResponse<List<AdminDtos.CustomerDto>> getCustomers() {
        return ApiResponse.ok(adminManagementService.getAllCustomers());
    }

    @GetMapping("/customers/{id}")
    public ApiResponse<AdminDtos.CustomerDto> getCustomerDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminManagementService.getCustomerDetail(id));
    }

    @PutMapping("/customers/{id}/status")
    public ApiResponse<AdminDtos.CustomerDto> updateCustomerStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body == null ? "ACTIVE" : body.getOrDefault("status", "ACTIVE");
        return ApiResponse.ok("Cập nhật trạng thái khách hàng thành công",
                adminManagementService.toggleCustomerStatus(id, status));
    }

    // ================== REVIEWS ==================

    @GetMapping("/reviews")
    public ApiResponse<List<CatalogDtos.ReviewDto>> getReviews() {
        return ApiResponse.ok(adminManagementService.getAllReviews());
    }

    @PutMapping("/reviews/{id}/reply")
    public ApiResponse<CatalogDtos.ReviewDto> replyReview(
            @PathVariable Long id,
            @Valid @RequestBody AdminDtos.ReplyTextRequest request
    ) {
        return ApiResponse.ok("Đã gửi phản hồi đánh giá", adminManagementService.replyReview(id, request.getReply()));
    }

    @PutMapping("/reviews/{id}/visibility")
    public ApiResponse<CatalogDtos.ReviewDto> toggleReviewVisibility(@PathVariable Long id) {
        return ApiResponse.ok("Đã cập nhật hiển thị đánh giá", adminManagementService.toggleReviewVisibility(id));
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        adminManagementService.deleteReview(id);
        return ApiResponse.ok("Đã xóa đánh giá", null);
    }

    // ================== CONTACTS ==================

    @GetMapping("/contacts")
    public ApiResponse<List<AdminDtos.ContactDto>> getContacts() {
        return ApiResponse.ok(adminManagementService.getAllContacts());
    }

    @GetMapping("/contacts/{id}")
    public ApiResponse<AdminDtos.ContactDto> getContactDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminManagementService.getContactDetail(id));
    }

    @PutMapping("/contacts/{id}/reply")
    public ApiResponse<AdminDtos.ContactDto> replyContact(
            @PathVariable Long id,
            @Valid @RequestBody AdminDtos.ReplyTextRequest request
    ) {
        return ApiResponse.ok("Đã phản hồi liên hệ", adminManagementService.replyContact(id, request.getReply()));
    }
}

