package com.kobe.pokekernle.domain.collection.request;

import com.kobe.pokekernle.domain.collection.entity.CardCondition;

import java.math.BigDecimal;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.request
 * fileName       : AddCollectionRequest
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
public record AddCollectionRequest(
        Long cardId,
        BigDecimal purchasePrice,
        CardCondition condition, // MINT, PLAYED 등
        String memo
) {}
