package com.fashionshop.be.service;

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

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
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

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private record OtpEntry(String code, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    @Transactional(readOnly = true)
    public AuthDtos.SendOtpResponse sendRegisterOtp(AuthDtos.SendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email này đã được đăng ký");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        otpStore.put(email, new OtpEntry(code, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS)));

        log.info("[LUXE EMAIL OTP] Mã xác thực đăng ký cho {} là: {} (hiệu lực {} giây)", email, code, OTP_EXPIRY_SECONDS);

        return AuthDtos.SendOtpResponse.builder()
                .email(email)
                .expiresInSeconds(OTP_EXPIRY_SECONDS)
                .sentViaSmtp(false)
                .otpCode(code)
                .build();
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email này đã được đăng ký");
        }

        String submittedOtp = request.getOtpCode() == null ? "" : request.getOtpCode().trim();
        OtpEntry otpEntry = otpStore.get(email);
        if (otpEntry == null || otpEntry.isExpired()) {
            otpStore.remove(email);
            throw new BusinessException("Mã OTP đã hết hạn hoặc chưa được gửi. Vui lòng nhấn 'Lấy mã OTP'");
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

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Email hoặc mật khẩu không chính xác"));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(user);
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .user(toUserDto(user))
                .build();
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

