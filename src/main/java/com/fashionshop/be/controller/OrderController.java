package com.fashionshop.be.controller;

import com.fashionshop.be.dto.OrderDtos;
import com.fashionshop.be.dto.response.ApiResponse;
import com.fashionshop.be.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderDtos.OrderResponseDto> createOrder(@Valid @RequestBody OrderDtos.CreateOrderRequest request) {
        return ApiResponse.ok("Đặt hàng thành công", orderService.createOrder(request));
    }

    @GetMapping("/my-orders")
    public ApiResponse<List<OrderDtos.OrderResponseDto>> getMyOrders() {
        return ApiResponse.ok(orderService.getMyOrders());
    }

    @GetMapping("/track")
    public ApiResponse<OrderDtos.OrderResponseDto> trackOrder(
            @RequestParam String orderCode,
            @RequestParam(required = false) String phone
    ) {
        return ApiResponse.ok(orderService.trackOrder(orderCode, phone));
    }

    @GetMapping("/{idOrCode}")
    public ApiResponse<OrderDtos.OrderResponseDto> getOrder(@PathVariable String idOrCode) {
        return ApiResponse.ok(orderService.getOrderByIdOrCode(idOrCode));
    }

    @PutMapping("/{idOrCode}/cancel")
    public ApiResponse<OrderDtos.OrderResponseDto> cancelMyOrder(
            @PathVariable String idOrCode,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body == null ? null : body.get("reason");
        return ApiResponse.ok("Đã hủy đơn hàng thành công", orderService.cancelMyOrder(idOrCode, reason));
    }
}
