package com.kobe.pokekernle.domain.collection.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.dto.response
 * fileName       : CollectionSummaryResponse
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
public record CollectionSummaryResponse(
        String totalPurchasePrice,                  // 총 구매액
        String totalCurrentValue,                   // 총 평가액 (현재 가치)
        String totalProfit,                         // 총 손익금 (+ € 50.00)
        String totalProfitRate,                     // 총 수익률 (+ 25.5%)
        boolean isGain                              // 이득 여부 (색상 표시용)
) {
    public static CollectionSummaryResponse of(BigDecimal totalBuy, BigDecimal totalCurr) {
        BigDecimal profit = totalCurr.subtract(totalBuy);
        boolean isGain = profit.compareTo(BigDecimal.ZERO) > 0;

        // 수익률 계산 (구매액이 0이면 0%)
        BigDecimal rate = BigDecimal.ZERO;
        if (totalBuy.compareTo(BigDecimal.ZERO) > 0) {
            rate = profit.divide(totalBuy, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new CollectionSummaryResponse(
                "€ " + totalBuy,
                "€ " + totalCurr,
                (isGain ? "+" : "") + "€ " + profit,
                (isGain ? "+" : "") + rate.setScale(2, RoundingMode.HALF_UP) + "%",
                isGain
        );
    }
}
