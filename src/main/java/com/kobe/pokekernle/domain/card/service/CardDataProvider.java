package com.kobe.pokekernle.domain.card.service;

import com.kobe.pokekernle.domain.card.dto.external.PokemonTcgApiResponse;

/**
 * packageName    : com.kobe.pokekernle.domain.card.service
 * fileName       : CardDataProvider
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
public interface CardDataProvider {
    /**
     * 외부 API를 통해 조건(Query)에 맞는 카드 데이터를 수집합니다.
     * @param query 검색 쿼리 (예: "set.id.sv3pt5")
     * @return API 응답 DTO
     */
    PokemonTcgApiResponse fetchCardsBySet(String query);

    /**
     * 특정 페이지의 카드 데이터를 가져옵니다.
     * @param query 검색 쿼리
     * @param page 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 항목 수 (최대 250)
     * @return API 응답 DTO
     */
    default PokemonTcgApiResponse fetchCardsBySet(String query, int page, int pageSize) {
        // 기본 구현: 첫 페이지만 반환 (하위 호환성)
        return fetchCardsBySet(query);
    }
}
