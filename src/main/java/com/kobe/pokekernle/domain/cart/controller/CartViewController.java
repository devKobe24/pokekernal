package com.kobe.pokekernle.domain.cart.controller;

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
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartViewController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public String cartPage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        try {
            String email = principal.getName();
            log.info("[CART VIEW] 장바구니 페이지 접근 - Email: {}", email);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            log.info("[CART VIEW] 사용자 찾음 - User ID: {}, Email: {}", user.getId(), user.getEmail());

            CartResponse cart = cartService.getCart(user.getId());
            
            log.info("[CART VIEW] 장바구니 조회 완료 - Items Count: {}, Total Price: {}", 
                    cart.getItems().size(), cart.getTotalPrice());
            
            model.addAttribute("cart", cart);
            model.addAttribute("user", user);
        } catch (IllegalArgumentException e) {
            log.warn("[CART] 장바구니 조회 실패 - {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            // 빈 장바구니 응답 생성
            model.addAttribute("cart", CartResponse.builder()
                    .items(java.util.Collections.emptyList())
                    .totalPrice(0L)
                    .totalItems(0)
                    .build());
        } catch (Exception e) {
            log.error("[CART] 장바구니 조회 중 예상치 못한 오류 발생", e);
            model.addAttribute("error", "장바구니를 불러오는 중 오류가 발생했습니다.");
            // 빈 장바구니 응답 생성
            model.addAttribute("cart", CartResponse.builder()
                    .items(java.util.Collections.emptyList())
                    .totalPrice(0L)
                    .totalItems(0)
                    .build());
        }

        return "cart";
    }
}

