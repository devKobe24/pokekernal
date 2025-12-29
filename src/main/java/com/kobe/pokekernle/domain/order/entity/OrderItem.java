package com.kobe.pokekernle.domain.order.entity;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 아이템 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(nullable = false)
    private Integer quantity; // 주문 수량

    @Column(nullable = false)
    private Long unitPrice; // 주문 당시 단가 (원화)

    @Column(nullable = false)
    private Long totalPrice; // 주문 당시 총 가격 (원화)

    @Builder
    public OrderItem(Order order, Card card, Integer quantity, Long unitPrice, Long totalPrice) {
        this.order = order;
        this.card = card;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    /**
     * Order 설정 (양방향 관계)
     */
    public void setOrder(Order order) {
        this.order = order;
    }
}

