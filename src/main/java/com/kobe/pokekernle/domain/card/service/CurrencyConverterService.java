package com.kobe.pokekernle.domain.card.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 통화 변환 서비스
 * EUR를 USD로 변환하는 기능 제공
 */
@Service
public class CurrencyConverterService {

    // EUR to USD 환율 (기본값: 1.10, application.yml에서 설정 가능)
    @Value("${currency.exchange-rate.eur-to-usd:1.10}")
    private BigDecimal eurToUsdRate;

    /**
     * EUR를 USD로 변환
     * @param eurPrice EUR 가격
     * @return USD 가격 (소수점 2자리까지 반올림)
     */
    public BigDecimal convertEurToUsd(BigDecimal eurPrice) {
        if (eurPrice == null) {
            return null;
        }
        return eurPrice.multiply(eurToUsdRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 통화에 따라 가격을 USD로 변환
     * @param price 원본 가격
     * @param currency 원본 통화 (EUR, USD 등)
     * @return USD 가격
     */
    public BigDecimal convertToUsd(BigDecimal price, String currency) {
        if (price == null) {
            return null;
        }
        
        if (currency == null || currency.isBlank()) {
            // 통화 정보가 없으면 그대로 반환 (기본값 EUR로 가정)
            return convertEurToUsd(price);
        }
        
        switch (currency.toUpperCase()) {
            case "EUR":
                return convertEurToUsd(price);
            case "USD":
                return price; // 이미 USD
            default:
                // 알 수 없는 통화는 EUR로 가정하고 변환
                return convertEurToUsd(price);
        }
    }
}

