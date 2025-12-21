package com.kobe.pokekernle.domain.card.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * packageName    : com.kobe.pokekernle.domain.card.dto.response
 * fileName       : PriceHistoryDto
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
public record PriceHistoryDto(
        LocalDate date,
        BigDecimal price
) {
}
