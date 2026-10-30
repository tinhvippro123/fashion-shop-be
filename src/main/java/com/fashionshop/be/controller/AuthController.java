package com.fashionshop.be.controller;

import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.AccountService;
import com.fashionshop.be.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AccountService accountService;

    @PostMapping("/send-otp")
    public ApiResponse<AuthDtos.SendOtpResponse> sendRegisterOtp(@Valid @RequestBody AuthDtos.SendOtpRequest request) {
        return ApiResponse.ok("Đã gửi mã xác thực OTP tới email của bạn", authService.sendRegisterOtp(request));
    }

    @PostMapping("/register")
    public ApiResponse<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ApiResponse.ok("Đăng ký tài khoản thành công", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ApiResponse.ok("Đăng nhập thành công", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthDtos.UserDto> me() {
        return ApiResponse.ok(accountService.getProfile());
    }
}

