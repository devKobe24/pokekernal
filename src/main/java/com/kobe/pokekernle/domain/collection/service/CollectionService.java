package com.kobe.pokekernle.domain.collection.service;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import com.kobe.pokekernle.domain.card.repository.MarketPriceRepository;
import com.kobe.pokekernle.domain.collection.dto.response.CollectionSummaryResponse;
import com.kobe.pokekernle.domain.collection.dto.response.MyCollectionResponse;
import com.kobe.pokekernle.domain.collection.entity.CollectionStatus;
import com.kobe.pokekernle.domain.collection.entity.UserCard;
import com.kobe.pokekernle.domain.collection.repository.UserCardRepository;
import com.kobe.pokekernle.domain.collection.request.AddCollectionRequest;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.service
 * fileName       : CollectionService
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final UserCardRepository userCardRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final MarketPriceRepository marketPriceRepository;

    @Transactional(readOnly = true)
    public List<MyCollectionResponse> getMyCollection(Long userId) {
        // 1. 내 카드 목록 조회
        List<UserCard> myCards = userCardRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        if (myCards.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 내 카드 목록에서 'Card' 엔티티만 추출
        List<Card> cards = myCards.stream()
                .map(UserCard::getCard)
                .collect(Collectors.toList());

        // 3. 해당 카드들의 시세 정보를 한 방에 조회 (쿼리 1번, IN절 사용)
        List<MarketPrice> prices = marketPriceRepository.findAllByCardIn(cards);

        // 4. 조회한 시세 리스트를 Map으로 변환 (Key: CardId, Value: MarketPrice)
        // 이렇게 하면 나중에 O(1) 속도로 시세를 찾을 수 있습니다.
        Map<Long, MarketPrice> priceMap = prices.stream()
                .collect(Collectors.toMap(
                        mp -> mp.getCard().getId(),         // Key
                        Function.identity(),                                // Value (Marketprice 객체 자체)
                        (p1, p2) -> p1       // (혹시 중복 키가 있다면 기존 것 사용)
                ));

        // 5. 메모리 상에서 매칭 (DB 조회 없음)
        return myCards.stream().map(userCard -> {
            // Map에서 꺼내기 (DB 접근 X, 메모리 접근 O)
            MarketPrice marketPrice = priceMap.get(userCard.getCard().getId());

            return MyCollectionResponse.from(userCard, marketPrice);
        }).collect(Collectors.toList());
    }

    @Transactional
    public Long addCardToCollection(AddCollectionRequest request) {
        // 1. 유저 조회(로그인 기능 전이라 ID 1번 유저 고정)
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("임시 유저(ID: 1)가 없습니다. SQL을 실행해주세요."));

        // 2. 카드 조회
        Card card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다."));

        // 3. UserCard 생성 및 저장
        UserCard userCard = UserCard.builder()
                .user(user)
                .card(card)
                .purchasePrice(request.purchasePrice())
                .cardCondition(request.condition())
                .status(CollectionStatus.OWNED) // 기본값: 보유중
                .memo(request.memo())
                .build();

        return userCardRepository.save(userCard).getId();
    }

    @Transactional(readOnly = true)
    public CollectionSummaryResponse getCollectionSummary(Long userId) {
        // 1. 유저 카드 목록 조회
        List<UserCard> myCards = userCardRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        if (myCards.isEmpty()) {
            return CollectionSummaryResponse.of(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // 2. 카드 엔티티 추출 및 시세 조회 (Map 최적화 사용)
        List<Card> cards = myCards.stream().map(UserCard::getCard).collect(Collectors.toList());
        List<MarketPrice> prices = marketPriceRepository.findAllByCardIn(cards);

        Map<Long, MarketPrice> priceMap = prices.stream()
                .collect(Collectors.toMap(mp -> mp.getCard().getId(), Function.identity(), (p1, p2) -> p1));

        // 3. 총 구매액 & 총 평가액 합산
        BigDecimal totalBuy = BigDecimal.ZERO;
        BigDecimal totalCurr = BigDecimal.ZERO;

        for (UserCard userCard : myCards) {
            // 구매가 합산
            if (userCard.getPurchasePrice() != null) {
                totalBuy.add(userCard.getPurchasePrice());
            }

            // 현재가 합산
            MarketPrice mp = priceMap.get(userCard.getCard().getId());
            if (mp != null && mp.getPrice() != null) {
                totalCurr = totalCurr.add(mp.getPrice());
            }
        }

        return CollectionSummaryResponse.of(totalBuy, totalCurr);
    }
}
