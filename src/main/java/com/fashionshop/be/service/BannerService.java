package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Banner;
import com.fashionshop.be.domain.enums.BannerPosition;
import com.fashionshop.be.dto.BannerDtos;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public List<BannerDtos.BannerResponseDto> getActiveBanners(BannerPosition position) {
        List<Banner> banners;
        if (position != null) {
            banners = bannerRepository.findByPositionAndIsActiveTrueOrderBySortOrderAsc(position);
        } else {
            banners = bannerRepository.findByIsActiveTrueOrderBySortOrderAsc();
        }
        return banners.stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BannerDtos.BannerResponseDto> getAllBannersAdmin() {
        return bannerRepository.findAllByOrderByPositionAscSortOrderAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BannerDtos.BannerResponseDto getBannerById(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với ID: " + id));
        return mapToDto(banner);
    }

    @Transactional
    public BannerDtos.BannerResponseDto createBanner(BannerDtos.CreateBannerRequest request) {
        Banner banner = Banner.builder()
                .title(request.getTitle().trim())
                .subtitle(request.getSubtitle() != null ? request.getSubtitle().trim() : null)
                .imageUrl(request.getImageUrl().trim())
                .targetUrl(request.getTargetUrl() != null ? request.getTargetUrl().trim() : null)
                .position(request.getPosition())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Banner saved = bannerRepository.save(banner);
        return mapToDto(saved);
    }

    @Transactional
    public BannerDtos.BannerResponseDto updateBanner(Long id, BannerDtos.UpdateBannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với ID: " + id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            banner.setTitle(request.getTitle().trim());
        }
        if (request.getSubtitle() != null) {
            banner.setSubtitle(request.getSubtitle().trim());
        }
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            banner.setImageUrl(request.getImageUrl().trim());
        }
        if (request.getTargetUrl() != null) {
            banner.setTargetUrl(request.getTargetUrl().trim());
        }
        if (request.getPosition() != null) {
            banner.setPosition(request.getPosition());
        }
        if (request.getSortOrder() != null) {
            banner.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            banner.setActive(request.getIsActive());
        }

        Banner updated = bannerRepository.save(banner);
        return mapToDto(updated);
    }

    @Transactional
    public BannerDtos.BannerResponseDto toggleBannerStatus(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với ID: " + id));
        banner.setActive(!banner.isActive());
        Banner updated = bannerRepository.save(banner);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với ID: " + id));
        bannerRepository.delete(banner);
    }

    private BannerDtos.BannerResponseDto mapToDto(Banner banner) {
        return BannerDtos.BannerResponseDto.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(banner.getImageUrl())
                .targetUrl(banner.getTargetUrl())
                .position(banner.getPosition())
                .sortOrder(banner.getSortOrder())
                .isActive(banner.isActive())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }
}

