package com.kobe.pokekernle.domain.onepiece.box.repository;

import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBox;
import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBoxMarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * packageName    : com.kobe.pokekernle.domain.onepiece.box.repository
 * fileName       : OnePieceBoxMarketPriceRepository
 * author         : kobe
 * date           : 2026. 1. 9.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 9.        kobe       최초 생성
 */
public interface OnePieceBoxMarketPriceRepository extends JpaRepository<OnePieceBoxMarketPrice, Long> {
    // 특정 박스의 현재 시세를 찾아보기 위함
    Optional<OnePieceBoxMarketPrice> findByOnePieceBox(OnePieceBox onePieceBox);

    List<OnePieceBoxMarketPrice> findAllByOnePieceBoxIn(List<OnePieceBox> onePieceBoxes);
}
