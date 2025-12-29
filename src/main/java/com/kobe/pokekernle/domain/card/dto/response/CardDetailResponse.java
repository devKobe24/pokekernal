package com.kobe.pokekernle.domain.card.dto.response;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.entity.PriceHistory;
import com.kobe.pokekernle.domain.card.service.CurrencyConverterService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * packageName    : com.kobe.pokekernle.domain.card.dto.response
 * fileName       : CardDetailResponse
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
public record CardDetailResponse(
        Long id,
        String name,
        String setName,
        String number,
        String rarity,
        String cardCondition,        // 카드 상태 (enum name)
        String cardConditionDesc,    // 카드 상태 설명
        String collectionStatus,     // 컬렉션 상태 (enum name)
        String collectionStatusDesc, // 컬렉션 상태 설명
        String imageUrl,
        String currentPrice,    // 현재가 (문자열)
        String currency,        // 통화(EUR, USD)
        Long salePrice,         // 희망 판매 가격 (원화, KRW)
        Integer quantity,       // 수량
        List<PriceHistoryDto> priceHistory // 그래프용 데이터
) {
    public static CardDetailResponse of(Card card, MarketPrice marketPrice, List<PriceHistory> histories, CurrencyConverterService currencyConverter) {
        String priceStr = "-";
        String curr = "USD"; // 기본값 USD

        if (marketPrice != null && marketPrice.getPrice() != null) {
            try {
                // EUR를 USD로 변환
                BigDecimal usdPrice = currencyConverter.convertToUsd(
                        marketPrice.getPrice(), 
                        marketPrice.getCurrency() != null ? marketPrice.getCurrency() : "USD"
                );
                priceStr = usdPrice != null ? usdPrice.toString() : "-";
                curr = "USD";
            } catch (Exception e) {
                // 변환 실패 시 기본값 사용
                priceStr = "-";
                curr = "USD";
            }
        }

        List<PriceHistoryDto> historyDtos = (histories == null) ? Collections.emptyList()
                : histories.stream()
                .map(h -> new PriceHistoryDto(h.getRecordedAt(), h.getPrice()))
                .collect(Collectors.toList());

        return new CardDetailResponse(
                card.getId(),
                card.getName() != null ? card.getName() : "Unknown",
                card.getSetName() != null ? card.getSetName() : "Unknown Set",
                card.getNumber(),
                card.getRarity() != null ? card.getRarity().name() : "UNKNOWN",
                card.getCardCondition() != null ? card.getCardCondition().name() : null, // 카드 상태
                card.getCardCondition() != null ? card.getCardCondition().getDescription() : null, // 카드 상태 설명
                card.getCollectionStatus() != null ? card.getCollectionStatus().name() : null, // 컬렉션 상태
                card.getCollectionStatus() != null ? card.getCollectionStatus().getDescription() : null, // 컬렉션 상태 설명
                card.getDisplayImageUrl() != null ? card.getDisplayImageUrl() : "/images/pokemon-card.png",
                priceStr,
                curr,
                card.getSalePrice(), // 희망 판매 가격 (원화)
                card.getQuantity() != null ? card.getQuantity() : 1, // 수량 (기본값 1)
                historyDtos
        );
    }
}
