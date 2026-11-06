package com.fashionshop.be.controller;

import com.fashionshop.be.dto.CartDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.CartService;
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

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartDtos.CartResponseDto> getMyCart() {
        return ApiResponse.ok(cartService.getMyCart());
    }

    @PostMapping("/items")
    public ApiResponse<CartDtos.CartResponseDto> addItem(@Valid @RequestBody CartDtos.AddToCartRequest request) {
        return ApiResponse.ok("Đã thêm sản phẩm vào giỏ hàng", cartService.addToCart(request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartDtos.CartResponseDto> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody CartDtos.UpdateCartItemRequest request
    ) {
        return ApiResponse.ok("Đã cập nhật số lượng", cartService.updateCartItem(id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartDtos.CartResponseDto> removeItem(@PathVariable Long id) {
        return ApiResponse.ok("Đã xóa sản phẩm khỏi giỏ hàng", cartService.removeCartItem(id));
    }

    @DeleteMapping
    public ApiResponse<CartDtos.CartResponseDto> clearCart() {
        return ApiResponse.ok("Đã xóa toàn bộ giỏ hàng", cartService.clearCart());
    }

    @PostMapping("/sync")
    public ApiResponse<CartDtos.CartResponseDto> syncCart(@RequestBody CartDtos.SyncCartRequest request) {
        return ApiResponse.ok("Đã đồng bộ giỏ hàng", cartService.syncCart(request));
    }
}

