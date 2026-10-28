package com.fashionshop.be.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Họ và tên không được để trống")
        private String fullName;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        private String phone;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private String id;
        private String name;
        private String fullName;
        private String email;
        private String phone;
        private String avatar;
        private String role;
        private boolean isAdmin;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private UserDto user;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {
        @NotBlank(message = "Họ và tên không được để trống")
        private String fullName;
        private String phone;
        private String avatarUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
        private String currentPassword;

        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @Size(min = 6, message = "Mật khẩu mới phải từ 6 ký tự trở lên")
        private String newPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private Long id;
        private String recipientName;
        private String phone;
        private String province;
        private String district;
        private String ward;
        private String streetAddress;
        private String fullAddress;
        private boolean isDefault;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressUpsertRequest {
        @NotBlank(message = "Tên người nhận không được để trống")
        private String recipientName;

        @NotBlank(message = "Số điện thoại không được để trống")
        private String phone;

        private String province;
        private String district;
        private String ward;
        private String streetAddress;
        private String fullAddress;
        private boolean isDefault;
    }
}
