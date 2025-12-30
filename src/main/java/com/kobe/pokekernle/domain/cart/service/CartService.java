package com.kobe.pokekernle.domain.cart.service;

import com.kobe.pokekernle.domain.cart.dto.request.AddCartItemRequest;
import com.kobe.pokekernle.domain.cart.dto.response.CartItemResponse;
import com.kobe.pokekernle.domain.cart.dto.response.CartResponse;
import com.kobe.pokekernle.domain.cart.entity.Cart;
import com.kobe.pokekernle.domain.cart.entity.CartItem;
import com.kobe.pokekernle.domain.cart.repository.CartItemRepository;
import com.kobe.pokekernle.domain.cart.repository.CartRepository;
import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
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
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 장바구니 가져오기 (없으면 생성)
     */
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("[CART] 새 장바구니 생성 - User ID: {}", user.getId());
                    Cart cart = Cart.builder()
                            .user(user)
                            .build();
                    Cart savedCart = cartRepository.save(cart);
                    log.info("[CART] 장바구니 생성 완료 - Cart ID: {}, User ID: {}", savedCart.getId(), user.getId());
                    return savedCart;
                });
    }

    /**
     * 장바구니에 아이템 추가
     */
    @Transactional
    public void addItem(Long userId, AddCartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다."));

        if (card.getSalePrice() == null || card.getSalePrice() == 0) {
            throw new IllegalArgumentException("판매 가격이 설정되지 않은 카드입니다.");
        }

        if (card.getQuantity() == null || card.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("요청한 수량이 재고를 초과합니다.");
        }

        Cart cart = getOrCreateCart(user);
        
        // 이미 장바구니에 있는 아이템인지 확인
        CartItem existingItem = cartItemRepository.findByCartIdAndCardId(cart.getId(), card.getId())
                .orElse(null);

        if (existingItem != null) {
            // 기존 아이템이 있으면 수량 추가
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > card.getQuantity()) {
                throw new IllegalArgumentException("요청한 수량이 재고를 초과합니다. (최대: " + card.getQuantity() + "개)");
            }
            existingItem.updateQuantity(newQuantity);
            log.info("[CART] 장바구니 아이템 수량 업데이트 - User ID: {}, Card ID: {}, Quantity: {} -> {}", 
                    userId, request.getCardId(), existingItem.getQuantity() - request.getQuantity(), newQuantity);
        } else {
            // 새 아이템 추가
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .card(card)
                    .quantity(request.getQuantity())
                    .unitPrice(card.getSalePrice())
                    .build();
            cart.addItem(cartItem);
            // CartItem을 데이터베이스에 저장 (CascadeType.ALL이지만 명시적으로 저장하는 것이 안전)
            cartItemRepository.save(cartItem);
            log.info("[CART] 장바구니에 새 아이템 추가됨 - User ID: {}, Card ID: {}, Quantity: {}, CartItem ID: {}", 
                    userId, request.getCardId(), request.getQuantity(), cartItem.getId());
        }
        
        // Cart를 저장하여 변경사항 반영 (CascadeType.ALL이 있지만 명시적으로 저장)
        cartRepository.save(cart);

        log.info("[CART] 장바구니에 추가 완료 - User ID: {}, Card ID: {}, Quantity: {}", userId, request.getCardId(), request.getQuantity());
    }

    /**
     * 장바구니 조회
     */
    @Transactional
    public CartResponse getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        log.info("[CART] 장바구니 조회 시작 - User ID: {}, Email: {}", userId, user.getEmail());

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("[CART] 장바구니가 없어 새로 생성 - User ID: {}", userId);
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });

        log.info("[CART] 장바구니 찾음 - Cart ID: {}, User ID: {}", cart.getId(), userId);

        // 장바구니 아이템들을 lazy loading으로 가져오기 위해 명시적으로 조회
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        log.info("[CART] 장바구니 아이템 개수 - Cart ID: {}, Items Count: {}", cart.getId(), cartItems.size());

        List<CartItemResponse> items = cartItems.stream()
                .map(item -> {
                    Card card = item.getCard();
                    return CartItemResponse.builder()
                            .id(item.getId())
                            .cardId(card.getId())
                            .cardName(card.getName() != null ? card.getName() : "")
                            .imageUrl(card.getDisplayImageUrl() != null ? card.getDisplayImageUrl() : "/images/pokemon-card.png")
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice() != null ? item.getUnitPrice() : 0L)
                            .totalPrice(item.getTotalPrice() != null ? item.getTotalPrice() : 0L)
                            .maxQuantity(card.getQuantity() != null ? card.getQuantity() : 0)
                            .build();
                })
                .collect(Collectors.toList());

        Long totalPrice = items.stream()
                .mapToLong(CartItemResponse::getTotalPrice)
                .sum();

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .items(items)
                .totalPrice(totalPrice)
                .totalItems(totalItems)
                .build();
    }

    /**
     * 장바구니 아이템 수량 업데이트
     */
    @Transactional
    public void updateItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다."));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        if (quantity > cartItem.getCard().getQuantity()) {
            throw new IllegalArgumentException("요청한 수량이 재고를 초과합니다. (최대: " + cartItem.getCard().getQuantity() + "개)");
        }

        cartItem.updateQuantity(quantity);
        log.info("[CART] 수량 업데이트 - CartItem ID: {}, Quantity: {}", cartItemId, quantity);
    }

    /**
     * 장바구니 아이템 삭제
     */
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다."));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        cartItemRepository.delete(cartItem);
        log.info("[CART] 아이템 삭제 - CartItem ID: {}", cartItemId);
    }

    /**
     * 장바구니 비우기
     */
    @Transactional
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUser(user)
                .orElse(null);

        if (cart != null) {
            cart.clear();
            log.info("[CART] 장바구니 비우기 - User ID: {}", userId);
        }
    }
}

