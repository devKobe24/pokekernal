package com.kobe.pokekernle.domain.card.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

/**
 * packageName    : com.kobe.pokekernle.domain.card.dto.external
 * fileName       : ImageDto
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
public class ImageDto {
    private String small;
    private String large;
}
