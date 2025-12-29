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

    // 배송지 정보
    private String recipientName; // 받는 분 이름
    private String recipientPhone; // 받는 분 연락처
    private String deliveryAddress; // 배송 주소
    private String deliveryMemo; // 배송 메모

    // 결제 정보
    private String paymentMethod; // 결제 방법 (CREDIT_CARD, NAVER_PAY, KAKAO_PAY, BANK_TRANSFER 등)

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderItemRequest {
        private Long cardId;
        private Integer quantity;
    }
}

