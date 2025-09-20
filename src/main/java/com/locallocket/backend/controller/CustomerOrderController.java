package com.locallocket.backend.controller;

import com.locallocket.backend.dto.order.*;
import com.locallocket.backend.entity.OrderStatus;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/orders")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController {

    @Autowired
    private OrderService orderService;

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {

        User user = getCurrentUser(authentication);
        OrderResponse response = orderService.createOrderFromCart(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {

        User user = getCurrentUser(authentication);

        Page<OrderResponse> orders;
        if (status != null) {
            orders = orderService.getCustomerOrdersByStatus(user, status, pageable);
        } else {
            orders = orderService.getCustomerOrders(user, pageable);
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        User user = getCurrentUser(authentication);
        OrderResponse order = orderService.getCustomerOrder(user, orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        User user = getCurrentUser(authentication);
        OrderResponse response = orderService.cancelOrder(user, orderId, request);
        return ResponseEntity.ok(response);
    }
}
