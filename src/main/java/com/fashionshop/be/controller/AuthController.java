package com.fashionshop.be.controller;

import com.fashionshop.be.dto.AuthDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.AccountService;
import com.fashionshop.be.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    private void setAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(false) // dev localhost HTTP; can be true on HTTPS production
                .path("/")
                .maxAge(7 * 24 * 3600)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/send-otp")
    public ApiResponse<AuthDtos.SendOtpResponse> sendRegisterOtp(@Valid @RequestBody AuthDtos.SendOtpRequest request) {
        return ApiResponse.ok("Đã gửi mã xác thực OTP tới email của bạn", authService.sendRegisterOtp(request));
    }

    @PostMapping("/register")
    public ApiResponse<AuthDtos.AuthResponse> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthDtos.AuthResponse authResp = authService.register(request);
        setAuthCookie(response, authResp.getToken());
        return ApiResponse.ok("Đăng ký tài khoản thành công", authResp);
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.AuthResponse> login(
            @Valid @RequestBody AuthDtos.LoginRequest request,
            HttpServletResponse response
    ) {
        AuthDtos.AuthResponse authResp = authService.login(request);
        setAuthCookie(response, authResp.getToken());
        return ApiResponse.ok("Đăng nhập thành công", authResp);
    }

    @PostMapping("/google")
    public ApiResponse<AuthDtos.AuthResponse> loginWithGoogle(
            @Valid @RequestBody AuthDtos.GoogleLoginRequest request,
            HttpServletResponse response
    ) {
        AuthDtos.AuthResponse authResp = authService.loginWithGoogle(request);
        setAuthCookie(response, authResp.getToken());
        return ApiResponse.ok("Đăng nhập Google thành công", authResp);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        clearAuthCookie(response);
        return ApiResponse.ok("Đăng xuất thành công", null);
    }

    @PostMapping("/forgot-password/send-otp")
    public ApiResponse<AuthDtos.SendOtpResponse> sendForgotPasswordOtp(
            @Valid @RequestBody AuthDtos.ForgotPasswordSendOtpRequest request
    ) {
        return ApiResponse.ok("Đã gửi mã xác thực OTP khôi phục mật khẩu tới email", authService.sendForgotPasswordOtp(request));
    }

    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody AuthDtos.ForgotPasswordResetRequest request
    ) {
        authService.resetPassword(request);
        return ApiResponse.ok("Đổi mật khẩu thành công. Vui lòng đăng nhập với mật khẩu mới", null);
    }

    @GetMapping("/me")
    public ApiResponse<AuthDtos.UserDto> me() {
        return ApiResponse.ok(accountService.getProfile());
    }
}
