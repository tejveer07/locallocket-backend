package com.locallocket.backend.controller;

import com.locallocket.backend.dto.order.*;
import com.locallocket.backend.entity.OrderStatus;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.entity.Vendor;
import com.locallocket.backend.service.OrderService;
import com.locallocket.backend.service.VendorAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/orders")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('VENDOR')")
public class VendorOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VendorAuthService vendorAuthService;

    private Vendor getCurrentVendor(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return vendorAuthService.currentVendor(user);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {

        Vendor vendor = getCurrentVendor(authentication);

        Page<OrderResponse> orders;
        if (status != null) {
            orders = orderService.getVendorOrdersByStatus(vendor, status, pageable);
        } else {
            orders = orderService.getVendorOrders(vendor, pageable);
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        Vendor vendor = getCurrentVendor(authentication);
        OrderResponse order = orderService.getVendorOrder(vendor, orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        Vendor vendor = getCurrentVendor(authentication);
        OrderResponse response = orderService.acceptOrder(vendor, orderId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        Vendor vendor = getCurrentVendor(authentication);
        OrderResponse response = orderService.rejectOrder(vendor, orderId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        Vendor vendor = getCurrentVendor(authentication);
        OrderResponse response = orderService.updateOrderStatus(vendor, orderId, status, request);
        return ResponseEntity.ok(response);
    }
}
