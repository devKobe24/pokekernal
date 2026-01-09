package com.kobe.pokekernle.domain.card.service;

import com.kobe.pokekernle.domain.card.dto.response.CardDetailResponse;
import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.entity.PriceHistory;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import com.kobe.pokekernle.domain.card.repository.MarketPriceRepository;
import com.kobe.pokekernle.domain.card.repository.PriceHistoryRepository;
import com.kobe.pokekernle.domain.card.response.CardListResponse;
import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBox;
import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBoxMarketPrice;
import com.kobe.pokekernle.domain.onepiece.box.repository.OnePieceBoxRepository;
import com.kobe.pokekernle.domain.onepiece.box.repository.OnePieceBoxMarketPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * packageName    : com.kobe.pokekernle.domain.card.service
 * fileName       : CardService
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
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final CurrencyConverterService currencyConverterService;
    private final OnePieceBoxRepository onePieceBoxRepository;
    private final OnePieceBoxMarketPriceRepository onePieceBoxMarketPriceRepository;

    public CardDetailResponse getCardDetail(Long cardId) {
        // 1. 카드 조회 (없으면 404 예외)
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다. ID=" + cardId));

        // 2. 현재 시세 조회
        MarketPrice marketPrice = marketPriceRepository.findByCard(card).orElse(null);

        // 3. 시세 히스토리 조회(그래프용)
        List<PriceHistory> histories = priceHistoryRepository.findAllByCardOrderByRecordedAtAsc(card);

        // 4. DTO 변환 (USD로 변환하여 표시)
        return CardDetailResponse.of(card, marketPrice, histories, currencyConverterService);
    }

    public List<CardListResponse> getAllCards() {
        List<CardListResponse> result = new ArrayList<>();

        // 1. 모든 카드 조회 (실무에선 페이징 필수, 지금은 MVP라 전체 조회)
        List<Card> cards = cardRepository.findAll();

        // 2. 시세 정보 조회 (N+1 문제 방지를 위해 미리 다 가져오거나 Fetch Join 사용)
        // 여기서는 간단하게 모든 시세를 가져와 메모리에서 매핑합니다.
        List<MarketPrice> prices = marketPriceRepository.findAll();

        // 카드 ID를 키로 하는 시세 맵 생성
        Map<Long, MarketPrice> priceMap = prices.stream()
                .filter(mp -> mp.getCard() != null)
                .collect(Collectors.toMap(mp -> mp.getCard().getId(), Function.identity(), (p1, p2) -> p1));

        // 3. 시세 히스토리 조회 (시세 변동 계산용)
        // 성능 최적화: 모든 카드의 히스토리를 한 번에 조회
        List<PriceHistory> allHistories = priceHistoryRepository.findAll();
        Map<Long, List<PriceHistory>> historyMap = allHistories.stream()
                .filter(h -> h.getCard() != null)
                .collect(Collectors.groupingBy(h -> h.getCard().getId()));

        // 4. 카드 DTO 변환 (USD로 변환하여 표시, 시세 변동 포함)
        List<CardListResponse> cardResponses = cards.stream()
                .map(card -> CardListResponse.from(
                        card, 
                        priceMap.get(card.getId()), 
                        historyMap.get(card.getId()),
                        currencyConverterService))
                .collect(Collectors.toList());
        result.addAll(cardResponses);

        // 5. 원피스 박스 조회 및 변환
        List<OnePieceBox> boxes = onePieceBoxRepository.findAll();
        List<OnePieceBoxMarketPrice> boxPrices = onePieceBoxMarketPriceRepository.findAll();
        Map<Long, OnePieceBoxMarketPrice> boxPriceMap = boxPrices.stream()
                .filter(mp -> mp.getOnePieceBox() != null)
                .collect(Collectors.toMap(mp -> mp.getOnePieceBox().getId(), Function.identity(), (p1, p2) -> p1));

        List<CardListResponse> boxResponses = boxes.stream()
                .map(box -> CardListResponse.fromOnePieceBox(box, boxPriceMap.get(box.getId())))
                .collect(Collectors.toList());
        result.addAll(boxResponses);

        return result;
    }
}
