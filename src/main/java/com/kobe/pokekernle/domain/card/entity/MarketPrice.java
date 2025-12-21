package com.kobe.pokekernle.domain.card.entity;

import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * packageName    : com.kobe.pokekernle.domain.card.entity
 * fileName       : MarketPrice
 * author         : kobe
 * date           : 2025. 12. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 20.        kobe       최초 생성
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "market_prices")
public class MarketPrice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Card와 1:1 관계 (하나의 카드는 하나의 현재 시세를 가짐)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    // 가격은 정밀도가 중요하므로 BigDecimal 사용 (double/float 금지)
    @Column(precision = 10, scale = 2)
    private BigDecimal price; // 현재 시세

    private String currency; // 통화(USD, KRW)

    private String source; // 출처 (예: TCGPlayer, eBay)

    @Builder
    public MarketPrice(Card card, BigDecimal price, String currency, String source) {
        this.card = card;
        this.price = price;
        this.currency = currency;
        this.source = source;
    }

    public void updatePrice(BigDecimal newPrice) {
        this.price = newPrice;
    }

    // 생성자등 Builder 패턴 추가 가능
}
