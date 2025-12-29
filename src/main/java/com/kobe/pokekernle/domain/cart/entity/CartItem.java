package com.kobe.pokekernle.domain.cart.entity;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 아이템 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart_items")
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(nullable = false)
    private Integer quantity; // 수량

    @Column(nullable = false)
    private Long unitPrice; // 단가 (원화)

    @Builder
    public CartItem(Cart cart, Card card, Integer quantity, Long unitPrice) {
        this.cart = cart;
        this.card = card;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /**
     * Cart 설정 (양방향 관계)
     */
    public void setCart(Cart cart) {
        this.cart = cart;
    }

    /**
     * 수량 변경
     */
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 총 가격 계산
     */
    public Long getTotalPrice() {
        return unitPrice * quantity;
    }
}

