package com.fashionshop.be.controller;

import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ApiResponse<List<CatalogDtos.CategoryDto>> getCategories() {
        return ApiResponse.ok(catalogService.getCategoryTree());
    }

    @GetMapping("/colors")
    public ApiResponse<List<CatalogDtos.ColorDto>> getColors() {
        return ApiResponse.ok(catalogService.getAllColors());
    }

    @GetMapping("/sizes")
    public ApiResponse<List<CatalogDtos.SizeDto>> getSizes() {
        return ApiResponse.ok(catalogService.getAllSizes());
    }

    @GetMapping("/products")
    public ApiResponse<List<CatalogDtos.ProductResponseDto>> getProducts(
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isNew,
            @RequestParam(required = false) Boolean isBestSeller,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(catalogService.filterPublicProducts(
                categorySlug, keyword, color, size, minPrice, maxPrice, isNew, isBestSeller, onSale, sort
        ));
    }

    @GetMapping("/products/{slug}")
    public ApiResponse<CatalogDtos.ProductResponseDto> getProductDetail(@PathVariable String slug) {
        return ApiResponse.ok(catalogService.getProductBySlug(slug));
    }

    @GetMapping("/products/{id}/reviews")
    public ApiResponse<List<CatalogDtos.ReviewDto>> getProductReviews(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.getProductReviews(id));
    }

    @PostMapping("/products/{id}/reviews")
    public ApiResponse<CatalogDtos.ReviewDto> addReview(
            @PathVariable Long id,
            @Valid @RequestBody CatalogDtos.CreateReviewRequest request
    ) {
        return ApiResponse.ok("Gửi đánh giá sản phẩm thành công", catalogService.addProductReview(id, request));
    }

    @GetMapping("/vouchers/active")
    public ApiResponse<List<OrderDtos.VoucherDto>> getActiveVouchers() {
        return ApiResponse.ok(catalogService.getActiveVouchers());
    }

    @PostMapping("/vouchers/validate")
    public ApiResponse<OrderDtos.ValidateVoucherResponse> validateVoucher(
            @Valid @RequestBody OrderDtos.ValidateVoucherRequest request
    ) {
        return ApiResponse.ok("Áp dụng mã giảm giá thành công", catalogService.validateVoucher(request));
    }
}

