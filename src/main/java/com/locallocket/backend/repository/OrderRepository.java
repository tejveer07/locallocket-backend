package com.locallocket.backend.repository;

import com.locallocket.backend.entity.Order;
import com.locallocket.backend.entity.OrderStatus;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Customer order queries
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndUser(Long id, User user);

    // Vendor order queries
    Page<Order> findByVendorOrderByCreatedAtDesc(Vendor vendor, Pageable pageable);

    Page<Order> findByVendorAndStatusOrderByCreatedAtDesc(Vendor vendor, OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndVendor(Long id, Vendor vendor);

    // Order number queries
    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    // Status queries
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime);

    // Analytics queries
    @Query("SELECT COUNT(o) FROM Order o WHERE o.vendor = :vendor AND o.status = :status")
    long countByVendorAndStatus(@Param("vendor") Vendor vendor, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user AND o.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses AND o.createdAt >= :fromDate ORDER BY o.createdAt DESC")
    List<Order> findByStatusInAndCreatedAtAfter(@Param("statuses") List<OrderStatus> statuses,
                                                @Param("fromDate") LocalDateTime fromDate);
}
