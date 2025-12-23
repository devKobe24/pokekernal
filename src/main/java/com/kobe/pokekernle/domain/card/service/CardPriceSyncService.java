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
     * 페이지네이션을 지원하여 모든 데이터를 수집합니다.
     */
    @Transactional
    public void syncLatestPrices(String query) {
        syncLatestPrices(query, null);
    }

    /**
     * 특정 검색 조건(Query)에 해당하는 카드들의 최신 시세를 동기화합니다.
     * 업로드된 이미지 URL이 있으면 첫 번째로 매칭되는 카드에 적용합니다.
     */
    @Transactional
    public void syncLatestPrices(String query, String uploadedImageUrl) {
        syncLatestPrices(query, uploadedImageUrl, null);
    }

    /**
     * 특정 검색 조건(Query)에 해당하는 카드들의 최신 시세를 동기화합니다.
     * 업로드된 이미지 URL과 판매 가격이 있으면 첫 번째로 매칭되는 카드에 적용합니다.
     */
    @Transactional
    public void syncLatestPrices(String query, String uploadedImageUrl, Long salePrice) {
        log.info("[BATCH] 시세 데이터 동기화 시작. Query: {}", query);

        int totalSuccessCount = 0;
        int totalFailCount = 0;
        int currentPage = 1;
        int pageSize = 5; // 타임아웃 방지를 위해 최소 페이지 크기 사용 (응답 속도 최적화)
        boolean hasMorePages = true;
        
        // 광범위한 쿼리 경고
        if (query.contains("name:") && !query.contains("set.id:")) {
            log.warn("[BATCH] 광범위한 쿼리 감지: {}. 특정 세트로 제한하는 것을 권장합니다. (예: name:pikachu set.id:sv3pt5)", query);
        }

        while (hasMorePages) {
            log.info("[BATCH] 페이지 {} 처리 중... (pageSize: {})", currentPage, pageSize);
            
            // 페이지 요청 간 최소 딜레이 (Rate limiting 방지, 첫 페이지는 딜레이 없음)
            if (currentPage > 1) {
                try {
                    Thread.sleep(500); // 0.5초 대기로 단축 (API rate limit 고려)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("[BATCH] 딜레이 중 인터럽트 발생");
                }
            }
            
            PokemonTcgApiResponse response;
            try {
                response = dataProvider.fetchCardsBySet(query, currentPage, pageSize);
            } catch (Exception e) {
                log.error("[BATCH] 페이지 {} 요청 실패. 다음 페이지로 진행하지 않습니다. - Query: {}", 
                        currentPage, query, e);
                break; // 현재 페이지 실패 시 중단
            }

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                log.info("[BATCH] 페이지 {}에 데이터가 없습니다. 수집 종료.", currentPage);
                break;
            }

            int pageSuccessCount = 0;
            int pageFailCount = 0;

            for (PokemonCardDto dto : response.getData()) {
                try {
                    // 업로드된 이미지 URL과 판매 가격이 있고 아직 적용되지 않았다면 첫 번째 카드에 적용
                    boolean isFirstCard = (pageSuccessCount == 0 && currentPage == 1);
                    String imageUrlToApply = (uploadedImageUrl != null && !uploadedImageUrl.isBlank() && isFirstCard) 
                            ? uploadedImageUrl 
                            : null;
                    Long salePriceToApply = (salePrice != null && isFirstCard) ? salePrice : null;
                    processCardData(dto, imageUrlToApply, salePriceToApply);
                    pageSuccessCount++;
                } catch (Exception e) {
                    pageFailCount++;
                    // 에러 로그를 상세하게 찍어서 원인을 파악합니다.
                    log.error("[BATCH] 카드 처리 실패 - ID: {}, Name: {}", dto.getId(), dto.getName());
                    log.error("에러 원인:", e); // 스택 트레이스 출력
                }
            }

            totalSuccessCount += pageSuccessCount;
            totalFailCount += pageFailCount;

            log.info("[BATCH] 페이지 {} 완료. 성공: {}, 실패: {}", currentPage, pageSuccessCount, pageFailCount);

            // 다음 페이지가 있는지 확인
            Integer totalCount = response.getTotalCount();
            Integer count = response.getCount();
            int processedCount = currentPage * pageSize;

            if (totalCount != null && processedCount >= totalCount) {
                hasMorePages = false;
                log.info("[BATCH] 모든 페이지 처리 완료. 총 {}개 중 {}개 처리됨", totalCount, processedCount);
            } else if (count == null || count < pageSize) {
                // 현재 페이지의 데이터가 pageSize보다 적으면 마지막 페이지
                hasMorePages = false;
                log.info("[BATCH] 마지막 페이지 도달");
            } else {
                currentPage++;
            }
        }

        log.info("[BATCH] 시세 동기화 완료. 총 성공: {}, 총 실패: {}", totalSuccessCount, totalFailCount);
    }

    private void processCardData(PokemonCardDto dto) {
        processCardData(dto, null, null);
    }

    private void processCardData(PokemonCardDto dto, String uploadedImageUrl) {
        processCardData(dto, uploadedImageUrl, null);
    }

    private void processCardData(PokemonCardDto dto, String uploadedImageUrl, Long salePrice) {
        // 1. 카드 정보 동기 (없으면 생성, 있으면 조회)
        Card card = cardRepository.findByExternalId(dto.getId())
                .orElseGet(() -> createNewCard(dto, uploadedImageUrl, salePrice));

        // 카드가 새로 생성되었는데 ID가 없다면 save 필요
        if (card.getId() == null) {
            card = cardRepository.save(card);
        }

        // 업로드된 이미지 URL이 있으면 설정 (기존 카드도 업데이트)
        boolean needsSave = false;
        if (uploadedImageUrl != null && !uploadedImageUrl.isBlank()) {
            card.setUploadedImageUrl(uploadedImageUrl);
            needsSave = true;
            log.info("[BATCH] 업로드된 이미지 적용 - Card ID: {}, Image URL: {}", card.getId(), uploadedImageUrl);
        }

        // 판매 가격이 있으면 설정 (기존 카드도 업데이트)
        if (salePrice != null) {
            card.setSalePrice(salePrice);
            needsSave = true;
            log.info("[BATCH] 판매 가격 적용 - Card ID: {}, Sale Price: ₩{}", card.getId(), salePrice);
        }

        // 변경사항이 있으면 명시적으로 저장
        if (needsSave) {
            cardRepository.save(card);
            log.info("[BATCH] 카드 정보 업데이트 완료 - Card ID: {}", card.getId());
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
        return createNewCard(dto, null, null);
    }

    private Card createNewCard(PokemonCardDto dto, String uploadedImageUrl) {
        return createNewCard(dto, uploadedImageUrl, null);
    }

    private Card createNewCard(PokemonCardDto dto, String uploadedImageUrl, Long salePrice) {
        // [최적화] API에서 최소한의 데이터만 가져오므로, 없는 필드는 기본값 사용
        // 필요한 필드: id, name, rarity, cardmarket.prices.trendPrice
        // 선택적 필드: set.name, number, images (없으면 null 또는 기본값 사용)
        String setName = (dto.getSet() != null && dto.getSet().getName() != null) 
                ? dto.getSet().getName() 
                : "Unknown Set";
        String number = dto.getNumber(); // null 가능
        String imageUrl = null; // 이미지는 업로드된 이미지 사용하므로 API 이미지 불필요

        return Card.builder()
                .externalId(dto.getId())
                .name(dto.getName())
                .setName(setName)
                .number(number) // null 가능
                .rarity(parseRarity(dto.getRarity()))
                .imageUrl(imageUrl) // null (업로드된 이미지 사용)
                .uploadedImageUrl(uploadedImageUrl)
                .salePrice(salePrice) // 희망 판매 가격 (원화)
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
