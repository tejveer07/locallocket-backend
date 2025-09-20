package com.locallocket.backend.repository;

import com.locallocket.backend.entity.Cart;
import com.locallocket.backend.entity.CartItem;
import com.locallocket.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findByCart(Cart cart);

    void deleteByCart(Cart cart);

    boolean existsByCartAndProduct(Cart cart, Product product);
}
