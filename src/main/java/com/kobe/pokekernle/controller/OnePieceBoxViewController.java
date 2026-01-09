package com.kobe.pokekernle.controller;

import com.kobe.pokekernle.domain.onepiece.box.dto.response.OnePieceBoxDetailResponse;
import com.kobe.pokekernle.domain.onepiece.box.service.OnePieceBoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * packageName    : com.kobe.pokekernle.controller
 * fileName       : OnePieceBoxViewController
 * author         : kobe
 * date           : 2026. 1. 9.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 9.        kobe       최초 생성
 */
@Controller
@RequestMapping("/onepiece-boxes")
@RequiredArgsConstructor
public class OnePieceBoxViewController {

    private final OnePieceBoxService onePieceBoxService;

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OnePieceBoxDetailResponse box = onePieceBoxService.getBoxDetail(id);
        model.addAttribute("box", box);
        return "onepiece-box/detail"; // src/main/resources/templates/onepiece-box/detail.html
    }
}
