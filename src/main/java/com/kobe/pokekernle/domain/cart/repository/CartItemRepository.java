package com.kobe.pokekernle.domain.cart.repository;

import com.kobe.pokekernle.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndCardId(Long cartId, Long cardId);
}

