package com.kobe.pokekernle.domain.onepiece.box.entity;

import com.kobe.pokekernle.domain.card.entity.CardCategory;
import com.kobe.pokekernle.domain.collection.entity.CardCondition;
import com.kobe.pokekernle.domain.collection.entity.CollectionStatus;
import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.kobe.pokekernle.domain.onepiece.box.entity
 * fileName       : OnePieceBox
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
@Table(name = "one_piece_box")
public class OnePieceBox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 상품명 (예: 새로운 황제: ONE PIECE)

    @Column(nullable = false)
    private String setName; // 세트 이름 (예: OPK-09)

    @Enumerated(EnumType.STRING)
    @Column(name = "box_condition", length = 50)
    private CardCondition condition; // 박스 상태 (MINT, NEAR_MINT 등)

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_status", length = 50)
    private CollectionStatus collectionStatus; // 컬렉션 상태 (OWNED, FOR_SALE 등)

    // 6면의 이미지 URL 필드
    @Column(length = 1000)
    private String frontImageUrl; // 앞면

    @Column(length = 1000)
    private String backImageUrl; // 뒷면

    @Column(length = 1000)
    private String leftImageUrl; // 왼쪽면

    @Column(length = 1000)
    private String rightImageUrl; // 오른쪽면

    @Column(length = 1000)
    private String topImageUrl; // 윗면

    @Column(length = 1000)
    private String bottomImageUrl; // 아랫면

    @Column(precision = 12, scale = 0)
    private Long salePrice; // 희망 판매 가격 (원화, KRW)

    @Column(precision = 10, scale = 0)
    private Integer quantity; // 수량 (기본값: 1)

    @Enumerated(EnumType.STRING)
    @Column(name = "card_category", length = 50)
    private CardCategory cardCategory;

    @Builder
    public OnePieceBox(String name,
                       String setName,
                       CardCondition condition,
                       CollectionStatus collectionStatus,
                       String frontImageUrl,
                       String backImageUrl,
                       String leftImageUrl,
                       String rightImageUrl,
                       String topImageUrl,
                       String bottomImageUrl,
                       Long salePrice,
                       Integer quantity,
                       CardCategory cardCategory) {
        this.name = name;
        this.setName = setName;
        this.condition = condition;
        this.collectionStatus = collectionStatus;
        this.frontImageUrl = frontImageUrl;
        this.backImageUrl = backImageUrl;
        this.leftImageUrl = leftImageUrl;
        this.rightImageUrl = rightImageUrl;
        this.topImageUrl = topImageUrl;
        this.bottomImageUrl = bottomImageUrl;
        this.salePrice = salePrice;
        this.quantity = quantity != null ? quantity : 1; // 기본값 1
        this.cardCategory = cardCategory;
    }

    /**
     * 특정 면의 이미지 URL 변환 (null 체크 포함)
     */
    public String getImageUrl(String face) {
        return switch (face.toLowerCase()) {
            case "front" -> frontImageUrl;
            case "back" -> backImageUrl;
            case "left" -> leftImageUrl;
            case "right" -> rightImageUrl;
            case "top" -> topImageUrl;
            case "bottom" -> bottomImageUrl;
            default -> null;
        };
    }
}
