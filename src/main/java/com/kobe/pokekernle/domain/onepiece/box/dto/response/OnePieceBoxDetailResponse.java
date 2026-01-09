package com.kobe.pokekernle.domain.onepiece.box.dto.response;

import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBox;
import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBoxMarketPrice;

/**
 * packageName    : com.kobe.pokekernle.domain.onepiece.box.dto.response
 * fileName       : OnePieceBoxDetailResponse
 * author         : kobe
 * date           : 2026. 1. 9.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 9.        kobe       최초 생성
 */
public record OnePieceBoxDetailResponse(
        Long id,
        String name,
        String setName,
        String condition,              // 박스 상태 (enum name)
        String conditionDesc,          // 박스 상태 설명
        String collectionStatus,        // 컬렉션 상태 (enum name)
        String collectionStatusDesc,   // 컬렉션 상태 설명
        String frontImageUrl,          // 앞면 이미지
        String backImageUrl,           // 뒷면 이미지
        String leftImageUrl,           // 왼쪽면 이미지
        String rightImageUrl,          // 오른쪽면 이미지
        String topImageUrl,            // 윗면 이미지
        String bottomImageUrl,         // 아랫면 이미지
        String currentPrice,           // 현재가 (문자열)
        String currency,              // 통화
        Long salePrice,               // 희망 판매 가격 (원화, KRW)
        Integer quantity              // 수량
) {
    public static OnePieceBoxDetailResponse of(OnePieceBox box, OnePieceBoxMarketPrice marketPrice) {
        String priceStr = "-";
        String curr = "KRW"; // 기본값 KRW

        if (marketPrice != null && marketPrice.getPrice() != null) {
            priceStr = marketPrice.getPrice().toString();
            curr = marketPrice.getCurrency() != null ? marketPrice.getCurrency() : "KRW";
        }

        return new OnePieceBoxDetailResponse(
                box.getId(),
                box.getName() != null ? box.getName() : "Unknown",
                box.getSetName() != null ? box.getSetName() : "Unknown Set",
                box.getCondition() != null ? box.getCondition().name() : null,
                box.getCondition() != null ? box.getCondition().getDescription() : null,
                box.getCollectionStatus() != null ? box.getCollectionStatus().name() : null,
                box.getCollectionStatus() != null ? box.getCollectionStatus().getDescription() : null,
                box.getFrontImageUrl(),
                box.getBackImageUrl(),
                box.getLeftImageUrl(),
                box.getRightImageUrl(),
                box.getTopImageUrl(),
                box.getBottomImageUrl(),
                priceStr,
                curr,
                box.getSalePrice(),
                box.getQuantity() != null ? box.getQuantity() : 1
        );
    }
}
