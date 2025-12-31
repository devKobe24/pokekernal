package com.kobe.pokekernle.global.config;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.entity.PriceHistory;
import com.kobe.pokekernle.domain.card.entity.Rarity;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import com.kobe.pokekernle.domain.card.repository.MarketPriceRepository;
import com.kobe.pokekernle.domain.card.repository.PriceHistoryRepository;
import com.kobe.pokekernle.domain.collection.entity.CardCondition;
import com.kobe.pokekernle.domain.collection.entity.CollectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * packageName    : com.kobe.pokekernle.global.config
 * fileName       : CardInitializer
 * author         : kobe
 * date           : 2025. 12. 31.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 31.        kobe       최초 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardInitializer implements CommandLineRunner {

    private final CardRepository cardRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Value("${card.name:Psyduck}")
    private String name;

    @Value("${card.set_name:sv2a}")
    private String setName;

    @Value("${card.number:175}")
    private String number;

    @Value("${card.image_url:/uploads/images/2e8f25b3-a9cd-40df-a74d-7181d3f9cd6c.jpeg}")
    private String imageUrl;

    @Value("${card.uploaded_image_url:/uploads/images/2e8f25b3-a9cd-40df-a74d-7181d3f9cd6c.jpeg}")
    private String uploadedImageUrl;

    @Value("${card.market_price_usd:25.99}")
    private String marketPriceUsdStr;

    @Value("${card.sale_price:43000}")
    private Long salePrice;

    @Value("${card.quantity:1}")
    private Integer quantity;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[CARD INIT] 개발용 카드 초기화 시작");
        log.info("[CARD INIT]        카드 세트 이름: {}", setName);

        // 이미 카드가 등록되어 있는지 확인
        if (cardRepository.findBySetName(setName).isPresent()) {
            log.info("[CARD INIT] 개발용 카드가 이미 존재합니다: {}, {}", name, setName);
            return;
        }

        // 개발용 카드 생성
        if (setName == null || setName.isEmpty()) {
            log.warn("[CARD INIT] 개발용 카드 SET NAME이 설정되지 않았습니다. 카드 등록을 건너뜁니다.");
            return;
        }

        if (uploadedImageUrl == null || uploadedImageUrl.isEmpty()) {
            log.warn("[CARD INIT] 개발용 카드 이미지를 불러오지 못했습니다. 카드 등록을 건너뜁니다.");
        }

        Card card = Card.builder()
                .name(name)
                .setName(setName)
                .number(number)
                .rarity(Rarity.ART_RARE)
                .cardCondition(CardCondition.NEAR_MINT)
                .collectionStatus(CollectionStatus.OWNED)
                .imageUrl(imageUrl)
                .uploadedImageUrl(uploadedImageUrl)
                .salePrice(salePrice)
                .quantity(quantity)
                .build();

        cardRepository.save(card);
        log.info("[CARD INIT] 개발용 카드가 생성되었습니다:");
        log.info("[CARD INIT]        카드 이름: {}", name);
        log.info("[CARD INIT]        카드 세트 이름: {}", setName);

        // MarketPrice 생성 (USD 시세)
        if (marketPriceUsdStr != null && !marketPriceUsdStr.isBlank()) {
            try {
                BigDecimal marketPriceUsd = new BigDecimal(marketPriceUsdStr.trim());
                if (marketPriceUsd.compareTo(BigDecimal.ZERO) > 0) {
                    MarketPrice marketPrice = MarketPrice.builder()
                            .card(card)
                            .price(marketPriceUsd)
                            .currency("USD")
                            .source("Manual")
                            .build();
                    marketPriceRepository.save(marketPrice);
                    log.info("[CARD INIT] 시세 정보 생성 완료 - USD: ${}", marketPriceUsd);

                    // PriceHistory에도 기록 추가 (그래프용)
                    PriceHistory priceHistory = PriceHistory.builder()
                            .card(card)
                            .price(marketPriceUsd)
                            .recordedAt(LocalDate.now())
                            .build();
                    priceHistoryRepository.save(priceHistory);
                    log.info("[CARD INIT] 시세 히스토리 기록 추가 완료 - USD: ${}", marketPriceUsd);
                }
            } catch (NumberFormatException e) {
                log.warn("[CARD INIT] USD 시세 파싱 실패: {}, 시세 정보를 생성하지 않습니다.", marketPriceUsdStr);
            }
        }
    }
}
