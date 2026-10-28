package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Product;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.entity.UserAddress;
import com.fashionshop.be.domain.entity.Wishlist;
import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.repository.ProductRepository;
import com.fashionshop.be.repository.UserAddressRepository;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.repository.WishlistRepository;
import com.fashionshop.be.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final CatalogService catalogService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthDtos.UserDto getProfile() {
        User user = securityUtils.getCurrentUserRequired();
        return AuthService.toUserDto(user);
    }

    @Transactional
    public AuthDtos.UserDto updateProfile(AuthDtos.UpdateProfileRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        user.setFullName(request.getFullName().trim());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        return AuthService.toUserDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(AuthDtos.ChangePasswordRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<AuthDtos.AddressDto> getMyAddresses() {
        User user = securityUtils.getCurrentUserRequired();
        return userAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId())
                .stream()
                .map(this::toAddressDto)
                .toList();
    }

    @Transactional
    public AuthDtos.AddressDto addAddress(AuthDtos.AddressUpsertRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        List<UserAddress> existing = userAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId());
        boolean makeDefault = request.isDefault() || existing.isEmpty();
        if (makeDefault) {
            existing.forEach(a -> a.setDefault(false));
            userAddressRepository.saveAll(existing);
        }

        String fullAddr = buildFullAddress(request);
        UserAddress address = UserAddress.builder()
                .user(user)
                .recipientName(request.getRecipientName().trim())
                .phone(request.getPhone().trim())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .streetAddress(request.getStreetAddress())
                .fullAddress(fullAddr)
                .isDefault(makeDefault)
                .build();

        return toAddressDto(userAddressRepository.save(address));
    }

    @Transactional
    public AuthDtos.AddressDto updateAddress(Long id, AuthDtos.AddressUpsertRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        UserAddress address = userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (request.isDefault()) {
            List<UserAddress> existing = userAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId());
            existing.forEach(a -> a.setDefault(a.getId().equals(id)));
            userAddressRepository.saveAll(existing);
        }

        address.setRecipientName(request.getRecipientName().trim());
        address.setPhone(request.getPhone().trim());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setStreetAddress(request.getStreetAddress());
        address.setFullAddress(buildFullAddress(request));
        address.setDefault(request.isDefault() || address.isDefault());
        return toAddressDto(userAddressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id) {
        User user = securityUtils.getCurrentUserRequired();
        UserAddress address = userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));
        userAddressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ProductResponseDto> getMyWishlist() {
        User user = securityUtils.getCurrentUserRequired();
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(w -> catalogService.toProductDto(w.getProduct(), false))
                .toList();
    }

    @Transactional
    public boolean toggleWishlist(Long productId) {
        User user = securityUtils.getCurrentUserRequired();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        return wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .map(existing -> {
                    wishlistRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
                    return true;
                });
    }

    private String buildFullAddress(AuthDtos.AddressUpsertRequest req) {
        if (req.getFullAddress() != null && !req.getFullAddress().isBlank()) {
            return req.getFullAddress().trim();
        }
        String joined = Stream.of(req.getStreetAddress(), req.getWard(), req.getDistrict(), req.getProvince())
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        if (joined.isBlank()) {
            throw new BusinessException("Địa chỉ chi tiết không được để trống");
        }
        return joined;
    }

    private AuthDtos.AddressDto toAddressDto(UserAddress a) {
        return AuthDtos.AddressDto.builder()
                .id(a.getId())
                .recipientName(a.getRecipientName())
                .phone(a.getPhone())
                .province(a.getProvince())
                .district(a.getDistrict())
                .ward(a.getWard())
                .streetAddress(a.getStreetAddress())
                .fullAddress(a.getFullAddress())
                .isDefault(a.isDefault())
                .build();
    }
}
