package com.kobe.pokekernle.domain.card.entity;

/**
 * packageName    : com.kobe.pokekernle.domain.card.entity
 * fileName       : Rarity
 * author         : kobe
 * date           : 2025. 12. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 20.        kobe       최초 생성
 */
public enum Rarity {
    COMMON,             // 일반
    UNCOMMON,           // 언커먼
    RARE,               // 레어 (일반 레어)
    HOLO_RARE,          // 홀로 레어(반짝이는 레어)
    DOUBLE_RARE,        // RR (Double Rare) - V, EX 등
    ULTRA_RARE,         // UR/SR (Full Art 등)
    ILLUSTRATION_RARE,  // AR/SAR (일러스트 강조)
    SECRET_RARE,        // 시크릿 레어 (번호가 101/100 처럼 넘어가는 것)
    PROMO,              // 프로모
    UNKNOWN             // 알 수 없음 (매핑 실패 시)
    // 필요에 따라 계속 추가
}
