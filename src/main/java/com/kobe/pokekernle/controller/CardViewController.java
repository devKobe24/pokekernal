package com.kobe.pokekernle.controller;

import com.kobe.pokekernle.domain.card.dto.response.CardDetailResponse;
import com.kobe.pokekernle.domain.card.response.CardListResponse;
import com.kobe.pokekernle.domain.card.service.CardService;
import com.kobe.pokekernle.domain.notice.dto.response.NoticeResponse;
import com.kobe.pokekernle.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

/**
 * packageName    : com.kobe.pokekernle.controller
 * fileName       : CardViewController
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Controller
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardViewController {

    private final CardService cardService;
    private final NoticeService noticeService;

    @GetMapping
    public String list(Model model, 
                       @RequestParam(value = "signup", required = false) String signup,
                       @RequestParam(value = "login", required = false) String login,
                       Principal principal,
                       Authentication authentication) {
        List<CardListResponse> cards = cardService.getAllCards();
        model.addAttribute("cards", cards);
        
        // 공지사항 정보 추가
        try {
            List<NoticeResponse> notices = noticeService.getActiveNotices();
            // 가장 우선순위가 높은 공지사항 1개만 모달로 표시
            if (notices != null && !notices.isEmpty()) {
                NoticeResponse latestNotice = notices.get(0);
                model.addAttribute("latestNotice", latestNotice);
                // 디버깅용 로그
                System.out.println("[DEBUG] 공지사항 로드 성공 - ID: " + latestNotice.getId() + ", 제목: " + latestNotice.getTitle());
            } else {
                System.out.println("[DEBUG] 활성화된 공지사항이 없습니다.");
            }
        } catch (Exception e) {
            // 공지사항 로드 실패 시 로그 출력
            System.err.println("[ERROR] 공지사항 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        if ("1".equals(signup)) {
            model.addAttribute("showRegisterModal", true);
        }
        if ("success".equals(signup)) {
            model.addAttribute("registerSuccess", true);
        }
        if ("1".equals(login)) {
            model.addAttribute("showLoginModal", true);
        }
        // 인증 정보 추가
        model.addAttribute("isAuthenticated", authentication != null && authentication.isAuthenticated());
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        return "cards/list"; // src/main/resources/templates/cards/list.html
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CardDetailResponse card = cardService.getCardDetail(id);
        model.addAttribute("card", card);
        return "cards/detail"; // src/main/resources/templates/cards/detail.html
    }
}
