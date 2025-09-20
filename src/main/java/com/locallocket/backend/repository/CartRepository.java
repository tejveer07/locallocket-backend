package com.locallocket.backend.repository;

import com.locallocket.backend.entity.Cart;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    void deleteByUser(User user);

    boolean existsByUser(User user);
}
