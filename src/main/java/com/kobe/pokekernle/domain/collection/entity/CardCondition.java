package com.kobe.pokekernle.domain.collection.entity;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.entity
 * fileName       : CardCondition
 * author         : kobe
 * date           : 2025. 12. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 20.        kobe       최초 생성
 */
public enum CardCondition {
    MINT("미개봄/신품"),
    NEAR_MINT("S급 (거의 새것)"),
    EXCELLENT("A급 (약간의 사용감)"),
    LIGHTLY_PLAYED("B급 (눈에 띄는 상처)"),
    PLAYED("C급 (플레이용)"),
    HEAVILY_PLAYED("D급 (심한 사용 흔적)"),
    DAMAGED("파손됨");

    private final String description;

    CardCondition(String description) {
        this.description = description;
    }
}
