package com.kobe.pokekernle.domain.admin.controller;

import com.kobe.pokekernle.domain.admin.service.ImageUploadService;
import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.entity.PriceHistory;
import com.kobe.pokekernle.domain.card.entity.Rarity;
import com.kobe.pokekernle.domain.collection.entity.CardCondition;
import com.kobe.pokekernle.domain.collection.entity.CollectionStatus;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import com.kobe.pokekernle.domain.card.repository.MarketPriceRepository;
import com.kobe.pokekernle.domain.card.repository.PriceHistoryRepository;
import com.kobe.pokekernle.domain.card.response.CardListResponse;
import com.kobe.pokekernle.domain.card.service.CardService;
import com.kobe.pokekernle.domain.collection.repository.UserCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * packageName    : com.kobe.pokekernle.domain.admin.controller
 * fileName       : AdminController
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ImageUploadService imageUploadService;
    private final CardService cardService;
    private final CardRepository cardRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final UserCardRepository userCardRepository;

    // 1. 카드 등록 페이지 보여주기
    @GetMapping("/cards/register")
    public String registerPage(Model model) {
        model.addAttribute("rarities", Rarity.values());
        model.addAttribute("cardConditions", CardCondition.values());
        model.addAttribute("collectionStatuses", CollectionStatus.values());
        return "admin/register"; // templates/admin/register.html
    }

    // 2. 관리자용 카드 목록 페이지
    @GetMapping("/cards/list")
    public String cardList(Model model) {
        List<CardListResponse> cards = cardService.getAllCards();
        model.addAttribute("cards", cards);
        return "admin/card-list"; // templates/admin/card-list.html
    }

    // 2-1. 관리자용 카드 목록 데이터 (JSON, AJAX용)
    @GetMapping("/cards/list-data")
    @ResponseBody
    public ResponseEntity<List<CardListResponse>> cardListData() {
        List<CardListResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    // 3. 카드 수동 등록
    @PostMapping("/cards/register")
    public String registerCard(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String setName,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String cardCondition,
            @RequestParam(required = false) String collectionStatus,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String salePrice,
            @RequestParam(required = false) String currentPriceUsd,
            RedirectAttributes redirectAttributes
    ) {
        // 이미지 업로드 처리
        String uploadedImageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                uploadedImageUrl = imageUploadService.uploadImage(imageFile);
                log.info("[ADMIN] 이미지 업로드 완료: {}", uploadedImageUrl);
            } catch (Exception e) {
                log.error("[ADMIN] 이미지 업로드 실패", e);
                redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패: " + e.getMessage());
                return "redirect:/admin/cards/register";
            }
        }

        // 필수 정보 검증
        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "카드 이름은 필수입니다.");
            return "redirect:/admin/cards/register";
        }

        try {
            // 판매 가격 파싱 (원화)
            Long salePriceLong = null;
            if (salePrice != null && !salePrice.isBlank()) {
                try {
                    salePriceLong = Long.parseLong(salePrice.trim());
                } catch (NumberFormatException e) {
                    log.warn("[ADMIN] 판매 가격 파싱 실패: {}", salePrice);
                }
            }

            // Rarity 파싱
            Rarity rarityEnum = null;
            if (rarity != null && !rarity.isBlank()) {
                try {
                    rarityEnum = Rarity.valueOf(rarity.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("[ADMIN] Rarity 파싱 실패: {}", rarity);
                }
            }

            // CardCondition 파싱
            CardCondition cardConditionEnum = null;
            if (cardCondition != null && !cardCondition.isBlank()) {
                try {
                    cardConditionEnum = CardCondition.valueOf(cardCondition.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("[ADMIN] CardCondition 파싱 실패: {}", cardCondition);
                }
            }

            // CollectionStatus 파싱
            CollectionStatus collectionStatusEnum = null;
            if (collectionStatus != null && !collectionStatus.isBlank()) {
                try {
                    collectionStatusEnum = CollectionStatus.valueOf(collectionStatus.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("[ADMIN] CollectionStatus 파싱 실패: {}", collectionStatus);
                }
            }

            // 카드 생성
            Card card = Card.builder()
                    .name(name.trim())
                    .setName(setName != null && !setName.isBlank() ? setName.trim() : "Unknown Set")
                    .number(number != null && !number.isBlank() ? number.trim() : null)
                    .rarity(rarityEnum)
                    .cardCondition(cardConditionEnum)
                    .collectionStatus(collectionStatusEnum)
                    .imageUrl(imageUrl != null && !imageUrl.isBlank() ? imageUrl.trim() : null)
                    .uploadedImageUrl(uploadedImageUrl)
                    .salePrice(salePriceLong)
                    .build();

            cardRepository.save(card);
            log.info("[ADMIN] 카드 등록 완료 - Card ID: {}", card.getId());

            // 현재 시세(USD) 처리
            if (currentPriceUsd != null && !currentPriceUsd.isBlank()) {
                try {
                    BigDecimal price = new BigDecimal(currentPriceUsd.trim());
                    
                    // MarketPrice 업데이트 또는 생성
                    marketPriceRepository.findByCard(card)
                            .ifPresentOrElse(
                                    marketPrice -> {
                                        marketPrice.updatePrice(price);
                                        marketPriceRepository.save(marketPrice);
                                        log.info("[ADMIN] MarketPrice 업데이트 완료 - Card ID: {}, Price: ${}", card.getId(), price);
                                    },
                                    () -> {
                                        MarketPrice marketPrice = MarketPrice.builder()
                                                .card(card)
                                                .price(price)
                                                .currency("USD")
                                                .source("Manual")
                                                .build();
                                        marketPriceRepository.save(marketPrice);
                                        log.info("[ADMIN] MarketPrice 생성 완료 - Card ID: {}, Price: ${}", card.getId(), price);
                                    }
                            );
                    
                    // PriceHistory에 기록 추가 (그래프용)
                    PriceHistory priceHistory = PriceHistory.builder()
                            .card(card)
                            .price(price)
                            .recordedAt(java.time.LocalDate.now())
                            .build();
                    priceHistoryRepository.save(priceHistory);
                    log.info("[ADMIN] PriceHistory 기록 추가 완료 - Card ID: {}, Price: ${}", card.getId(), price);
                } catch (NumberFormatException e) {
                    log.warn("[ADMIN] 현재 시세(USD) 파싱 실패: {}", currentPriceUsd);
                    redirectAttributes.addFlashAttribute("error", "현재 시세(USD) 형식이 올바르지 않습니다.");
                }
            }

            String successMessage = "카드가 등록되었습니다!";
            if (currentPriceUsd != null && !currentPriceUsd.isBlank()) {
                successMessage += " 현재 시세(USD): $" + currentPriceUsd + "가 기록되었습니다.";
            }
            redirectAttributes.addFlashAttribute("message", successMessage);
        } catch (Exception e) {
            log.error("[ADMIN] 카드 등록 실패", e);
            redirectAttributes.addFlashAttribute("error", "카드 등록 중 오류 발생: " + e.getMessage());
        }

        return "redirect:/admin/cards/register";
    }

    // 5. 카드 수정 페이지 보여주기
    @GetMapping("/cards/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다. ID=" + id));
        model.addAttribute("card", card);
        model.addAttribute("rarities", Rarity.values());
        model.addAttribute("cardConditions", CardCondition.values());
        model.addAttribute("collectionStatuses", CollectionStatus.values());
        
        // 현재 시세 정보 가져오기
        marketPriceRepository.findByCard(card).ifPresent(marketPrice -> {
            model.addAttribute("currentMarketPrice", marketPrice);
        });
        
        return "admin/edit"; // templates/admin/edit.html
    }

    // 6. 카드 수정 처리
    @PostMapping("/cards/edit/{id}")
    @Transactional
    public String updateCard(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String setName,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String cardCondition,
            @RequestParam(required = false) String collectionStatus,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) String salePrice,
            @RequestParam(required = false) String currentPriceUsd,
            RedirectAttributes redirectAttributes
    ) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다. ID=" + id));

        // 이미지 업로드 처리
        String uploadedImageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                uploadedImageUrl = imageUploadService.uploadImage(imageFile);
                log.info("[ADMIN] 이미지 업로드 완료: {}", uploadedImageUrl);
            } catch (Exception e) {
                log.error("[ADMIN] 이미지 업로드 실패", e);
                redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패: " + e.getMessage());
                return "redirect:/admin/cards/edit/" + id;
            }
        }

        // 판매 가격 파싱
        Long salePriceLong = null;
        if (salePrice != null && !salePrice.isBlank()) {
            try {
                salePriceLong = Long.parseLong(salePrice.trim());
            } catch (NumberFormatException e) {
                log.warn("[ADMIN] 판매 가격 파싱 실패: {}", salePrice);
            }
        }

        // Rarity 파싱
        Rarity rarityEnum = null;
        if (rarity != null && !rarity.isBlank()) {
            try {
                rarityEnum = Rarity.valueOf(rarity.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[ADMIN] Rarity 파싱 실패: {}", rarity);
            }
        }

        // CardCondition 파싱
        CardCondition cardConditionEnum = null;
        if (cardCondition != null && !cardCondition.isBlank()) {
            try {
                cardConditionEnum = CardCondition.valueOf(cardCondition.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[ADMIN] CardCondition 파싱 실패: {}", cardCondition);
            }
        }

        // CollectionStatus 파싱
        CollectionStatus collectionStatusEnum = null;
        if (collectionStatus != null && !collectionStatus.isBlank()) {
            try {
                collectionStatusEnum = CollectionStatus.valueOf(collectionStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[ADMIN] CollectionStatus 파싱 실패: {}", collectionStatus);
            }
        }

        // 카드 정보 업데이트
        card.updateCard(
                name,
                setName,
                number,
                rarityEnum,
                cardConditionEnum,
                collectionStatusEnum,
                imageUrl,
                uploadedImageUrl != null ? uploadedImageUrl : card.getUploadedImageUrl(),
                salePriceLong
        );

        cardRepository.save(card);
        log.info("[ADMIN] 카드 수정 완료 - Card ID: {}", id);

        // 현재 시세(USD) 처리
        if (currentPriceUsd != null && !currentPriceUsd.isBlank()) {
            try {
                BigDecimal price = new BigDecimal(currentPriceUsd.trim());
                
                // MarketPrice 업데이트 또는 생성
                marketPriceRepository.findByCard(card)
                        .ifPresentOrElse(
                                marketPrice -> {
                                    marketPrice.updatePrice(price);
                                    marketPriceRepository.save(marketPrice);
                                    log.info("[ADMIN] MarketPrice 업데이트 완료 - Card ID: {}, Price: ${}", card.getId(), price);
                                },
                                () -> {
                                    MarketPrice marketPrice = MarketPrice.builder()
                                            .card(card)
                                            .price(price)
                                            .currency("USD")
                                            .source("Manual")
                                            .build();
                                    marketPriceRepository.save(marketPrice);
                                    log.info("[ADMIN] MarketPrice 생성 완료 - Card ID: {}, Price: ${}", card.getId(), price);
                                }
                        );
                
                // PriceHistory에 기록 추가 (그래프용)
                PriceHistory priceHistory = PriceHistory.builder()
                        .card(card)
                        .price(price)
                        .recordedAt(java.time.LocalDate.now())
                        .build();
                priceHistoryRepository.save(priceHistory);
                log.info("[ADMIN] PriceHistory 기록 추가 완료 - Card ID: {}, Price: ${}", card.getId(), price);
            } catch (NumberFormatException e) {
                log.warn("[ADMIN] 현재 시세(USD) 파싱 실패: {}", currentPriceUsd);
                redirectAttributes.addFlashAttribute("error", "현재 시세(USD) 형식이 올바르지 않습니다.");
            }
        }

        String successMessage = "카드 정보가 수정되었습니다.";
        if (currentPriceUsd != null && !currentPriceUsd.isBlank()) {
            successMessage += " 현재 시세(USD): $" + currentPriceUsd + "가 기록되었습니다.";
        }
        redirectAttributes.addFlashAttribute("message", successMessage);
        return "redirect:/admin/cards/list";
    }

    // 7. 카드 삭제 처리
    @PostMapping("/cards/delete/{id}")
    @Transactional
    public String deleteCard(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Card card = cardRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다. ID=" + id));

            // 연관된 데이터 먼저 삭제 (외래키 제약조건 해결)
            // 1. MarketPrice 삭제
            marketPriceRepository.findByCard(card).ifPresent(marketPriceRepository::delete);
            log.info("[ADMIN] MarketPrice 삭제 완료 - Card ID: {}", id);

            // 2. PriceHistory 삭제
            priceHistoryRepository.deleteAll(priceHistoryRepository.findAllByCardOrderByRecordedAtAsc(card));
            log.info("[ADMIN] PriceHistory 삭제 완료 - Card ID: {}", id);

            // 3. UserCard 삭제
            userCardRepository.deleteAll(userCardRepository.findByCard(card));
            log.info("[ADMIN] UserCard 삭제 완료 - Card ID: {}", id);

            // 4. Card 삭제
            cardRepository.delete(card);
            log.info("[ADMIN] 카드 삭제 완료 - Card ID: {}", id);

            redirectAttributes.addFlashAttribute("message", "카드가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("[ADMIN] 카드 삭제 실패 - Card ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "카드 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/admin/cards/list";
    }
}
