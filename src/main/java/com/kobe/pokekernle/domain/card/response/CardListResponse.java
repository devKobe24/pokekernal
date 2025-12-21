package com.kobe.pokekernle.domain.card.response;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;

/**
 * packageName    : com.kobe.pokekernle.domain.card.response
 * fileName       : CardListResponse
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
// Java 17 Record 사용 (데이터 전달용으로 간결함)
public record CardListResponse(
        Long id,
        String name,
        String setName,
        String rarity,
        String imageUrl,
        String priceDisplay // 화면에 보여줄 가격 문자열 (예: "€ 12.50")
) {
    public static CardListResponse from(Card card, MarketPrice marketPrice) {
        String priceStr = "가격 정보 없음";
        if (marketPrice != null && marketPrice.getPrice() != null) {
            // 편의상 유로(EUR) 기호 붙임 (데이터에 따라 동적으로 변경 가능)
            priceStr = "€ " + marketPrice.getPrice();
        }

        return new CardListResponse(
                card.getId(),
                card.getName(),
                card.getSetName(),
                card.getRarity().name(), // Enum -> String
                card.getImageUrl(),
                priceStr
        );
    }
}