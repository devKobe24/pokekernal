package com.kobe.pokekernle.domain.collection.dto.response;

import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.collection.entity.UserCard;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.dto.response
 * fileName       : MyCollectionResponse
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
public record MyCollectionResponse(
        Long id,               // UserCard ID
        Long cardId,           // Card ID (이동용)
        String cardName,
        String imageUrl,
        String condition,      // 상태 (MINT, PLAYED...)
        String purchasePrice,  // 구매가 (내가 산 가격)
        String currentPrice,   // 현재가 (시장 가격)
        String profitAmount,   // 수익금 (+10.00)
        String profitRate,     // 수익률 (+15%)
        boolean isGain,        // 이득인지 손해인지 (색상 표시용)
        String memo
) {
    public static MyCollectionResponse from(UserCard userCard, MarketPrice marketPrice) {
        BigDecimal buyPrice = userCard.getPurchasePrice();
        BigDecimal currPrice = (marketPrice != null && marketPrice.getPrice() != null) ? marketPrice.getPrice() : BigDecimal.ZERO;

        // 수익 계산
        BigDecimal profit = currPrice.subtract(buyPrice);
        boolean isGain = profit.compareTo(BigDecimal.ZERO) >= 0;

        // 수익률 계산 ( (현재가 - 구매가) / 구매가 * 100 )
        BigDecimal rate = BigDecimal.ZERO;
        if (buyPrice.compareTo(BigDecimal.ZERO) > 0) {
            rate = profit.divide(buyPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new MyCollectionResponse(
                userCard.getId(),
                userCard.getCard().getId(),
                userCard.getCard().getName(),
                userCard.getCard().getImageUrl(),
                userCard.getCardCondition().name(),
                "€ " + buyPrice,
                (currPrice.equals(BigDecimal.ZERO)) ? "-" : "€ " + currPrice,
                (isGain ? "+" : "") + "€ " + profit,
                (isGain ? "+" : "") + rate.setScale(2, RoundingMode.HALF_UP) + "%",
                isGain,
                userCard.getMemo()
        );
    }
}
