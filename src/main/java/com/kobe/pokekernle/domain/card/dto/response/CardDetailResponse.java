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
        String imageUrl,
        String currentPrice,    // 현재가 (문자열)
        String currency,        // 통화(EUR, USD)
        Long salePrice,         // 희망 판매 가격 (원화, KRW)
        List<PriceHistoryDto> priceHistory // 그래프용 데이터
) {
    public static CardDetailResponse of(Card card, MarketPrice marketPrice, List<PriceHistory> histories, CurrencyConverterService currencyConverter) {
        String priceStr = "-";
        String curr = "USD"; // 기본값 USD

        if (marketPrice != null && marketPrice.getCard() != null) {
            // EUR를 USD로 변환
            BigDecimal usdPrice = currencyConverter.convertToUsd(
                    marketPrice.getPrice(), 
                    marketPrice.getCurrency()
            );
            priceStr = usdPrice != null ? usdPrice.toString() : "-";
            curr = "USD";
        }

        List<PriceHistoryDto> historyDtos = (histories == null) ? Collections.emptyList()
                : histories.stream()
                .map(h -> new PriceHistoryDto(h.getRecordedAt(), h.getPrice()))
                .collect(Collectors.toList());

        return new CardDetailResponse(
                card.getId(),
                card.getName(),
                card.getSetName(),
                card.getNumber(),
                card.getRarity().name(),
                card.getDisplayImageUrl(), // 업로드된 이미지 우선 사용
                priceStr,
                curr,
                card.getSalePrice(), // 희망 판매 가격 (원화)
                historyDtos
        );
    }
}
