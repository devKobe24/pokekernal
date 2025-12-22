package com.kobe.pokekernle.domain.card.repository;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * packageName    : com.kobe.pokekernle.domain.card.repository
 * fileName       : MarketPriceRepository
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {
    // 특정 카드의 현재 시세를 찾아보기 위함
    Optional<MarketPrice> findByCard(Card card);

    // 카드 리스트에 포함된 모든 시세 정보를 한 번에 조회 (IN 쿼리 사용)
    List<MarketPrice> findAllByCardIn(List<Card> cards);
}
