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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int OTP_EXPIRY_SECONDS = 300;

    private record OtpEntry(String code, LocalDateTime expiresAt) {}

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Transactional(readOnly = true)
    public AuthDtos.SendOtpResponse sendRegisterOtp(AuthDtos.SendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email này đã được đăng ký");
        }

        String otpCode = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));
        otpStore.put(email, new OtpEntry(otpCode, LocalDateTime.now().plusSeconds(OTP_EXPIRY_SECONDS)));

        boolean smtpConfigured = mailUsername != null && !mailUsername.isBlank();
        log.info("[LUXE EMAIL OTP] Ma OTP xac thuc dang ky cho {} la: {} (hieu luc {}s)",
                email, otpCode, OTP_EXPIRY_SECONDS);

        return AuthDtos.SendOtpResponse.builder()
                .email(email)
                .expiresInSeconds(OTP_EXPIRY_SECONDS)
                .sentViaSmtp(smtpConfigured)
                .otpCode(otpCode)
                .build();
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email này đã được đăng ký");
        }

        OtpEntry otpEntry = otpStore.get(email);
        if (otpEntry == null || LocalDateTime.now().isAfter(otpEntry.expiresAt())) {
            throw new BusinessException("Mã OTP đã hết hạn hoặc chưa được gửi. Vui lòng nhấn 'Gửi mã OTP'");
        }
        if (request.getOtpCode() == null || !otpEntry.code().equals(request.getOtpCode().trim())) {
            throw new BusinessException("Mã OTP xác thực email không chính xác");
        }
        otpStore.remove(email);

        User user = User.builder()
                .email(email)
                .fullName(request.getFullName().trim())
                .phone(request.getPhone() == null ? "" : request.getPhone().trim())
                .gender(normalizeGender(request.getGender()))
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

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
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

    public static String normalizeGender(String raw) {
        if (raw == null || raw.isBlank()) {
            return "FEMALE";
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "MALE", "NAM" -> "MALE";
            case "OTHER", "KHAC", "KHÁC" -> "OTHER";
            default -> "FEMALE";
        };
    }

    public static AuthDtos.UserDto toUserDto(User user) {
        return AuthDtos.UserDto.builder()
                .id(String.valueOf(user.getId()))
                .name(user.getFullName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone() == null ? "" : user.getPhone())
                .gender(user.getGender() == null || user.getGender().isBlank() ? "FEMALE" : user.getGender())
                .dateOfBirth(user.getDateOfBirth() == null ? "" : user.getDateOfBirth().toString())
                .avatar(user.getAvatarUrl())
                .role(user.getRole().name())
                .isAdmin(user.isAdmin())
                .status(user.getStatus().name())
                .build();
    }
}

