package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.ProductColor;
import com.fashionshop.be.domain.entity.ProductVariant;
import com.fashionshop.be.domain.entity.StockImport;
import com.fashionshop.be.domain.entity.StockImportItem;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.dto.AdminDtos;
import com.fashionshop.be.exception.ErrorCode;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.repository.ProductVariantRepository;
import com.fashionshop.be.repository.StockImportRepository;
import com.fashionshop.be.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockImportService {

    private final StockImportRepository stockImportRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<AdminDtos.StockImportResponseDto> getAllStockImports() {
        return stockImportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(StockImportService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminDtos.StockImportResponseDto getStockImportById(Long id) {
        StockImport stockImport = stockImportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STOCK_IMPORT_NOT_FOUND));
        return toDto(stockImport);
    }

    @Transactional
    public AdminDtos.StockImportResponseDto createStockImport(AdminDtos.CreateStockImportRequest request) {
        String creatorEmail = securityUtils.getCurrentUserOptional()
                .map(User::getEmail)
                .orElse("admin@luxe.vn");

        String importCode = "NK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + "-" + (1000 + (System.currentTimeMillis() % 9000));

        StockImport stockImport = StockImport.builder()
                .importCode(importCode)
                .supplierName(request.getSupplierName().trim())
                .note(request.getNote())
                .createdByEmail(creatorEmail)
                .build();

        for (AdminDtos.StockImportItemRequest itemReq : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VARIANT_NOT_FOUND));

            // OOP Encapsulation: ProductVariant updates its own stock & weighted-average costPrice
            variant.importStock(itemReq.getQuantity(), itemReq.getUnitCost());
            productVariantRepository.save(variant);

            BigDecimal subtotal = itemReq.getUnitCost().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            stockImport.addItem(StockImportItem.builder()
                    .variant(variant)
                    .quantity(itemReq.getQuantity())
                    .unitCost(itemReq.getUnitCost())
                    .subtotal(subtotal)
                    .build());
        }

        StockImport saved = stockImportRepository.save(stockImport);
        return toDto(saved);
    }

    public static AdminDtos.StockImportResponseDto toDto(StockImport si) {
        List<AdminDtos.StockImportItemDto> items = si.getItems().stream()
                .map(item -> {
                    ProductVariant v = item.getVariant();
                    ProductColor pc = v.getProductColor();
                    return AdminDtos.StockImportItemDto.builder()
                            .id(item.getId())
                            .variantId(v.getId())
                            .sku(v.getSku())
                            .productName(pc.getProduct().getName())
                            .colorName(pc.getColor().getName())
                            .sizeName(v.getSize().getName())
                            .quantity(item.getQuantity())
                            .unitCost(item.getUnitCost())
                            .subtotal(item.getSubtotal())
                            .currentStock(v.getStock())
                            .build();
                })
                .toList();

        int totalQty = items.stream().mapToInt(AdminDtos.StockImportItemDto::getQuantity).sum();
        return AdminDtos.StockImportResponseDto.builder()
                .id(si.getId())
                .importCode(si.getImportCode())
                .supplierName(si.getSupplierName())
                .note(si.getNote())
                .totalCost(si.getTotalCost())
                .formattedTotalCost(SlugUtils.formatVnd(si.getTotalCost()))
                .createdByEmail(si.getCreatedByEmail())
                .createdAt(SlugUtils.formatDateTime(si.getCreatedAt()))
                .totalQuantity(totalQty)
                .itemCount(items.size())
                .items(items)
                .build();
    }
}

