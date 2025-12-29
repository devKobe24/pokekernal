package com.kobe.pokekernle.domain.order.entity;

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
 * 주문 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Long totalPrice; // 총 주문 금액 (원화)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status; // 주문 상태

    @Builder
    public Order(User user, Long totalPrice, OrderStatus status) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.status = status != null ? status : OrderStatus.PENDING;
    }

    /**
     * 주문 아이템 추가
     */
    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    /**
     * 주문 상태 변경
     */
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * 총 가격 업데이트
     */
    public void updateTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }
}

