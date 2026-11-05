package com.fashionshop.be.controller;

import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/vnpay/callback")
    public ApiResponse<PaymentService.VnPayCallbackResult> handleVnPayCallback(@RequestParam Map<String, String> queryParams) {
        PaymentService.VnPayCallbackResult result = paymentService.verifyVnPayCallback(queryParams);
        return ApiResponse.ok("Xử lý kết quả giao dịch VNPay thành công", result);
    }

    @PostMapping("/direct-pay")
    public ApiResponse<PaymentService.VnPayCallbackResult> handleDirectPay(@RequestBody Map<String, String> body) {
        String orderCode = body == null ? null : body.get("orderCode");
        String bankCode = (body == null || body.get("bankCode") == null) ? "NCB" : body.get("bankCode");
        PaymentService.VnPayCallbackResult result = paymentService.markDirectPaymentSuccess(orderCode, bankCode);
        return ApiResponse.ok("Thanh toán thành công trực tiếp", result);
    }
}

