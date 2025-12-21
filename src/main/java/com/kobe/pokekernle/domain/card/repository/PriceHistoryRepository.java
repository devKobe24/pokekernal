package com.kobe.pokekernle.domain.card.repository;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * packageName    : com.kobe.pokekernle.domain.card.repository
 * fileName       : PriceHistoryRepository
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    // 특정 카드의 시세 기록을 날짜 오름차순으로 조회
    List<PriceHistory> findAllByCardOrderByRecordedAtAsc(Card card);
}
