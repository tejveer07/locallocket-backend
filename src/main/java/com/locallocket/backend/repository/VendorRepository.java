// src/main/java/com/locallocket/backend/repository/VendorRepository.java
package com.locallocket.backend.repository;

import com.locallocket.backend.entity.User;
import com.locallocket.backend.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUser(User user);
    boolean existsByUser(User user);

    List<Vendor> findByIsActiveTrue();
    Optional<Vendor> findByIdAndIsActiveTrue(Long id);
}
