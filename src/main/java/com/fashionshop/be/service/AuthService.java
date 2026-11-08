package com.fashionshop.be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.entity.UserAddress;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.domain.enums.UserStatus;
import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ErrorCode;
import com.fashionshop.be.repository.UserAddressRepository;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int OTP_EXPIRY_SECONDS = 300;
    private static final int OTP_RESEND_COOLDOWN_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Map<String, OtpEntry> forgotOtpStore = new ConcurrentHashMap<>();

    private record OtpEntry(String code, Instant expiresAt, Instant createdAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        boolean isInCooldown() {
            return Instant.now().isBefore(createdAt.plusSeconds(OTP_RESEND_COOLDOWN_SECONDS));
        }
    }

    // ================= REGISTER & REGISTER OTP =================

    @Transactional(readOnly = true)
    public AuthDtos.SendOtpResponse sendRegisterOtp(AuthDtos.SendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        OtpEntry existing = otpStore.get(email);
        if (existing != null && existing.isInCooldown()) {
            throw new BusinessException(ErrorCode.OTP_COOLDOWN);
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        otpStore.put(email, new OtpEntry(code, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS), Instant.now()));

        boolean sentRealEmail = emailService.sendOtpEmail(
                email,
                code,
                "Đăng Ký Tài Khoản",
                "Bạn đang thực hiện đăng ký tài khoản mới tại LUXE Fashion"
        );

        return AuthDtos.SendOtpResponse.builder()
                .email(email)
                .expiresInSeconds(OTP_EXPIRY_SECONDS)
                .sentViaSmtp(sentRealEmail)
                .otpCode(null)
                .build();
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String submittedOtp = request.getOtpCode() == null ? "" : request.getOtpCode().trim();
        OtpEntry otpEntry = otpStore.get(email);
        if (otpEntry == null || otpEntry.isExpired()) {
            otpStore.remove(email);
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }
        if (!otpEntry.code().equals(submittedOtp)) {
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }
        otpStore.remove(email);

        String normalizedGender = normalizeGender(request.getGender());

        User user = User.builder()
                .email(email)
                .fullName(request.getFullName().trim())
                .phone(request.getPhone() == null ? "" : request.getPhone().trim())
                .gender(normalizedGender)
                .dateOfBirth(request.getDateOfBirth())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        // Tự động lưu sổ địa chỉ mặc định đầu tiên nếu đăng ký có điền địa chỉ
        if (request.getProvince() != null && !request.getProvince().isBlank()) {
            String street = request.getStreetAddress() != null ? request.getStreetAddress().trim() : "";
            String ward = request.getWard() != null ? request.getWard().trim() : "";
            String district = request.getDistrict() != null ? request.getDistrict().trim() : "";
            String province = request.getProvince().trim();

            String fullAddr = Stream.of(street, ward, district, province)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(", "));

            UserAddress address = UserAddress.builder()
                    .user(saved)
                    .recipientName(saved.getFullName())
                    .phone(saved.getPhone() != null && !saved.getPhone().isBlank() ? saved.getPhone() : "0900000000")
                    .province(province)
                    .district(district)
                    .ward(ward)
                    .streetAddress(street)
                    .fullAddress(fullAddr.isBlank() ? province : fullAddr)
                    .isDefault(true)
                    .build();
            userAddressRepository.save(address);
        }

        String token = jwtTokenProvider.generateToken(saved);
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .user(toUserDto(saved))
                .build();
    }

    // ================= FORGOT PASSWORD =================

    @Transactional(readOnly = true)
    public AuthDtos.SendOtpResponse sendForgotPasswordOtp(AuthDtos.ForgotPasswordSendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        OtpEntry existing = forgotOtpStore.get(email);
        if (existing != null && existing.isInCooldown()) {
            throw new BusinessException(ErrorCode.OTP_COOLDOWN);
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        forgotOtpStore.put(email, new OtpEntry(code, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS), Instant.now()));

        boolean sentRealEmail = emailService.sendOtpEmail(
                email,
                code,
                "Khôi Phục Mật Khẩu",
                "Hệ thống nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn"
        );

        return AuthDtos.SendOtpResponse.builder()
                .email(email)
                .expiresInSeconds(OTP_EXPIRY_SECONDS)
                .sentViaSmtp(sentRealEmail)
                .otpCode(null)
                .build();
    }

    @Transactional
    public void resetPassword(AuthDtos.ForgotPasswordResetRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String submittedOtp = request.getOtpCode() == null ? "" : request.getOtpCode().trim();
        OtpEntry otpEntry = forgotOtpStore.get(email);
        if (otpEntry == null || otpEntry.isExpired()) {
            forgotOtpStore.remove(email);
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }
        if (!otpEntry.code().equals(submittedOtp)) {
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }
        forgotOtpStore.remove(email);

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("[LUXE AUTH] Đã cập nhật mật khẩu mới thành công cho email: {}", email);
    }

    // ================= GOOGLE LOGIN =================

    @Transactional
    public AuthDtos.AuthResponse loginWithGoogle(AuthDtos.GoogleLoginRequest request) {
        String credential = request.getCredential();
        if (credential == null || credential.isBlank()) {
            throw new BusinessException(ErrorCode.GOOGLE_AUTH_FAILED);
        }

        try {
            String[] parts = credential.split("\\.");
            if (parts.length < 2) {
                throw new BusinessException(ErrorCode.GOOGLE_AUTH_FAILED);
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(payloadJson);

            String email = payload.path("email").asText("").trim().toLowerCase();
            String name = payload.path("name").asText("").trim();
            String picture = payload.path("picture").asText(null);

            if (email.isBlank()) {
                throw new BusinessException(ErrorCode.GOOGLE_EMAIL_NOT_FOUND);
            }

            User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

            if (user != null) {
                if (user.getStatus() == UserStatus.BANNED) {
                    throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
                }
                // Cập nhật avatar nếu trước đó chưa có
                if ((user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) && picture != null) {
                    user.setAvatarUrl(picture);
                    userRepository.save(user);
                }
            } else {
                // Tự động tạo tài khoản mới nếu chưa tồn tại
                user = User.builder()
                        .email(email)
                        .fullName(!name.isBlank() ? name : "Khách hàng Google")
                        .avatarUrl(picture)
                        .gender("FEMALE")
                        .phone("")
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(Role.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .build();
                user = userRepository.save(user);
                log.info("[LUXE GOOGLE LOGIN] Đã tự động tạo tài khoản mới cho Google User: {}", email);
            }

            String token = jwtTokenProvider.generateToken(user);
            return AuthDtos.AuthResponse.builder()
                    .token(token)
                    .user(toUserDto(user))
                    .build();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[LUXE GOOGLE LOGIN] Lỗi giải mã Google Token: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.GOOGLE_AUTH_FAILED);
        }
    }

    // ================= STANDARD LOGIN =================

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        String token = jwtTokenProvider.generateToken(user);
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .user(toUserDto(user))
                .build();
    }

    public static String normalizeGender(String raw) {
        if (raw == null || raw.isBlank()) {
            return "FEMALE";
        }
        String upper = raw.trim().toUpperCase();
        if ("NAM".equals(upper) || "MALE".equals(upper)) {
            return "MALE";
        }
        if ("KHÁC".equals(upper) || "KHAC".equals(upper) || "OTHER".equals(upper)) {
            return "OTHER";
        }
        return "FEMALE";
    }

    public String getOtpForTesting(String email) {
        if (email == null) return null;
        OtpEntry e = otpStore.get(email.trim().toLowerCase());
        return e != null ? e.code : null;
    }

    public static AuthDtos.UserDto toUserDto(User user) {
        return AuthDtos.UserDto.builder()
                .id(String.valueOf(user.getId()))
                .name(user.getFullName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone() == null ? "" : user.getPhone())
                .gender(user.getGender() == null ? "FEMALE" : user.getGender())
                .dateOfBirth(user.getDateOfBirth() == null ? null : user.getDateOfBirth().toString())
                .avatar(user.getAvatarUrl())
                .role(user.getRole().name())
                .isAdmin(user.isAdmin())
                .status(user.getStatus().name())
                .build();
    }
}
