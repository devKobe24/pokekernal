package com.kobe.pokekernle.domain.collection.entity;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.entity
 * fileName       : CollectionStatus
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
public enum CollectionStatus {
    OWNED("보유중"),
    WHISHLIST("위시리스트(갖고싶음)"),
    FOR_TRADE("교환중"),
    FOR_SALE("판매중"),
    TRADED("교환 완료"),
    SOLD("판매 완료");

    private final String description;

    CollectionStatus(String description) {
        this.description = description;
    }
}
