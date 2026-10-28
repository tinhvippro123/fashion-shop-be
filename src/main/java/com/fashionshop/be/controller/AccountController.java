package com.fashionshop.be.controller;

import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.AccountService;
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
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/profile")
    public ApiResponse<AuthDtos.UserDto> getProfile() {
        return ApiResponse.ok(accountService.getProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<AuthDtos.UserDto> updateProfile(@Valid @RequestBody AuthDtos.UpdateProfileRequest request) {
        return ApiResponse.ok("Cập nhật thông tin tài khoản thành công", accountService.updateProfile(request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        accountService.changePassword(request);
        return ApiResponse.ok("Đổi mật khẩu thành công", null);
    }

    @GetMapping("/addresses")
    public ApiResponse<List<AuthDtos.AddressDto>> getAddresses() {
        return ApiResponse.ok(accountService.getMyAddresses());
    }

    @PostMapping("/addresses")
    public ApiResponse<AuthDtos.AddressDto> addAddress(@Valid @RequestBody AuthDtos.AddressUpsertRequest request) {
        return ApiResponse.ok("Thêm địa chỉ thành công", accountService.addAddress(request));
    }

    @PutMapping("/addresses/{id}")
    public ApiResponse<AuthDtos.AddressDto> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AuthDtos.AddressUpsertRequest request
    ) {
        return ApiResponse.ok("Cập nhật địa chỉ thành công", accountService.updateAddress(id, request));
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        accountService.deleteAddress(id);
        return ApiResponse.ok("Đã xóa địa chỉ", null);
    }

    @GetMapping("/wishlists")
    public ApiResponse<List<CatalogDtos.ProductResponseDto>> getWishlists() {
        return ApiResponse.ok(accountService.getMyWishlist());
    }

    @PostMapping("/wishlists/{productId}")
    public ApiResponse<Map<String, Boolean>> toggleWishlist(@PathVariable Long productId) {
        boolean favorited = accountService.toggleWishlist(productId);
        String msg = favorited ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích";
        return ApiResponse.ok(msg, Map.of("favorited", favorited));
    }
}
