package com.fashionshop.be.controller;

import com.fashionshop.be.dto.AdminDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.AdminManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final AdminManagementService adminManagementService;

    @PostMapping
    public ApiResponse<AdminDtos.ContactDto> createContact(@Valid @RequestBody AdminDtos.CreateContactRequest request) {
        return ApiResponse.ok("Gửi thông tin liên hệ thành công! LUXE sẽ phản hồi sớm nhất.",
                adminManagementService.submitPublicContact(request));
    }
}

