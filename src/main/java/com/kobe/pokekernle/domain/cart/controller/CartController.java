package com.kobe.pokekernle.domain.cart.controller;

import com.kobe.pokekernle.domain.cart.dto.request.AddCartItemRequest;
import com.kobe.pokekernle.domain.cart.dto.response.CartResponse;
import com.kobe.pokekernle.domain.cart.service.CartService;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    /**
     * 현재 사용자 정보 가져오기
     */
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 장바구니에 아이템 추가
     */
    @PostMapping("/items")
    public ResponseEntity<?> addItem(@Valid @RequestBody AddCartItemRequest request, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            cartService.addItem(user.getId(), request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "장바구니에 추가되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[CART] 장바구니 추가 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "장바구니 추가 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 조회
     */
    @GetMapping
    public ResponseEntity<?> getCart(Principal principal) {
        try {
            User user = getCurrentUser(principal);
            CartResponse cart = cartService.getCart(user.getId());
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[CART] 장바구니 조회 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "장바구니 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 아이템 수량 업데이트
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            Principal principal) {
        try {
            User user = getCurrentUser(principal);
            cartService.updateItemQuantity(user.getId(), cartItemId, quantity);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "수량이 업데이트되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[CART] 수량 업데이트 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "수량 업데이트 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 아이템 삭제
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long cartItemId, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            cartService.removeItem(user.getId(), cartItemId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "장바구니에서 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[CART] 아이템 삭제 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "아이템 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 비우기
     */
    @DeleteMapping
    public ResponseEntity<?> clearCart(Principal principal) {
        try {
            User user = getCurrentUser(principal);
            cartService.clearCart(user.getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "장바구니가 비워졌습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[CART] 장바구니 비우기 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "장바구니 비우기 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

