package com.kobe.pokekernle.domain.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderRequest {
    
    @NotEmpty(message = "주문 아이템이 필요합니다.")
    private List<OrderItemRequest> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderItemRequest {
        private Long cardId;
        private Integer quantity;
    }
}

