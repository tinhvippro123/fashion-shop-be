package com.fashionshop.be.service;

import com.fashionshop.be.config.VnPayConfig;
import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.enums.OrderStatus;
import com.fashionshop.be.domain.enums.PaymentStatus;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ErrorCode;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.repository.OrderRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VnPayConfig vnPayConfig;
    private final OrderRepository orderRepository;

    @Data
    @Builder
    public static class VnPayCallbackResult {
        private boolean success;
        private String orderCode;
        private Long orderId;
        private BigDecimal amount;
        private String bankCode;
        private String transactionNo;
        private String responseCode;
        private String message;
        private String payDate;
    }

    public String createVnPayPaymentUrl(Order order, String ipAddress) {
        if (order == null || order.getTotalAmount() == null) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID);
        }

        long amount = order.getTotalAmount().longValue() * 100L;
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnpCreateDate = now.format(formatter);
        String vnpExpireDate = now.plusMinutes(15).format(formatter);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", order.getOrderCode());
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnpParams.put("vnp_IpAddr", (ipAddress == null || ipAddress.isBlank()) ? "127.0.0.1" : ipAddress);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (!first) {
                    hashData.append('&');
                    query.append('&');
                }
                try {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    hashData.append(fieldName).append('=').append(fieldValue);
                    query.append(fieldName).append('=').append(fieldValue);
                }
                first = false;
            }
        }

        String vnpSecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);

        return vnPayConfig.getVnpPayUrl() + "?" + query;
    }

    @Transactional
    public VnPayCallbackResult verifyVnPayCallback(Map<String, String> queryParams) {
        String secureHash = queryParams.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) {
            throw new BusinessException(ErrorCode.VNPAY_SIGNATURE_MISSING);
        }

        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank()
                    && !entry.getKey().equals("vnp_SecureHash")
                    && !entry.getKey().equals("vnp_SecureHashType")) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        String calculatedHash = vnPayConfig.hashAllFields(fields);
        if (!calculatedHash.equalsIgnoreCase(secureHash)) {
            log.error("VNPay Hash mismatch! calculated={}, received={}", calculatedHash, secureHash);
            throw new BusinessException(ErrorCode.VNPAY_SIGNATURE_INVALID);
        }

        String orderCode = queryParams.get("vnp_TxnRef");
        String responseCode = queryParams.get("vnp_ResponseCode");
        String transactionNo = queryParams.get("vnp_TransactionNo");
        String bankCode = queryParams.get("vnp_BankCode");
        String payDate = queryParams.get("vnp_PayDate");
        String amountStr = queryParams.get("vnp_Amount");

        BigDecimal rawAmount = BigDecimal.ZERO;
        if (amountStr != null) {
            try {
                rawAmount = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100));
            } catch (Exception ignored) {}
        }

        Order order = orderRepository.findByOrderCodeIgnoreCase(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        boolean isSuccess = "00".equals(responseCode);

        if (isSuccess) {
            order.setPaymentStatus(PaymentStatus.PAID);
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PROCESSING);
            }
            orderRepository.save(order);
            log.info("VNPay payment successful for order {}. Amount={}", orderCode, rawAmount);
        } else {
            log.warn("VNPay payment failed or cancelled for order {}. ResponseCode={}", orderCode, responseCode);
        }

        return VnPayCallbackResult.builder()
                .success(isSuccess)
                .orderCode(orderCode)
                .orderId(order.getId())
                .amount(rawAmount.compareTo(BigDecimal.ZERO) > 0 ? rawAmount : order.getTotalAmount())
                .bankCode(bankCode)
                .transactionNo(transactionNo)
                .responseCode(responseCode)
                .payDate(payDate)
                .message(isSuccess ? "Giao dịch thành công" : "Giao dịch không thành công hoặc đã bị hủy (Mã lỗi: " + responseCode + ")")
                .build();
    }

    @Transactional
    public VnPayCallbackResult markDirectPaymentSuccess(String orderCode, String bankCode) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new BusinessException(ErrorCode.ORDER_CODE_EMPTY);
        }
        Order order = orderRepository.findByOrderCodeIgnoreCase(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        order.setPaymentStatus(PaymentStatus.PAID);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PROCESSING);
        }
        orderRepository.save(order);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String nowStr = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(formatter);
        String txnNo = "VNP" + System.currentTimeMillis();

        log.info("Direct in-app payment successful for order {}. Amount={}", orderCode, order.getTotalAmount());

        return VnPayCallbackResult.builder()
                .success(true)
                .orderCode(order.getOrderCode())
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .bankCode(bankCode != null && !bankCode.isBlank() ? bankCode : "NCB")
                .transactionNo(txnNo)
                .responseCode("00")
                .payDate(nowStr)
                .message("Thanh toán thành công trực tiếp qua cổng VNPay trong ứng dụng LUXE")
                .build();
    }
}

