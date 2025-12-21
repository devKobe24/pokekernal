package com.kobe.pokekernle.domain.admin.controller;

import com.kobe.pokekernle.domain.card.service.CardPriceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName    : com.kobe.pokekernle.domain.admin.controller
 * fileName       : AdminController
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CardPriceSyncService cardPriceSyncService;

    /**
     * 수동 데이터 수집 트리거
     * 사용법: http://localhost:8080/admin/collect?query=set.id:sv3pt5
     */
    @GetMapping("/collect")
    public String triggerCardCollection(@RequestParam String query) {
        log.info("[ADMIN] 수동 데이터 수집 요청 - Query: {}", query);

        // 비동기 처리하지 않고 동기로 처리하여 결과를 확실히 기다림 (테스트 용이성 위함)
        cardPriceSyncService.syncLatestPrices(query);

        return String.format("수집 완료! Query: %s (로그와 DB를 확인하세요)", query);
    }
}
