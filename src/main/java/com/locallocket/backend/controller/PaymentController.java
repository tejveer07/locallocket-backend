package com.locallocket.backend.controller;

import com.locallocket.backend.dto.payment.*;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @PostMapping("/create-order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(
            Authentication authentication,
            @Valid @RequestBody CreatePaymentRequest request) {

        User user = getCurrentUser(authentication);
        PaymentOrderResponse response = paymentService.createPaymentOrder(user, request);

        logger.info("Payment order created for user: {} and order: {}",
                user.getEmail(), request.getOrderId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> verifyPayment(
            Authentication authentication,
            @Valid @RequestBody VerifyPaymentRequest request) {

        User user = getCurrentUser(authentication);
        PaymentResponse response = paymentService.verifyPayment(request);

        logger.info("Payment verified for user: {} and payment: {}",
                user.getEmail(), request.getPaymentId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        logger.info("Received payment webhook");

        try {
            paymentService.handlePaymentWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.error("Error processing webhook: ", e);
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        User user = getCurrentUser(authentication);
        // This will be implemented in the updated OrderService

        return ResponseEntity.ok().build(); // Placeholder
    }
}
