package com.kobe.pokekernle.domain.card.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * packageName    : com.kobe.pokekernle.domain.card.entity
 * fileName       : PriceHistory
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
@Table(name = "price_histories")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private LocalDate recordedAt; // 기록된 날짜

    @Builder
    public PriceHistory(Card card, BigDecimal price, LocalDate recordedAt) {
        this.card = card;
        this.price = price;
        this.recordedAt = recordedAt != null ? recordedAt : LocalDate.now();
    }
}
