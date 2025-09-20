package com.locallocket.backend.service;

import com.locallocket.backend.dto.payment.*;
import com.locallocket.backend.entity.*;
import com.locallocket.backend.exception.BadRequestException;
import com.locallocket.backend.exception.PaymentException;
import com.locallocket.backend.repository.*;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RazorpayClient razorpayClient;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    @Transactional
    public PaymentOrderResponse createPaymentOrder(User user, CreatePaymentRequest request) {
        // Get order
        com.locallocket.backend.entity.Order order = orderRepository.findByIdAndUser(request.getOrderId(), user)
                .orElseThrow(() -> new BadRequestException("Order not found"));

        // Check if order already has payment
        if (order.getPayment() != null) {
            throw new BadRequestException("Payment already exists for this order");
        }

        // Check if order is in valid state for payment
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in valid state for payment. Current status: " + order.getStatus());
        }

        try {
            // Create Razorpay order
            JSONObject razorpayOrderRequest = new JSONObject();
            razorpayOrderRequest.put("amount", order.getTotalAmount().multiply(new BigDecimal("100")).intValue()); // Convert to paise
            razorpayOrderRequest.put("currency", "INR");
            razorpayOrderRequest.put("receipt", order.getOrderNumber());

            // Add order notes
            JSONObject notes = new JSONObject();
            notes.put("order_id", order.getId());
            notes.put("customer_id", user.getId());
            notes.put("vendor_id", order.getVendor().getId());
            razorpayOrderRequest.put("notes", notes);

            Order razorpayOrder = razorpayClient.orders.create(razorpayOrderRequest);

            // Create payment record
            com.locallocket.backend.entity.Payment payment = new com.locallocket.backend.entity.Payment(
                    order, razorpayOrder.get("id"), order.getTotalAmount());
            payment = paymentRepository.save(payment);

            // Link payment to order
            order.setPayment(payment);
            order.setPaymentStatus(PaymentStatus.CREATED);
            orderRepository.save(order);

            // Create response
            return new PaymentOrderResponse(
                    payment.getId(),
                    razorpayOrder.get("id"),
                    razorpayKeyId,
                    order.getTotalAmount(),
                    "INR",
                    user.getFullName(),
                    user.getEmail(),
                    order.getCustomerPhone(),
                    "Payment for Order " + order.getOrderNumber()
            );

        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order: ", e);
            throw new PaymentException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        // Get payment
        com.locallocket.backend.entity.Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new BadRequestException("Payment not found"));

        // Verify signature
        boolean signatureValid = verifyRazorpaySignature(
                payment.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!signatureValid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid payment signature");
            paymentRepository.save(payment);
            throw new BadRequestException("Invalid payment signature");
        }

        try {
            // Fetch payment details from Razorpay
            Payment razorpayPayment = razorpayClient.payments.fetch(request.getRazorpayPaymentId());

            // Update payment record
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setStatus(PaymentStatus.SUCCESS);

            // Extract payment method details
            updatePaymentMethodDetails(payment, razorpayPayment);

            payment = paymentRepository.save(payment);

            // Update order status
            com.locallocket.backend.entity.Order order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            orderRepository.save(order);

            logger.info("Payment verified successfully for order: {}", order.getOrderNumber());

            return new PaymentResponse(payment);

        } catch (RazorpayException e) {
            logger.error("Error verifying payment: ", e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment verification failed: " + e.getMessage());
            paymentRepository.save(payment);
            throw new PaymentException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public void handlePaymentWebhook(String payload, String signature) {
        // Verify webhook signature
        if (!verifyWebhookSignature(payload, signature)) {
            logger.warn("Invalid webhook signature received");
            return;
        }

        try {
            JSONObject webhookPayload = new JSONObject(payload);
            String event = webhookPayload.getString("event");
            JSONObject paymentEntity = webhookPayload.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayPaymentId = paymentEntity.getString("id");
            String razorpayOrderId = paymentEntity.getString("order_id");

            // Find payment by Razorpay order ID
            Optional<com.locallocket.backend.entity.Payment> paymentOpt =
                    paymentRepository.findByRazorpayOrderId(razorpayOrderId);

            if (paymentOpt.isEmpty()) {
                logger.warn("Payment not found for Razorpay order ID: {}", razorpayOrderId);
                return;
            }

            com.locallocket.backend.entity.Payment payment = paymentOpt.get();
            payment.setWebhookVerified(true);

            switch (event) {
                case "payment.captured":
                    handlePaymentCaptured(payment, paymentEntity);
                    break;
                case "payment.failed":
                    handlePaymentFailed(payment, paymentEntity);
                    break;
                case "payment.authorized":
                    handlePaymentAuthorized(payment, paymentEntity);
                    break;
                default:
                    logger.info("Unhandled webhook event: {}", event);
            }

            paymentRepository.save(payment);

        } catch (Exception e) {
            logger.error("Error processing payment webhook: ", e);
        }
    }

    @Transactional
    public RefundResponse processRefund(com.locallocket.backend.entity.Order order, String reason) {
        com.locallocket.backend.entity.Payment payment = order.getPayment();

        if (payment == null || !payment.isSuccessful()) {
            throw new BadRequestException("No successful payment found for this order");
        }

        if (payment.isRefunded()) {
            throw new BadRequestException("Payment has already been refunded");
        }

        try {
            // Create refund request JSON
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", payment.getAmount().multiply(new BigDecimal("100")).intValue()); // Convert to paise
            refundRequest.put("speed", "normal"); // normal or optimum

            // Add notes
            JSONObject notes = new JSONObject();
            notes.put("order_id", order.getId());
            notes.put("refund_reason", reason);
            refundRequest.put("notes", notes);

            // Create refund using RazorpayClient (NOT Payment.createRefund)
            com.razorpay.Refund refund = razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            // Update payment record
            payment.setRefundId(refund.get("id"));
            payment.setRefundAmount(payment.getAmount());
            payment.setRefundedAt(LocalDateTime.now());
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // Update order status
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            orderRepository.save(order);

            logger.info("Refund processed successfully for order: {} with refund ID: {}",
                    order.getOrderNumber(), refund.get("id"));

            return new RefundResponse(
                    refund.get("id"),
                    payment.getAmount(),
                    refund.get("status"),
                    reason
            );

        } catch (RazorpayException e) {
            logger.error("Error processing refund for order {}: ", order.getOrderNumber(), e);
            throw new PaymentException("Refund processing failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(com.locallocket.backend.entity.Order order) {
        com.locallocket.backend.entity.Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new BadRequestException("Payment not found for this order"));
        return new PaymentResponse(payment);
    }

    private boolean verifyRazorpaySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            return Utils.verifySignature(payload, signature, webhookSecret);
        } catch (Exception e) {
            logger.error("Error verifying Razorpay signature: ", e);
            return false;
        }
    }

    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] computedHash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = bytesToHex(computedHash);

            return computedSignature.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error verifying webhook signature: ", e);
            return false;
        }
    }

    private void updatePaymentMethodDetails(com.locallocket.backend.entity.Payment payment, Payment razorpayPayment) {
        try {
            payment.setPaymentMethod(razorpayPayment.get("method"));

            if ("card".equals(payment.getPaymentMethod())) {
                JSONObject card = razorpayPayment.toJson().getJSONObject("card");
                payment.setCardLast4(card.getString("last4"));
                payment.setCardNetwork(card.getString("network"));
            } else if ("upi".equals(payment.getPaymentMethod())) {
                JSONObject upi = razorpayPayment.toJson().getJSONObject("upi");
                payment.setVpa(upi.optString("vpa"));
            } else if ("wallet".equals(payment.getPaymentMethod())) {
                payment.setWallet(razorpayPayment.get("wallet"));
            } else if ("netbanking".equals(payment.getPaymentMethod())) {
                payment.setBank(razorpayPayment.get("bank"));
            }
        } catch (Exception e) {
            logger.warn("Error updating payment method details: ", e);
        }
    }

    private void handlePaymentCaptured(com.locallocket.backend.entity.Payment payment, JSONObject paymentEntity) {
        payment.setRazorpayPaymentId(paymentEntity.getString("id"));
        payment.setStatus(PaymentStatus.SUCCESS);

        // Update order status
        com.locallocket.backend.entity.Order order = payment.getOrder();
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        orderRepository.save(order);

        logger.info("Payment captured via webhook for order: {}", order.getOrderNumber());
    }

    private void handlePaymentFailed(com.locallocket.backend.entity.Payment payment, JSONObject paymentEntity) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(paymentEntity.optString("error_description", "Payment failed"));

        // Update order status
        com.locallocket.backend.entity.Order order = payment.getOrder();
        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);

        logger.info("Payment failed via webhook for order: {}", order.getOrderNumber());
    }

    private void handlePaymentAuthorized(com.locallocket.backend.entity.Payment payment, JSONObject paymentEntity) {
        payment.setRazorpayPaymentId(paymentEntity.getString("id"));
        payment.setStatus(PaymentStatus.PENDING);

        logger.info("Payment authorized via webhook for order: {}", payment.getOrder().getOrderNumber());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
