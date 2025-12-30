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
    MIRROR_POKE_BALL,               // 미러볼(포켓볼 버전)
    MIRROR_MASTER_BALL,             // 미러볼(마스터볼 버전)
    COMMON,                         // 일반
    UNCOMMON,                       // 언커먼
    RARE,                           // 레어 (일반 레어)
    DOUBLE_RARE,                    // RR (Double Rare) - V, EX 등
    SUPER_RARE,                     // SR (Super Rare)
    ART_RARE,                       // AR (Art Rare)
    SPECIAL_ART_RARE,               // SAR
    ULTRA_RARE,                     // UR
    ACE,                            // ACE SPEC (에이스 스펙)
    SHINY,                          // S (Shiny, 색이 다른 포켓몬(이로치)을 위한 레어도)
    SHINING_SUPER_RARE,             // SSR (샤이닝 슈퍼레어, 색이 다른 GX, V, VMAX, ex 포켓몬의 등급)
    BLACK_WHITE_RARE,               // BWR (블랙 화이트 레어, 확장팩 블랙 볼트, 화이트 플레어 전용등급)
    MEGA_ULTRA_RARE,                // MUR (메가 울트라 레어, 메가브레이브, 메가심포니아부터 추가된 등급)
    MEGA_ATTACK_RARE,               // MAR (메가 어택 레어, MEGA드림ex 부터 추가된 등급)
    PROMO                           // PROMO 카드를 통해 얻은 등급
    // 필요에 따라 계속 추가
}
