package com.kobe.pokekernle.domain.order.service;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import com.kobe.pokekernle.domain.cart.service.CartService;
import com.kobe.pokekernle.domain.order.dto.request.CreateOrderRequest;
import com.kobe.pokekernle.domain.order.dto.response.OrderItemResponse;
import com.kobe.pokekernle.domain.order.dto.response.OrderResponse;
import com.kobe.pokekernle.domain.order.entity.Order;
import com.kobe.pokekernle.domain.order.entity.OrderItem;
import com.kobe.pokekernle.domain.order.entity.OrderStatus;
import com.kobe.pokekernle.domain.order.repository.OrderRepository;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    /**
     * 주문 생성 (바로 구매)
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        long totalPrice = 0;
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(0L) // 나중에 업데이트
                .build();

        // 각 주문 아이템 처리
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Card card = cardRepository.findById(itemRequest.getCardId())
                    .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다. ID: " + itemRequest.getCardId()));

            if (card.getSalePrice() == null || card.getSalePrice() == 0) {
                throw new IllegalArgumentException("판매 가격이 설정되지 않은 카드입니다: " + card.getName());
            }

            if (card.getQuantity() == null || card.getQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("재고가 부족합니다. (카드: " + card.getName() + ", 요청: " + itemRequest.getQuantity() + "개, 재고: " + (card.getQuantity() != null ? card.getQuantity() : 0) + "개)");
            }

            long itemTotalPrice = card.getSalePrice() * itemRequest.getQuantity();
            totalPrice += itemTotalPrice;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .card(card)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(card.getSalePrice())
                    .totalPrice(itemTotalPrice)
                    .build();
            order.addItem(orderItem);
        }

        // 총 가격 설정
        order.updateTotalPrice(totalPrice);

        order = orderRepository.save(order);
        log.info("[ORDER] 주문 생성 - Order ID: {}, User ID: {}, Total Price: {}", order.getId(), userId, totalPrice);

        return toResponse(order);
    }

    /**
     * 장바구니에서 주문 생성
     */
    @Transactional
    public OrderResponse createOrderFromCart(Long userId) {
        var cart = cartService.getCart(userId);
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다.");
        }

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(cart.getItems().stream()
                .map(item -> {
                    CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
                    orderItem.setCardId(item.getCardId());
                    orderItem.setQuantity(item.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList()));

        OrderResponse orderResponse = createOrder(userId, request);
        
        // 주문 성공 시 장바구니 비우기
        cartService.clearCart(userId);
        
        return orderResponse;
    }

    /**
     * 주문 조회
     */
    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        return toResponse(order);
    }

    /**
     * 사용자의 주문 목록 조회
     */
    public List<OrderResponse> getOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Order 엔티티를 OrderResponse로 변환
     */
    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .cardId(item.getCard().getId())
                        .cardName(item.getCard().getName())
                        .imageUrl(item.getCard().getDisplayImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .items(items)
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}

