package com.kobe.pokekernle.domain.card.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

/**
 * packageName    : com.kobe.pokekernle.domain.card.dto.external
 * fileName       : PokemonCardDto
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
public class PokemonCardDto {
    private String id;
    private String name;
    private String number;
    private String rarity;

    @JsonProperty("set")
    private SetDto set; // 분리한 SetDto 사용

    @JsonProperty("images")
    private ImageDto images; // 분리한 ImageDto 사용

    @JsonProperty("cardmarket")
    private CardMarketDto cardmarket; // 분리한 CardMarketDto 사용
}
