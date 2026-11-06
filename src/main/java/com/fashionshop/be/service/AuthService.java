package com.fashionshop.be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.domain.enums.UserStatus;
import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.exception.BusinessException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int OTP_EXPIRY_SECONDS = 300;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Map<String, OtpEntry> forgotOtpStore = new ConcurrentHashMap<>();

    private record OtpEntry(String code, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    // ================= REGISTER & REGISTER OTP =================

    @Transactional(readOnly = true)
    public AuthDtos.SendOtpResponse sendRegisterOtp(AuthDtos.SendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email này đã được đăng ký trong hệ thống");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        otpStore.put(email, new OtpEntry(code, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS)));

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
                .otpCode(code)
                .build();
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email này đã được đăng ký trong hệ thống");
        }

        String submittedOtp = request.getOtpCode() == null ? "" : request.getOtpCode().trim();
        OtpEntry otpEntry = otpStore.get(email);
        if (otpEntry == null || otpEntry.isExpired()) {
            otpStore.remove(email);
            throw new BusinessException("Mã OTP đã hết hạn hoặc chưa được gửi. Vui lòng nhấn 'Gửi mã xác thực'");
        }
        if (!otpEntry.code().equals(submittedOtp)) {
            throw new BusinessException("Mã OTP xác thực không chính xác");
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
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản liên kết với email: " + email));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        forgotOtpStore.put(email, new OtpEntry(code, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS)));

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
                .otpCode(code)
                .build();
    }

    @Transactional
    public void resetPassword(AuthDtos.ForgotPasswordResetRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản liên kết với email: " + email));

        String submittedOtp = request.getOtpCode() == null ? "" : request.getOtpCode().trim();
        OtpEntry otpEntry = forgotOtpStore.get(email);
        if (otpEntry == null || otpEntry.isExpired()) {
            forgotOtpStore.remove(email);
            throw new BusinessException("Mã OTP đã hết hạn hoặc chưa được gửi. Vui lòng nhấn 'Gửi mã xác thực'");
        }
        if (!otpEntry.code().equals(submittedOtp)) {
            throw new BusinessException("Mã OTP xác thực không chính xác");
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
            throw new BusinessException("Mã xác thực Google không hợp lệ");
        }

        try {
            String[] parts = credential.split("\\.");
            if (parts.length < 2) {
                throw new BusinessException("Định dạng Google Token không hợp lệ");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(payloadJson);

            String email = payload.path("email").asText("").trim().toLowerCase();
            String name = payload.path("name").asText("").trim();
            String picture = payload.path("picture").asText(null);

            if (email.isBlank()) {
                throw new BusinessException("Không thể lấy địa chỉ email từ tài khoản Google");
            }

            User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

            if (user != null) {
                if (user.getStatus() == UserStatus.BANNED) {
                    throw new BusinessException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên");
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
            throw new BusinessException("Đăng nhập bằng Google thất bại: " + e.getMessage());
        }
    }

    // ================= STANDARD LOGIN =================

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Email hoặc mật khẩu không chính xác"));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên");
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
