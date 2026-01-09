package com.kobe.pokekernle.domain.onepiece.box.service;

import com.kobe.pokekernle.domain.onepiece.box.dto.response.OnePieceBoxDetailResponse;
import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBox;
import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBoxMarketPrice;
import com.kobe.pokekernle.domain.onepiece.box.repository.OnePieceBoxMarketPriceRepository;
import com.kobe.pokekernle.domain.onepiece.box.repository.OnePieceBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * packageName    : com.kobe.pokekernle.domain.onepiece.box.service
 * fileName       : OnePieceBoxService
 * author         : kobe
 * date           : 2026. 1. 9.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 9.        kobe       최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnePieceBoxService {

    private final OnePieceBoxRepository onePieceBoxRepository;
    private final OnePieceBoxMarketPriceRepository marketPriceRepository;

    public OnePieceBoxDetailResponse getBoxDetail(Long boxId) {
        // 1. 박스 조회 (없으면 예외)
        OnePieceBox box = onePieceBoxRepository.findById(boxId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 박스입니다. ID=" + boxId));

        // 2. 현재 시세 조회
        OnePieceBoxMarketPrice marketPrice = marketPriceRepository.findByOnePieceBox(box).orElse(null);

        // 3. DTO 변환
        return OnePieceBoxDetailResponse.of(box, marketPrice);
    }
}
