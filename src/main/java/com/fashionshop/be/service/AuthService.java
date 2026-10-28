package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.enums.Role;
import com.fashionshop.be.domain.enums.UserStatus;
import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.repository.UserRepository;
import com.fashionshop.be.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email này đã được đăng ký");
        }

        User user = User.builder()
                .email(email)
                .fullName(request.getFullName().trim())
                .phone(request.getPhone() == null ? "" : request.getPhone().trim())
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
                .avatar(user.getAvatarUrl())
                .role(user.getRole().name())
                .isAdmin(user.isAdmin())
                .status(user.getStatus().name())
                .build();
    }
}
