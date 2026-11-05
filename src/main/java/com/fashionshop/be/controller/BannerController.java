package com.fashionshop.be.controller;

import com.fashionshop.be.domain.enums.BannerPosition;
import com.fashionshop.be.dto.BannerDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // ================= PUBLIC ENDPOINTS =================

    @GetMapping("/banners")
    public ApiResponse<List<BannerDtos.BannerResponseDto>> getPublicBanners(
            @RequestParam(required = false) BannerPosition position
    ) {
        return ApiResponse.ok(bannerService.getActiveBanners(position));
    }

    // ================= ADMIN ENDPOINTS =================

    @GetMapping("/admin/banners")
    public ApiResponse<List<BannerDtos.BannerResponseDto>> getAllBannersForAdmin() {
        return ApiResponse.ok(bannerService.getAllBannersAdmin());
    }

    @GetMapping("/admin/banners/{id}")
    public ApiResponse<BannerDtos.BannerResponseDto> getBannerById(@PathVariable Long id) {
        return ApiResponse.ok(bannerService.getBannerById(id));
    }

    @PostMapping("/admin/banners")
    public ApiResponse<BannerDtos.BannerResponseDto> createBanner(
            @Valid @RequestBody BannerDtos.CreateBannerRequest request
    ) {
        return ApiResponse.ok("Tạo banner thành công", bannerService.createBanner(request));
    }

    @PutMapping("/admin/banners/{id}")
    public ApiResponse<BannerDtos.BannerResponseDto> updateBanner(
            @PathVariable Long id,
            @Valid @RequestBody BannerDtos.UpdateBannerRequest request
    ) {
        return ApiResponse.ok("Cập nhật banner thành công", bannerService.updateBanner(id, request));
    }

    @PatchMapping("/admin/banners/{id}/status")
    public ApiResponse<BannerDtos.BannerResponseDto> toggleBannerStatus(@PathVariable Long id) {
        return ApiResponse.ok("Thay đổi trạng thái thành công", bannerService.toggleBannerStatus(id));
    }

    @DeleteMapping("/admin/banners/{id}")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ApiResponse.ok("Xóa banner thành công", null);
    }
}

