package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Cart;
import com.fashionshop.be.domain.entity.CartItem;
import com.fashionshop.be.domain.entity.Product;
import com.fashionshop.be.domain.entity.ProductColor;
import com.fashionshop.be.domain.entity.ProductVariant;
import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.dto.CartDtos;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ErrorCode;
import com.fashionshop.be.exception.ResourceNotFoundException;
import com.fashionshop.be.repository.CartItemRepository;
import com.fashionshop.be.repository.CartRepository;
import com.fashionshop.be.repository.ProductVariantRepository;
import com.fashionshop.be.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    @Transactional
    public CartDtos.CartResponseDto getMyCart() {
        User user = securityUtils.getCurrentUserRequired();
        Cart cart = getOrCreateCart(user);
        return toCartResponseDto(cart);
    }

    @Transactional
    public CartDtos.CartResponseDto addToCart(CartDtos.AddToCartRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        Cart cart = getOrCreateCart(user);

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VARIANT_NOT_FOUND));

        Product product = variant.getProductColor().getProduct();
        if (!product.isActive()) {
            throw new BusinessException(ErrorCode.PRODUCT_STOPPED_BUSINESS);
        }

        if (variant.getStock() <= 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        CartItem existingItem = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId())
                .orElse(null);

        int maxStock = variant.getStock();
        if (existingItem != null) {
            int newQty = Math.min(maxStock, existingItem.getQuantity() + request.getQuantity());
            existingItem.setQuantity(newQty);
            cartItemRepository.save(existingItem);
        } else {
            int qty = Math.min(maxStock, Math.max(1, request.getQuantity()));
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(qty)
                    .build();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        return toCartResponseDto(cart);
    }

    @Transactional
    public CartDtos.CartResponseDto updateCartItem(Long itemId, CartDtos.UpdateCartItemRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (request.getQuantity() <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            int maxStock = Math.max(1, item.getVariant().getStock());
            item.setQuantity(Math.min(maxStock, request.getQuantity()));
            cartItemRepository.save(item);
        }

        return toCartResponseDto(cart);
    }

    @Transactional
    public CartDtos.CartResponseDto removeCartItem(Long itemId) {
        User user = securityUtils.getCurrentUserRequired();
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);
        return toCartResponseDto(cart);
    }

    @Transactional
    public CartDtos.CartResponseDto clearCart() {
        User user = securityUtils.getCurrentUserRequired();
        Cart cart = getOrCreateCart(user);
        cart.clear();
        cartRepository.save(cart);
        return toCartResponseDto(cart);
    }

    @Transactional
    public CartDtos.CartResponseDto syncCart(CartDtos.SyncCartRequest request) {
        User user = securityUtils.getCurrentUserRequired();
        Cart cart = getOrCreateCart(user);

        if (request != null && request.getItems() != null && !request.getItems().isEmpty()) {
            for (CartDtos.SyncCartItem syncItem : request.getItems()) {
                if (syncItem.getVariantId() == null || syncItem.getQuantity() <= 0) {
                    continue;
                }
                ProductVariant variant = productVariantRepository.findById(syncItem.getVariantId()).orElse(null);
                if (variant == null || variant.getStock() <= 0 || !variant.getProductColor().getProduct().isActive()) {
                    continue;
                }

                CartItem existing = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId())
                        .orElse(null);

                int maxStock = variant.getStock();
                if (existing != null) {
                    existing.setQuantity(Math.min(maxStock, Math.max(existing.getQuantity(), syncItem.getQuantity())));
                    cartItemRepository.save(existing);
                } else {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .variant(variant)
                            .quantity(Math.min(maxStock, syncItem.getQuantity()))
                            .build();
                    cart.addItem(newItem);
                    cartItemRepository.save(newItem);
                }
            }
        }

        return toCartResponseDto(cart);
    }

    private CartDtos.CartResponseDto toCartResponseDto(Cart cart) {
        List<CartDtos.CartItemDto> itemDtos = new ArrayList<>();
        int totalItems = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getVariant();
            ProductColor pc = variant.getProductColor();
            Product product = pc.getProduct();

            BigDecimal basePrice = (variant.getPrice() != null && variant.getPrice().compareTo(BigDecimal.ZERO) > 0)
                    ? variant.getPrice()
                    : product.getBasePrice();

            BigDecimal unitPrice = product.getDiscountPercent() > 0
                    ? basePrice.multiply(BigDecimal.valueOf(100 - product.getDiscountPercent()))
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                    : basePrice;

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            String img = pc.getImages().isEmpty() ? "" : pc.getImages().get(0).getImageUrl();

            itemDtos.add(CartDtos.CartItemDto.builder()
                    .id(item.getId())
                    .variantId(variant.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSlug(product.getSlug())
                    .colorId(pc.getId())
                    .colorName(pc.getColor().getName())
                    .sizeName(variant.getSize().getName())
                    .sku(variant.getSku())
                    .imageUrl(img)
                    .unitPrice(unitPrice)
                    .originalPrice(basePrice)
                    .discountPercent(product.getDiscountPercent())
                    .quantity(item.getQuantity())
                    .stock(variant.getStock())
                    .lineTotal(lineTotal)
                    .build());

            totalItems += item.getQuantity();
            totalPrice = totalPrice.add(lineTotal);
        }

        return CartDtos.CartResponseDto.builder()
                .items(itemDtos)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }
}

