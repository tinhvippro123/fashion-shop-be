package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.Review;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.entity.Voucher;
import com.fashionshop.be.domain.enums.DiscountType;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.domain.enums.UserStatus;
import com.fashionshop.be.domain.enums.VoucherStatus;
import com.fashionshop.be.dto.AdminDtos;
import com.fashionshop.be.dto.CatalogDtos;
import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.repository.OrderRepository;
import com.fashionshop.be.repository.ReviewRepository;
import com.fashionshop.be.repository.UserAddressRepository;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminManagementService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final CatalogService catalogService;

    // ================== VOUCHERS ==================

    @Transactional(readOnly = true)
    public List<OrderDtos.VoucherDto> getAllVouchers() {
        return voucherRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(CatalogService::toVoucherDto)
                .toList();
    }

    @Transactional
    public OrderDtos.VoucherDto createVoucher(OrderDtos.VoucherUpsertRequest request) {
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Mã giảm giá " + code + " đã tồn tại");
        }
        DiscountType type = DiscountType.valueOf(request.getDiscountType().trim().toUpperCase(Locale.ROOT));
        validateVoucherPayload(type, request);
        LocalDateTime start = request.getStartDate() == null ? LocalDateTime.now() : request.getStartDate();
        LocalDateTime end = request.getEndDate() == null ? start.plusDays(30) : request.getEndDate();
        if (end.isBefore(start)) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        LocalDateTime now = LocalDateTime.now();
        VoucherStatus initialStatus = end.isBefore(now)
                ? VoucherStatus.EXPIRED
                : (start.isAfter(now) ? VoucherStatus.SCHEDULED : VoucherStatus.ACTIVE);

        Voucher voucher = Voucher.builder()
                .code(code)
                .name((request.getName() == null || request.getName().isBlank()) ? "Voucher " + code : request.getName().trim())
                .discountType(type)
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue() == null ? BigDecimal.ZERO : request.getMinOrderValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .startDate(start)
                .endDate(end)
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .status(initialStatus)
                .build();

        return CatalogService.toVoucherDto(voucherRepository.save(voucher));
    }

    @Transactional
    public OrderDtos.VoucherDto updateVoucher(Long id, OrderDtos.VoucherUpsertRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá #" + id));
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        voucherRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Mã giảm giá " + code + " đã tồn tại");
                });
        DiscountType type = DiscountType.valueOf(request.getDiscountType().trim().toUpperCase(Locale.ROOT));
        validateVoucherPayload(type, request);

        LocalDateTime start = request.getStartDate() != null ? request.getStartDate() : voucher.getStartDate();
        LocalDateTime end = request.getEndDate() != null ? request.getEndDate() : voucher.getEndDate();
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        voucher.setCode(code);
        if (request.getName() != null && !request.getName().isBlank()) {
            voucher.setName(request.getName().trim());
        }
        voucher.setDiscountType(type);
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue() == null ? BigDecimal.ZERO : request.getMinOrderValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getStartDate() != null) voucher.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) voucher.setEndDate(request.getEndDate());
        voucher.setUsageLimit(request.getUsageLimit());

        if (voucher.getStatus() != VoucherStatus.CANCELLED) {
            LocalDateTime now = LocalDateTime.now();
            if ((voucher.getEndDate() != null && now.isAfter(voucher.getEndDate()))
                    || (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit())) {
                voucher.setStatus(VoucherStatus.EXPIRED);
            } else if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
                voucher.setStatus(VoucherStatus.SCHEDULED);
            } else {
                voucher.setStatus(VoucherStatus.ACTIVE);
            }
        }

        return CatalogService.toVoucherDto(voucherRepository.save(voucher));
    }

    private void validateVoucherPayload(DiscountType type, OrderDtos.VoucherUpsertRequest request) {
        if (request.getDiscountValue() == null || request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Mức giảm giá phải lớn hơn 0");
        }
        if (type == DiscountType.PERCENTAGE && request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("Mức giảm theo phần trăm không được vượt quá 100%");
        }
    }

    @Transactional
    public OrderDtos.VoucherDto changeVoucherAction(Long id, String action) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá #" + id));
        switch (action.trim().toUpperCase(Locale.ROOT)) {
            case "START_NOW" -> voucher.startNow();
            case "END_EARLY" -> voucher.endEarly();
            case "CANCEL" -> voucher.cancel();
            default -> throw new BusinessException("Thao tác mã giảm giá không hợp lệ: " + action);
        }
        return CatalogService.toVoucherDto(voucherRepository.save(voucher));
    }

    // ================== CUSTOMERS ==================

    @Transactional(readOnly = true)
    public List<AdminDtos.CustomerDto> getAllCustomers() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.CUSTOMER)
                .stream()
                .map(u -> toCustomerDto(u, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminDtos.CustomerDto getCustomerDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng #" + id));
        return toCustomerDto(user, true);
    }

    @Transactional
    public AdminDtos.CustomerDto toggleCustomerStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng #" + id));
        if ("BANNED".equalsIgnoreCase(status)) {
            user.lockAccount();
        } else {
            user.unlockAccount();
        }
        return toCustomerDto(userRepository.save(user), true);
    }

    private AdminDtos.CustomerDto toCustomerDto(User user, boolean includeDetails) {
        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        BigDecimal totalSpent = userOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String tier = totalSpent.compareTo(new BigDecimal("5000000")) >= 0 ? "Kim cương"
                : totalSpent.compareTo(new BigDecimal("2000000")) >= 0 ? "Vàng" : "Thành viên";

        List<AdminDtos.CustomerAddressItemDto> addrs = includeDetails
                ? userAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(user.getId()).stream()
                .map(a -> AdminDtos.CustomerAddressItemDto.builder()
                        .id(a.getId())
                        .recipientName(a.getRecipientName())
                        .isDefault(a.isDefault())
                        .address(a.getFullAddress())
                        .phone(a.getPhone())
                        .build())
                .toList()
                : List.of();

        List<AdminDtos.CustomerOrderHistoryDto> history = includeDetails
                ? userOrders.stream()
                .map(o -> AdminDtos.CustomerOrderHistoryDto.builder()
                        .id(o.getOrderCode())
                        .date(SlugUtils.formatDate(o.getCreatedAt()))
                        .status(o.getStatus().name())
                        .total(SlugUtils.formatVnd(o.getTotalAmount()))
                        .items(o.getItems().size())
                        .build())
                .toList()
                : List.of();

        return AdminDtos.CustomerDto.builder()
                .id(String.valueOf(user.getId()))
                .numericId(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone() == null || user.getPhone().isBlank() ? "Chưa cập nhật" : user.getPhone())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth() == null ? null : user.getDateOfBirth().toString())
                .orders(userOrders.size())
                .totalOrders(userOrders.size())
                .totalSpent(SlugUtils.formatVnd(totalSpent))
                .totalSpentRaw(totalSpent)
                .tier(tier)
                .accountStatus(user.getStatus() == null ? UserStatus.ACTIVE.name() : user.getStatus().name())
                .joinedDate(SlugUtils.formatDate(user.getCreatedAt()))
                .addresses(addrs)
                .orderHistory(history)
                .build();
    }

    // ================== REVIEWS ==================

    @Transactional(readOnly = true)
    public List<CatalogDtos.ReviewDto> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(catalogService::toReviewDto)
                .toList();
    }

    @Transactional
    public CatalogDtos.ReviewDto replyReview(Long id, String reply) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá #" + id));
        review.setAdminReply(reply.trim());
        return catalogService.toReviewDto(reviewRepository.save(review));
    }

    @Transactional
    public CatalogDtos.ReviewDto toggleReviewVisibility(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá #" + id));
        review.setVisible(!review.isVisible());
        return catalogService.toReviewDto(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá #" + id));
        reviewRepository.delete(review);
    }
}

