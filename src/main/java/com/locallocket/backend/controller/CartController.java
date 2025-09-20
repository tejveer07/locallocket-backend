package com.locallocket.backend.controller;

import com.locallocket.backend.dto.cart.*;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    @Autowired
    private CartService cartService;

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {

        User user = getCurrentUser(authentication);
        CartResponse response = cartService.addToCart(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        User user = getCurrentUser(authentication);
        CartResponse response = cartService.getCart(user);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        User user = getCurrentUser(authentication);
        CartResponse response = cartService.updateCartItem(user, cartItemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            Authentication authentication,
            @PathVariable Long cartItemId) {

        User user = getCurrentUser(authentication);
        cartService.removeCartItem(user, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        User user = getCurrentUser(authentication);
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/switch-vendor/{vendorId}")
    public ResponseEntity<CartResponse> switchVendor(
            Authentication authentication,
            @PathVariable Long vendorId) {

        User user = getCurrentUser(authentication);
        CartResponse response = cartService.switchVendor(user, vendorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(Authentication authentication) {
        User user = getCurrentUser(authentication);
        CartResponse cart = cartService.getCart(user);
        int count = cart != null ? cart.getTotalItems() : 0;
        return ResponseEntity.ok(count);
    }

    @PostMapping("/add-multiple")
    public ResponseEntity<CartResponse> addMultipleProductsToCart(
            Authentication authentication,
            @Valid @RequestBody AddMultipleProductsRequest request) {

        User user = getCurrentUser(authentication);
        CartResponse response = cartService.addMultipleProductsToCart(user, request);
        return ResponseEntity.ok(response);
    }

}   
