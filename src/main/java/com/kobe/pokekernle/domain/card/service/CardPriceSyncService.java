package com.kobe.pokekernle.domain.card.service;

import com.kobe.pokekernle.domain.card.dto.external.PokemonCardDto;
import com.kobe.pokekernle.domain.card.dto.external.PokemonTcgApiResponse;
import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.entity.Rarity;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import com.kobe.pokekernle.domain.card.repository.MarketPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * packageName    : com.kobe.pokekernle.domain.card.service
 * fileName       : CardPriceSyncService
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardPriceSyncService {

    private final CardDataProvider dataProvider;
    private final CardRepository cardRepository;
    private final MarketPriceRepository marketPriceRepository;

    /**
     * 특정 검색 조건(Query)에 해당하는 카드들의 최신 시세를 동기화합니다.
     * MVP 규칙: 최신 시세 1개만 유지 (UPDATE)
     */
    @Transactional
    public void syncLatestPrices(String query) {
        log.info("[BATCH] 시세 데이터 동기화 시작. Query: {}", query);

        PokemonTcgApiResponse response = dataProvider.fetchCardsBySet(query);

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.warn("[BATCH] 수집된 데이터가 없습니다.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (PokemonCardDto dto : response.getData()) {
            try {
                processCardData(dto);
                successCount++;
            } catch (Exception e) {
                failCount++;
                // 에러 로그를 상세하게 찍어서 원인을 파악합니다.
                log.error("[BATCH] 카드 처리 실패 - ID: {}, Name: {}", dto.getId(), dto.getName());
                log.error("에러 원인:", e); // 스택 트레이스 출력
            }
        }

        log.info("[BATCH] 시세 동기화 완료. 처리 건수: {}", successCount);
        log.info("[BATCH] 시세 동기화 완료. 성공: {}, 실패: {}", successCount, failCount);
    }

    private void processCardData(PokemonCardDto dto) {
        // 1. 카드 정보 동기 (없으면 생성, 있으면 조회)
        Card card = cardRepository.findByExternalId(dto.getId())
                .orElseGet(() -> createNewCard(dto));

        // 카드가 새로 생성되었는데 ID가 없다면 save 필요
        if (card.getId() == null) {
            card = cardRepository.save(card);
        }

        // 2. 시세 정보 동기화
        // API 응답에 cardMarket(유럽) 정보가 있는 경우에만 처리
        if (dto.getCardmarket() != null && dto.getCardmarket().getPrices() != null) {
            BigDecimal newPrice = dto.getCardmarket().getPrices().getTrendPrice();
            updateOrInsertPrice(card, newPrice);
        } else {
            // cardMarket 정보가 없는 경우 로그만 남기고 통과 (에러 아님)
            log.debug("시세 정보 없음 (CardMarket is null) - ID: {}", dto.getId());
        }
    }

    private void updateOrInsertPrice(Card card, BigDecimal price) {
        if (price == null) {
            return;
        }

        MarketPrice marketPrice = marketPriceRepository.findByCard(card)
                .orElse(null);

        if (marketPrice == null) {
            // [CASE 1] 시세 정보가 없으면 -> 새로 만들기 (Insert)
            marketPrice = MarketPrice.builder()
                    .card(card)
                    .price(price)
                    .currency("EUR") // CardMarket API는 기본적으로 유로(EUR) 기준
                    .build();

            marketPriceRepository.save(marketPrice); // 저장
        } else {
            // [CASE 2] 이미 있으면 -> 가격만 업데이트 (Update)
            // Dirty Checking에 의해 트랜젝션 종료 시 자동 반영됨
            marketPrice.updatePrice(price);
        }
    }

    private Card createNewCard(PokemonCardDto dto) {
        // [수정] Null-Safe 로직 적용: API 응답의 일부 필드가 비어있어도 죽지 않도록 방어
        String setName = (dto.getSet() != null) ? dto.getSet().getName() : "Unknown Set";
        String imageUrl = (dto.getImages() != null) ? dto.getImages().getLarge() : null;

        return Card.builder()
                .externalId(dto.getId())
                .name(dto.getName())
                .setName(dto.getSet().getName())
                .number(dto.getNumber())
                .rarity(parseRarity(dto.getRarity()))
                .imageUrl(dto.getImages().getLarge())
                .build();
    }

    private Rarity parseRarity(String rarityStr) {
        if (rarityStr == null || rarityStr.isEmpty()) {
            return Rarity.UNKNOWN;
        }

        // 대소문자 무시 및 공백 제거
        String rarity = rarityStr.trim();

        return switch (rarity) {
            // 1. 기본 등급
            case "Common" -> Rarity.COMMON;
            case "Uncommon" -> Rarity.UNCOMMON;
            case "Rare" -> Rarity.RARE;

            // 2. 홀로(Holo) 계열
            case "Rare Holo", "Rare Holo Galaxy", "Prism Rare" -> Rarity.HOLO_RARE;

            // 3. 더블 레어 (V, EX, GX 기본 등급)
            case "Double Rare", "Rare Holo V", "Rare Holo EX", "Rare Holo GX", "Classic Collection" -> Rarity.DOUBLE_RARE;

            // 4. 울트라 레어(VMAX, VSTAR, Full Art)
            case "Ultra Rare", "Rare Ultra", "Rare Holo VMAX", "Rare Holo VSTAR", "Rare Holo LV.X", "LEGEND", "Rare Prime", "Rare BREAK" -> Rarity.ULTRA_RARE;

            // 5. 일러스트 레어 (최신 트렌드: AR, SAR, CHR)
            case "Illustration Rare", "Special Illustration Rare", "Trainer Gallery Rare Holo", "Shiny Rare" -> Rarity.ILLUSTRATION_RARE;

            // 6. 시크릿 레어 (금색, 무지개색 등)
            case "Secret Rare", "Hyper Rare", "Rainbow Rare", "Gold Rare", "Rare Secret", "Rare Shining", "Amazing Rare" -> Rarity.SECRET_RARE;

            // 7. 프로모
            case "Promo" -> Rarity.PROMO;

            // 8. 매핑되지 않은 새로운 등급이 나올 경우
            default -> {
                log.warn("[Rarity Parsing] 알 수 없는 희귀도 발견: {}", rarity);
                // "Rare"라는 단어가 포함되어 있으면 일단 RARE로 퉁치거나 UNKNOWN 처리
                if (rarity.contains("Rare")) {
                    yield Rarity.RARE;
                }
                yield Rarity.UNKNOWN;
            }
        };
    }
}
