// src/main/java/com/locallocket/backend/repository/ProductRepository.java
package com.locallocket.backend.repository;

import com.locallocket.backend.entity.Product;
import com.locallocket.backend.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByVendor(Vendor vendor, Pageable pageable);
    Page<Product> findByVendorAndNameContainingIgnoreCase(Vendor vendor, String name, Pageable pageable);
    boolean existsByIdAndVendor(Long id, Vendor vendor);

    Page<Product> findByVendorAndIsActiveTrue(Vendor vendor, Pageable pageable);

    Page<Product> findByVendorAndIsActiveTrueAndNameContainingIgnoreCase(
            Vendor vendor, String name, Pageable pageable);

    Page<Product> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByVendorInAndIsActiveTrueAndNameContainingIgnoreCase(
            List<Vendor> vendors, String name, Pageable pageable);

    // Count methods
    long countByVendor(Vendor vendor);
    long countByVendorAndIsActiveTrue(Vendor vendor);
}
