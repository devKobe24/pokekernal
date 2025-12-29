package com.kobe.pokekernle.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long cardId;
    private String cardName;
    private String imageUrl;
    private Integer quantity;
    private Long unitPrice;
    private Long totalPrice;
}

