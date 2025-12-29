package com.kobe.pokekernle.domain.cart.repository;

import com.kobe.pokekernle.domain.cart.entity.Cart;
import com.kobe.pokekernle.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
    
    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems WHERE c.user = :user")
    Optional<Cart> findByUserWithItems(@Param("user") User user);
}

