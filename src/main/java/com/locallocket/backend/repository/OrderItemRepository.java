package com.locallocket.backend.repository;

import com.locallocket.backend.entity.Order;
import com.locallocket.backend.entity.OrderItem;
import com.locallocket.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProduct(Product product);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product = :product")
    Long getTotalQuantitySoldForProduct(@Param("product") Product product);

    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold FROM OrderItem oi " +
            "WHERE oi.order.vendor.id = :vendorId " +
            "GROUP BY oi.product ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProductsByVendor(@Param("vendorId") Long vendorId);
}
