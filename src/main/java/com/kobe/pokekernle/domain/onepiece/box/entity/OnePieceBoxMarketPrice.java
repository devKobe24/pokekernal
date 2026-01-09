package com.kobe.pokekernle.domain.onepiece.box.entity;

import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * packageName    : com.kobe.pokekernle.domain.onepiece.box.entity
 * fileName       : OnePieceBoxMarketPrice
 * author         : kobe
 * date           : 2026. 1. 9.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 9.        kobe       최초 생성
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "one_piece_box_market_price")
public class OnePieceBoxMarketPrice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OnePiece Box와 1:1 관계 (하나의 박스는 하나의 현재 시세를 가짐)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "one_piece_box_id")
    private OnePieceBox onePieceBox;

    // 가격은 정밀도가 중요하므로 BigDecimal 사용 (double/float 금지)
    @Column(precision = 10, scale = 2)
    private BigDecimal price; // 현재 시세

    private String currency; // 통화(KRW)

    @Builder
    public OnePieceBoxMarketPrice(OnePieceBox onePieceBox, BigDecimal price, String currency) {
        this.onePieceBox = onePieceBox;
        this.price = price;
        this.currency = currency;
    }

    public void updatePrice(BigDecimal newPrice) { this.price = newPrice; }
}
