package com.kobe.pokekernle.domain.card.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * packageName    : com.kobe.pokekernle.domain.card.dto.external
 * fileName       : PokemonTcgApiResponse
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonTcgApiResponse {
    private List<PokemonCardDto> data;

    private Integer page;
    private Integer pageSize;
    private Integer count;
    private Integer totalCount;
}
