package com.kobe.pokekernle.domain.order.controller;

import com.kobe.pokekernle.domain.cart.dto.response.CartResponse;
import com.kobe.pokekernle.domain.cart.service.CartService;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class OrderViewController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public String checkoutPage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            CartResponse cart = cartService.getCart(user.getId());
            
            if (cart.getItems().isEmpty()) {
                return "redirect:/cart";
            }

            model.addAttribute("cart", cart);
            model.addAttribute("user", user);
            
            // 기본 배송비 (추후 설정 가능하도록)
            model.addAttribute("shippingFee", 3000L);
            
        } catch (Exception e) {
            log.error("[CHECKOUT] 주문서 페이지 로드 실패", e);
            model.addAttribute("error", "주문서를 불러오는 중 오류가 발생했습니다.");
        }

        return "checkout";
    }
}

