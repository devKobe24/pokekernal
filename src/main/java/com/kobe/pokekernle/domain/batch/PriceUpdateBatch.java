package com.kobe.pokekernle.domain.batch;

import com.kobe.pokekernle.domain.card.service.CardPriceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * packageName    : com.kobe.pokekernle.domain.batch
 * fileName       : PriceUpdateBatch
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdateBatch {

    private final CardPriceSyncService cardPriceSyncService;

    // MVP 전략: 모든 카드를 다 긁어 오면 API Rate Limit에 걸릴 수 있으므로,
    // 우선순위가 높은 특정 세트(예: Pokemon 151 - id:sv3pt5)를 타겟팅합니다.
    // 추후에는 DB에 'target_sets' 테이블을 만들어 루프를 돌리는 방식으로 고도화합니다.

    // Cron 표현식: 초 분 시 일 원 요일 (매일 새벽 4시 0분 0초)
    @Scheduled(cron = "0 0 4 * * *")
    public void dailyPriceSyncTask() {
        log.info("[Scheduler] 일일 시세 업데이트 배치를 시작합니다.");

        long start = System.currentTimeMillis();

        // 예시: 가장 인기 있는 'Pokemon 151' 세트 데이터 수집
        // 실제로는 "set.id:sv3pt5: 같은 쿼리를 사용
        cardPriceSyncService.syncLatestPrices("set.id:sv3pt5");

        long end = System.currentTimeMillis();
        log.info("[Scheduler] 배치 종료. 소요 시간: {}ms", (end - start));
    }
}
