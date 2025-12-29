package com.kobe.pokekernle.domain.cart.entity;

import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 장바구니 엔티티
 * 사용자당 하나의 장바구니를 가집니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carts")
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Builder
    public Cart(User user) {
        this.user = user;
    }

    /**
     * 장바구니에 아이템 추가
     */
    public void addItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    /**
     * 장바구니 비우기
     */
    public void clear() {
        cartItems.clear();
    }
}

