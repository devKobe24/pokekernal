package com.kobe.pokekernle.domain.collection.entity;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.entity
 * fileName       : UserCard
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_cards")
public class UserCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 가지고 있는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 카드인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    // 실물 카드 상태(S급, A급 등)
    @Enumerated(EnumType.STRING)
    private CardCondition cardCondition;

    // 카드 현재 상태(보유중, 판매중 등)
    @Enumerated(EnumType.STRING)
    private CollectionStatus status;

    // 내가 구매한 가격(시세와 비교하여 수익률 계산 가능)
    @Column(precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    // 사용자가 직접 찍은 인증샷 URL (S3)
    private String userImageUrl;

    @Column(length = 500)
    private String memo; // 개인 메모 (예: "서대전역 자판기에서 구매한 151 팩에서 뽑음")

    @Builder
    public UserCard(User user,
                    Card card,
                    CardCondition cardCondition,
                    CollectionStatus status,
                    BigDecimal purchasePrice,
                    String userImageUrl,
                    String memo
    ) {
        this.user = user;
        this.card = card;
        this.cardCondition = cardCondition;
        this.status = status;
        this.purchasePrice = purchasePrice;
        this.userImageUrl = userImageUrl;
        this.memo = memo;
    }
}
