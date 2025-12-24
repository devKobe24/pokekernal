package com.kobe.pokekernle.domain.card.response;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.service.CurrencyConverterService;

import java.math.BigDecimal;

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
        String priceDisplay // 화면에 보여줄 가격 문자열 (예: "$ 12.50")
) {
    public static CardListResponse from(Card card, MarketPrice marketPrice, CurrencyConverterService currencyConverter) {
        String priceStr = "가격 정보 없음";
        if (marketPrice != null && marketPrice.getPrice() != null) {
            try {
                // EUR를 USD로 변환하여 표시
                BigDecimal usdPrice = currencyConverter.convertToUsd(
                        marketPrice.getPrice(), 
                        marketPrice.getCurrency() != null ? marketPrice.getCurrency() : "USD"
                );
                priceStr = "$ " + usdPrice;
            } catch (Exception e) {
                // 변환 실패 시 기본값 사용
                priceStr = "가격 정보 없음";
            }
        }

        return new CardListResponse(
                card.getId(),
                card.getName() != null ? card.getName() : "Unknown",
                card.getSetName() != null ? card.getSetName() : "Unknown Set",
                card.getRarity() != null ? card.getRarity().name() : "UNKNOWN", // Enum -> String, null 처리
                card.getDisplayImageUrl() != null ? card.getDisplayImageUrl() : "/images/pokemon-card.png", // 업로드된 이미지 우선 사용
                priceStr
        );
    }
}