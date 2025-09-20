package com.locallocket.backend.repository;

import com.locallocket.backend.entity.Payment;
import com.locallocket.backend.entity.PaymentStatus;
import com.locallocket.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    List<Payment> findByStatus(PaymentStatus status);

    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    // Analytics queries
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.createdAt >= :fromDate")
    long countByStatusAndCreatedAtAfter(@Param("status") PaymentStatus status,
                                        @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.createdAt >= :fromDate")
    BigDecimal sumAmountByStatusAndCreatedAtAfter(@Param("status") PaymentStatus status,
                                                  @Param("fromDate") LocalDateTime fromDate);

    // Failed payments for retry
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime dateTime);

    // Webhook verification
    List<Payment> findByWebhookVerifiedFalseAndStatusIn(List<PaymentStatus> statuses);

    boolean existsByRazorpayOrderId(String razorpayOrderId);

    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}
