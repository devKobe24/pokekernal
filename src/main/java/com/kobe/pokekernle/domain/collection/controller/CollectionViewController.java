package com.kobe.pokekernle.domain.collection.controller;

import com.kobe.pokekernle.domain.collection.dto.response.CollectionSummaryResponse;
import com.kobe.pokekernle.domain.collection.dto.response.MyCollectionResponse;
import com.kobe.pokekernle.domain.collection.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.controller
 * fileName       : CollectionViewController
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Controller
@RequestMapping("/collection")
@RequiredArgsConstructor
public class CollectionViewController {

    private final CollectionService collectionService;

    @GetMapping
    public String myCollection(Model model) {
        // 임시 유저 ID: 1 (지우)
        Long userId = 1L;

        // 리스트 데이터
        List<MyCollectionResponse> collection = collectionService.getMyCollection(userId);

        // 요약 데이터
        CollectionSummaryResponse summary = collectionService.getCollectionSummary(userId);

        model.addAttribute("collection", collection);
        model.addAttribute("summary", summary); // 화면으로 전달

        // 수익률 합계 계산 (화면 상단 표시용) - 간단하게 0으로 처리하거나 서비스에서 계산 가능
        return "collection/List"; // templates/collection/list.html
    }
}
