package com.kobe.pokekernle.domain.order.dto.response;

import com.kobe.pokekernle.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private List<OrderItemResponse> items;
    private Long totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;
}

