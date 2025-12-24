package com.kobe.pokekernle.domain.card.entity;

import com.kobe.pokekernle.domain.collection.entity.CardCondition;
import com.kobe.pokekernle.domain.collection.entity.CollectionStatus;
import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.kobe.pokekernle.domain.card.entity
 * fileName       : Card
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
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_name", columnList = "name"),
        @Index(name = "idx_set_name", columnList = "setName")
})
public class Card extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 카드 이름 (예: Charizard)

    @Column(nullable = false)
    private String setName; // 세트 이름 (예: 151, 초전 브레이)

    private String number; // 카드 번호 (예: 7/109)

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", length = 50)
    private Rarity rarity; // 희귀도

    @Enumerated(EnumType.STRING)
    @Column(name = "card_condition", length = 50)
    private CardCondition cardCondition; // 카드 상태 (MINT, NEAR_MINT 등)

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_status", length = 50)
    private CollectionStatus collectionStatus; // 컬렉션 상태 (OWNED, FOR_SALE 등)

    @Column(length = 1000)
    private String imageUrl; // 카드 이미지 URL (S3 또는 외부 링크)

    @Column(length = 1000)
    private String uploadedImageUrl; // 사용자가 업로드한 이미지 URL (우선 사용)

    @Column(precision = 12, scale = 0)
    private Long salePrice; // 희망 판매 가격 (원화, KRW)

    // 외부 API(예: TCGPlayer)와의 연동을 위한 ID
    @Column(unique = true)
    private String externalId;

    @Builder
    public Card(String name, String setName, String number, Rarity rarity, CardCondition cardCondition, CollectionStatus collectionStatus, String imageUrl, String uploadedImageUrl, Long salePrice, String externalId) {
        this.name = name;
        this.setName = setName;
        this.number = number;
        this.rarity = rarity;
        this.cardCondition = cardCondition;
        this.collectionStatus = collectionStatus;
        this.imageUrl = imageUrl;
        this.uploadedImageUrl = uploadedImageUrl;
        this.salePrice = salePrice;
        this.externalId = externalId;
    }

    /**
     * 업로드된 이미지가 있으면 우선 사용, 없으면 API 이미지 사용
     * 이미지가 없으면 기본 플레이스홀더 반환
     */
    public String getDisplayImageUrl() {
        if (uploadedImageUrl != null && !uploadedImageUrl.isBlank()) {
            return uploadedImageUrl;
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }
        // 이미지가 없으면 기본 플레이스홀더 반환
        return "/images/pokemon-card.png";
    }

    /**
     * 업로드된 이미지 URL 설정
     */
    public void setUploadedImageUrl(String uploadedImageUrl) {
        this.uploadedImageUrl = uploadedImageUrl;
    }

    /**
     * 희망 판매 가격 설정
     */
    public void setSalePrice(Long salePrice) {
        this.salePrice = salePrice;
    }

    /**
     * 카드 정보 수정
     */
    public void updateCard(String name, String setName, String number, Rarity rarity, CardCondition cardCondition, CollectionStatus collectionStatus,
                          String imageUrl, String uploadedImageUrl, Long salePrice) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (setName != null && !setName.isBlank()) {
            this.setName = setName;
        }
        if (number != null) {
            this.number = number;
        }
        if (rarity != null) {
            this.rarity = rarity;
        }
        if (cardCondition != null) {
            this.cardCondition = cardCondition;
        }
        if (collectionStatus != null) {
            this.collectionStatus = collectionStatus;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (uploadedImageUrl != null) {
            this.uploadedImageUrl = uploadedImageUrl;
        }
        if (salePrice != null) {
            this.salePrice = salePrice;
        }
    }
}
