package com.kobe.pokekernle.controller;

import com.kobe.pokekernle.domain.card.dto.response.CardDetailResponse;
import com.kobe.pokekernle.domain.card.response.CardListResponse;
import com.kobe.pokekernle.domain.card.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping
    public String list(Model model) {
        List<CardListResponse> cards = cardService.getAllCards();
        model.addAttribute("cards", cards);
        return "cards/list"; // src/main/resources/templates/cards/list.html
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CardDetailResponse card = cardService.getCardDetail(id);
        model.addAttribute("card", card);
        return "cards/detail"; // src/main/resources/templates/cards/detail.html
    }
}
