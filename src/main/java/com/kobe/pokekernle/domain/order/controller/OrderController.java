package com.kobe.pokekernle.domain.order.controller;

import com.kobe.pokekernle.domain.order.dto.request.CreateOrderRequest;
import com.kobe.pokekernle.domain.order.dto.response.OrderResponse;
import com.kobe.pokekernle.domain.order.service.OrderService;
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
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
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
     * 주문 생성 (바로 구매)
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            OrderResponse order = orderService.createOrder(user.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[ORDER] 주문 생성 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "주문 생성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니에서 주문 생성
     */
    @PostMapping("/from-cart")
    public ResponseEntity<?> createOrderFromCart(Principal principal) {
        try {
            User user = getCurrentUser(principal);
            OrderResponse order = orderService.createOrderFromCart(user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[ORDER] 장바구니에서 주문 생성 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "주문 생성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 주문 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            OrderResponse order = orderService.getOrder(user.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[ORDER] 주문 조회 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "주문 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 주문 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getOrders(Principal principal) {
        try {
            User user = getCurrentUser(principal);
            List<OrderResponse> orders = orderService.getOrders(user.getId());
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[ORDER] 주문 목록 조회 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "주문 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

