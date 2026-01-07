package com.kobe.pokekernle.domain.card.response;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.entity.PriceHistory;
import com.kobe.pokekernle.domain.card.service.CurrencyConverterService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

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
        String cardCondition,        // 카드 상태 (enum name)
        String cardConditionDesc,    // 카드 상태 설명
        String collectionStatus,     // 컬렉션 상태 (enum name)
        String collectionStatusDesc, // 컬렉션 상태 설명
        String imageUrl,
        String priceDisplay, // 화면에 보여줄 가격 문자열 (예: "$ 12.50")
        Long salePrice, // 희망 판매 가격 (원화, KRW)
        String category, // 카테고리 (pokemon-single, pokemon-box, onepiece-single, onepiece-box)
        LocalDateTime createdAt, // 카드 추가 날짜
        BigDecimal priceChange // 시세 변동률 (절댓값, 정렬용)
) {
    public static CardListResponse from(Card card, MarketPrice marketPrice, CurrencyConverterService currencyConverter) {
        return from(card, marketPrice, null, currencyConverter);
    }
    
    public static CardListResponse from(Card card, MarketPrice marketPrice, List<PriceHistory> priceHistories, CurrencyConverterService currencyConverter) {
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

        // 시세 변동률 계산 (최근 2개 가격의 차이를 절댓값으로)
        BigDecimal priceChange = BigDecimal.ZERO;
        if (priceHistories != null && priceHistories.size() >= 2) {
            List<PriceHistory> sortedHistories = priceHistories.stream()
                    .sorted((h1, h2) -> h2.getRecordedAt().compareTo(h1.getRecordedAt()))
                    .toList();
            BigDecimal recentPrice = sortedHistories.get(0).getPrice();
            BigDecimal previousPrice = sortedHistories.get(1).getPrice();
            if (previousPrice != null && previousPrice.compareTo(BigDecimal.ZERO) > 0) {
                // 변동률 = (최근 가격 - 이전 가격) / 이전 가격 * 100
                priceChange = recentPrice.subtract(previousPrice)
                        .divide(previousPrice, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .abs(); // 절댓값
            }
        }

        return new CardListResponse(
                card.getId(),
                card.getName() != null ? card.getName() : "Unknown",
                card.getSetName() != null ? card.getSetName() : "Unknown Set",
                card.getRarity() != null ? card.getRarity().name() : "UNKNOWN", // Enum -> String, null 처리
                card.getCardCondition() != null ? card.getCardCondition().name() : null, // 카드 상태
                card.getCardCondition() != null ? card.getCardCondition().getDescription() : null, // 카드 상태 설명
                card.getCollectionStatus() != null ? card.getCollectionStatus().name() : null, // 컬렉션 상태
                card.getCollectionStatus() != null ? card.getCollectionStatus().getDescription() : null, // 컬렉션 상태 설명
                card.getDisplayImageUrl() != null ? card.getDisplayImageUrl() : "/images/pokemon-card.png", // 업로드된 이미지 우선 사용
                priceStr,
                card.getSalePrice(), // 희망 판매 가격 (원화)
                card.getCardCategory() != null ? card.getCardCategory().name() : null, // 카테고리 (Enum -> String)
                card.getCreatedAt(), // 카드 추가 날짜
                priceChange // 시세 변동률
        );
    }
}