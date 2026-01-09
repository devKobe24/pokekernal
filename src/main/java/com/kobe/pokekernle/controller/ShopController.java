package com.kobe.pokekernle.controller;

import com.kobe.pokekernle.domain.card.entity.CardCategory;
import com.kobe.pokekernle.domain.card.response.CardListResponse;
import com.kobe.pokekernle.domain.card.service.CardService;
import com.kobe.pokekernle.domain.notice.dto.response.NoticeResponse;
import com.kobe.pokekernle.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * packageName    : com.kobe.pokekernle.controller
 * fileName       : ShopController
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    : SHOP 메뉴 관련 컨트롤러
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final CardService cardService;
    private final NoticeService noticeService;

    /**
     * 포켓몬 싱글 카드 목록
     */
    @GetMapping("/pokemon-single")
    public String pokemonSingle(Model model, 
                                Principal principal,
                                Authentication authentication,
                                @RequestParam(value = "sortBy", defaultValue = "default") String sortBy) {
        List<CardListResponse> allCards = cardService.getAllCards();
        // 포켓몬 싱글 카드 필터링 (카테고리로 필터링)
        List<CardListResponse> cards = allCards.stream()
                .filter(card -> card.category() != null && CardCategory.POKEMON_SINGLE.name().equals(card.category()))
                .collect(Collectors.toList());
        
        // 정렬 적용
        cards = sortCards(cards, sortBy);
        
        model.addAttribute("cards", cards);
        model.addAttribute("category", "포켓몬 싱글 카드<br>[Pokemon Single Card]");
        model.addAttribute("sortBy", sortBy);
        addNoticeAttributes(model);
        addAuthAttributes(model, authentication);
        return "cards/list";
    }

    /**
     * 포켓몬 카드 목록
     */
    @GetMapping("/pokemon")
    public String pokemon(Model model, 
                              Principal principal,
                              Authentication authentication) {
        List<CardListResponse> allCards = cardService.getAllCards();
        // 포켓몬 카드 필터링
        List<CardListResponse> cards = allCards.stream()
                .filter(card -> card.name() != null && card.name().contains("포켓몬"))
                .collect(Collectors.toList());
        
        model.addAttribute("cards", cards);
        model.addAttribute("category", "포켓몬 카드<br>[Pokemon Card]");
        addNoticeAttributes(model);
        addAuthAttributes(model, authentication);
        return "cards/list";
    }

    /**
     * 원피스 싱글 카드 목록
     */
    @GetMapping("/onepiece-single")
    public String onePieceSingle(Model model, 
                                 Principal principal,
                                 Authentication authentication) {
        List<CardListResponse> allCards = cardService.getAllCards();
        // 원피스 싱글 카드 필터링
        List<CardListResponse> cards = allCards.stream()
                .filter(card -> card.name() != null && card.name().contains("원피스"))
                .collect(Collectors.toList());
        
        model.addAttribute("cards", cards);
        model.addAttribute("category", "원피스 싱글 카드<br>[Onepiece Single Card]");
        addNoticeAttributes(model);
        addAuthAttributes(model, authentication);
        return "cards/list";
    }

    /**
     * 원피스 카드 목록 (원피스 Box 포함)
     */
    @GetMapping("/onepiece")
    public String onePiece(Model model, 
                          Principal principal,
                          Authentication authentication) {
        List<CardListResponse> allCards = cardService.getAllCards();
        // 원피스 카드 및 원피스 Box 필터링
        List<CardListResponse> cards = allCards.stream()
                .filter(card -> {
                    // 카테고리로 필터링 (ONEPIECE_BOX 또는 이름에 "원피스" 포함)
                    if (card.category() != null) {
                        return card.category().equals("ONEPIECE_BOX") || 
                               card.category().equals("ONEPIECE_SINGLE") ||
                               (card.name() != null && card.name().contains("원피스"));
                    }
                    return card.name() != null && card.name().contains("원피스");
                })
                .collect(Collectors.toList());
        
        model.addAttribute("cards", cards);
        model.addAttribute("category", "원피스 카드<br>[Onepiece Card]");
        addNoticeAttributes(model);
        addAuthAttributes(model, authentication);
        return "cards/list";
    }

    /**
     * 공지사항 정보를 모델에 추가하는 헬퍼 메서드
     */
    private void addNoticeAttributes(Model model) {
        try {
            List<NoticeResponse> notices = noticeService.getActiveNotices();
            // 가장 우선순위가 높은 공지사항 1개만 모달로 표시
            if (notices != null && !notices.isEmpty()) {
                NoticeResponse latestNotice = notices.get(0);
                model.addAttribute("latestNotice", latestNotice);
            }
        } catch (Exception e) {
            // 공지사항 로드 실패 시 로그 출력
            System.err.println("[ERROR] 공지사항 로드 실패: " + e.getMessage());
        }
    }

    /**
     * 인증 정보를 모델에 추가하는 헬퍼 메서드
     */
    private void addAuthAttributes(Model model, Authentication authentication) {
        model.addAttribute("isAuthenticated", authentication != null && authentication.isAuthenticated());
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
    }

    /**
     * 카드 목록 정렬 헬퍼 메서드
     */
    private List<CardListResponse> sortCards(List<CardListResponse> cards, String sortBy) {
        return switch (sortBy) {
            case "price-asc" -> cards.stream()
                    .sorted(Comparator.comparing(
                            card -> card.salePrice() != null ? card.salePrice() : Long.MAX_VALUE,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
            case "price-desc" -> cards.stream()
                    .sorted(Comparator.comparing(
                            (CardListResponse card) -> card.salePrice() != null ? card.salePrice() : 0L,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            case "date-asc" -> cards.stream()
                    .sorted(Comparator.comparing(
                            CardListResponse::createdAt,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
            case "date-desc" -> cards.stream()
                    .sorted(Comparator.comparing(
                            CardListResponse::createdAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            case "price-change-asc" -> cards.stream()
                    .sorted(Comparator.comparing(
                            card -> card.priceChange() != null ? card.priceChange() : BigDecimal.ZERO,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
            case "price-change-desc" -> cards.stream()
                    .sorted(Comparator.comparing(
                            card -> card.priceChange() != null ? card.priceChange() : BigDecimal.ZERO,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            default -> cards; // 기본 정렬 (변경 없음)
        };
    }
}

