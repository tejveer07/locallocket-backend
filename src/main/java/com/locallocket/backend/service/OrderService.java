package com.locallocket.backend.service;

import com.locallocket.backend.dto.cart.CartItemResponse;
import com.locallocket.backend.dto.cart.CartResponse;
import com.locallocket.backend.dto.order.*;
import com.locallocket.backend.dto.payment.PaymentResponse;
import com.locallocket.backend.dto.payment.RefundResponse;
import com.locallocket.backend.entity.*;
import com.locallocket.backend.exception.BadRequestException;
import com.locallocket.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.locallocket.backend.entity.PaymentStatus;
import com.locallocket.backend.service.PaymentService;
import org.slf4j.Logger; // Add this import
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentService paymentService;

    private final Random random = new Random();

    @Transactional
    public OrderResponse createOrderFromCart(User user, CreateOrderRequest request) {
        // Get user's cart
        CartResponse cartResponse = cartService.getCart(user);
        if (cartResponse == null || cartResponse.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        // Validate all cart items are still available and in stock
        validateCartItems(cartResponse);

        // Generate unique order number
        String orderNumber = generateOrderNumber();

        // Get vendor from cart
        Vendor vendor = getVendorById(cartResponse.getVendorId());

        // Create order with PAYMENT_PENDING status initially
        Order order = new Order(orderNumber, user, vendor);
        order.setSubtotal(cartResponse.getSubtotal());
        order.setPlatformFee(cartResponse.getPlatformFee());
        order.setDeliveryFee(cartResponse.getDeliveryFee());
        order.setTotalAmount(cartResponse.getTotalAmount());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryLatitude(request.getDeliveryLatitude());
        order.setDeliveryLongitude(request.getDeliveryLongitude());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.CREATED); // Set payment status

        // Save order first to get ID
        order = orderRepository.save(order);

        // Create order items from cart items
        for (CartItemResponse cartItem : cartResponse.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product not found: " + cartItem.getProductId()));

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), cartItem.getPriceAtTime());
            order.addItem(orderItem);
            orderItemRepository.save(orderItem);
        }

        // Clear user's cart after successful order creation
        cartService.clearCart(user);

        // Update product stock (will be restored if payment fails or order is rejected)
        updateProductStock(order);

        logger.info("Order created successfully: {} for user: {}", order.getOrderNumber(), user.getEmail());

        return new OrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(User user, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return orders.map(OrderResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrdersByStatus(User user, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status, pageable);
        return orders.map(OrderResponse::new);
    }

    @Transactional(readOnly = true)
    public OrderResponse getCustomerOrder(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new BadRequestException("Order not found"));
        return new OrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getVendorOrders(Vendor vendor, Pageable pageable) {
        Page<Order> orders = orderRepository.findByVendorOrderByCreatedAtDesc(vendor, pageable);
        return orders.map(OrderResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getVendorOrdersByStatus(Vendor vendor, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByVendorAndStatusOrderByCreatedAtDesc(vendor, status, pageable);
        return orders.map(OrderResponse::new);
    }

    @Transactional(readOnly = true)
    public OrderResponse getVendorOrder(Vendor vendor, Long orderId) {
        Order order = orderRepository.findByIdAndVendor(orderId, vendor)
                .orElseThrow(() -> new BadRequestException("Order not found"));
        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse acceptOrder(Vendor vendor, Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdAndVendor(orderId, vendor)
                .orElseThrow(() -> new BadRequestException("Order not found"));

        if (!order.canBeAccepted()) {
            throw new BadRequestException("Order cannot be accepted. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());

        // Set estimated delivery time if provided
        if (request.getEstimatedDeliveryMinutes() != null) {
            order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(request.getEstimatedDeliveryMinutes()));
        }

        orderRepository.save(order);
        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse rejectOrder(Vendor vendor, Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdAndVendor(orderId, vendor)
                .orElseThrow(() -> new BadRequestException("Order not found"));

        if (!order.canBeRejected()) {
            throw new BadRequestException("Order cannot be rejected. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.REJECTED);
        order.setRejectedAt(LocalDateTime.now());
        order.setRejectionReason(request.getReason());

        // Process refund if payment was completed
        if (order.isPaymentCompleted()) {
            try {
                RefundResponse refund = paymentService.processRefund(order,
                        "Order rejected by vendor: " + (request.getReason() != null ? request.getReason() : "No reason provided"));

                logger.info("Refund processed for rejected order: {} with refund ID: {}",
                        order.getOrderNumber(), refund.getRefundId());
            } catch (Exception e) {
                logger.error("Error processing refund for rejected order {}: ", order.getOrderNumber(), e);
                // Continue with rejection even if refund fails - can be processed manually
            }
        }

        // Restore product stock
        restoreProductStock(order);

        orderRepository.save(order);

        logger.info("Order rejected: {} by vendor: {}", order.getOrderNumber(), vendor.getShopName());

        return new OrderResponse(order);
    }


    @Transactional
    public OrderResponse updateOrderStatus(Vendor vendor, Long orderId, OrderStatus newStatus, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdAndVendor(orderId, vendor)
                .orElseThrow(() -> new BadRequestException("Order not found"));

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        // Set timestamps based on status
        switch (newStatus) {
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                order.setCancellationReason(request.getReason());
                restoreProductStock(order);
                break;
        }

        orderRepository.save(order);
        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(User user, Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new BadRequestException("Order not found"));

        if (!order.canBeCancelled()) {
            throw new BadRequestException("Order cannot be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(request.getReason());

        // Process refund if payment was completed
        if (order.isPaymentCompleted()) {
            try {
                RefundResponse refund = paymentService.processRefund(order,
                        "Order cancelled by customer: " + (request.getReason() != null ? request.getReason() : "No reason provided"));

                logger.info("Refund processed for cancelled order: {} with refund ID: {}",
                        order.getOrderNumber(), refund.getRefundId());
            } catch (Exception e) {
                logger.error("Error processing refund for cancelled order {}: ", order.getOrderNumber(), e);
                // Continue with cancellation even if refund fails - can be processed manually
            }
        }

        // Restore product stock
        restoreProductStock(order);

        orderRepository.save(order);

        logger.info("Order cancelled: {} by customer: {}", order.getOrderNumber(), user.getEmail());

        return new OrderResponse(order);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getOrderPayment(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new BadRequestException("Order not found"));

        return paymentService.getPaymentByOrder(order);
    }

    public boolean isOrderReadyForVendor(Order order) {
        return order.isPaymentCompleted() && order.getStatus() == OrderStatus.PENDING;
    }

    private void validateCartItems(CartResponse cart) {
        for (CartItemResponse item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product not found: " + item.getProductName()));

            if (!product.getIsActive()) {
                throw new BadRequestException("Product is no longer available: " + product.getName());
            }

            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName() +
                        ". Available: " + product.getStock() + ", Required: " + item.getQuantity());
            }
        }
    }

    private String generateOrderNumber() {
        String prefix = "ORD";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String randomSuffix = String.format("%03d", random.nextInt(1000));
        String orderNumber = prefix + timestamp + randomSuffix;

        // Ensure uniqueness
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            randomSuffix = String.format("%03d", random.nextInt(1000));
            orderNumber = prefix + timestamp + randomSuffix;
        }

        return orderNumber;
    }

    private Vendor getVendorById(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new BadRequestException("Vendor not found"));
    }

    private void updateProductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }
            product.setStock(newStock);
            productRepository.save(product);
        }
    }

    private void restoreProductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getStock() + item.getQuantity();
            product.setStock(newStock);
            productRepository.save(product);
        }
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.ACCEPTED && newStatus != OrderStatus.REJECTED && newStatus != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case ACCEPTED:
                if (newStatus != OrderStatus.IN_PROGRESS && newStatus != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case IN_PROGRESS:
                if (newStatus != OrderStatus.READY && newStatus != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case READY:
                if (newStatus != OrderStatus.OUT_FOR_DELIVERY && newStatus != OrderStatus.DELIVERED && newStatus != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case OUT_FOR_DELIVERY:
                if (newStatus != OrderStatus.DELIVERED && newStatus != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case DELIVERED:
            case REJECTED:
            case CANCELLED:
                throw new BadRequestException("Cannot change status from " + currentStatus);
        }
    }

    @Autowired
    private VendorRepository vendorRepository;
}
