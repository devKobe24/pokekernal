package com.kobe.pokekernle.domain.card.dto.response;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.entity.PriceHistory;

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
        List<PriceHistoryDto> priceHistory // 그래프용 데이터
) {
    public static CardDetailResponse of(Card card, MarketPrice marketPrice, List<PriceHistory> histories) {
        String priceStr = "-";
        String curr = "";

        if (marketPrice != null && marketPrice.getCard() != null) {
            priceStr = marketPrice.getPrice().toString();
            curr = marketPrice.getCurrency();
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
                card.getImageUrl(),
                priceStr,
                curr,
                historyDtos
        );
    }
}
